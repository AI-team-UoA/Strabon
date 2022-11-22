#!/bin/bash
LOC="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

ENDPOINT="http://localhost:8080/endpoint"
DB="endpoint"

HOTSPOTS_URL="http://jose.di.uoa.gr/rdf/hotspots/MSG2"
#HOTSPOTS_URL="http://jose.di.uoa.gr/rdf/hotspots/MSG1"

logFile="chain.log"

function timer()
{
   if [[ $# -eq 0 ]]; then
       t=$(date '+%s%N')
       t=$((t/1000000))
       echo $t
   else
       local  stime=$1
       etime=$(date '+%s%N')
       etime=$((etime/1000000))

       if [[ -z "$stime" ]]; then stime=$etime; fi
       dt=$((etime - stime)) #dt in milliseconds
       dM=$((dt%1000))
       Dt=$((dt/1000)) #delta t in seconds
       ds=$((Dt % 60))
       dm=$(((Dt / 60) % 60))
       dh=$((Dt / 3600))
       printf '%d:%02d:%02d.%03d' $dh $dm $ds $dM
   fi
}

# find out the postgres service to use
postgres=$(ls -1 /etc/init.d/| grep postgres | head -1)

tomcat=
function chooseTomcat()
{
	if test -s /etc/fedora-release ; then
		tomcat="tomcat"
	#elif test -s /etc/centos-release ; then
	#elif test -s /etc/yellowdog-release ; then
	#elif test -s /etc/redhat-release ; then
	#elif test -s /etc/SuSE-release ; then
	#elif test -s /etc/gentoo-release ; then
	elif test -s /etc/lsb-release ; then # Ubuntu
			tomcat=$(ls -1 /etc/init.d/| grep tomcat | head -1)
	elif test -s /etc/debian_version ; then
		tomcat="tomcat"
	fi

	# check for service availability
	if ! test -e "/etc/init.d/${tomcat}"; then
		tomcat=
	fi
}

insertMunicipalities=`cat ${LOC}/insertMunicipalities.sparql` 
deleteSeaHotspots=`cat ${LOC}/deleteSeaHotspots.sparql` # | sed 's/\"/\\\"/g'`
refinePartialSeaHotspots=`cat ${LOC}/refinePartialSeaHotspots.sparql` # | sed 's/\"/\\\"/g'`
refineTimePersistence=`cat ${LOC}/refineTimePersistence.sparql` # | sed 's/\"/\\\"/g'`
invalidForFires=`cat ${LOC}/landUseInvalidForFires.sparql`
discover=`cat ${LOC}/discover.sparql`
#InsertMunicipalities =`cat ${LOC}/InsertMunicipalities.sparql` # | sed 's/\"/\\\"/g'`

# Initialize (stop tomcat, restart postgres, drop/create database, start tomcat)
chooseTomcat
echo "stopping tomcat"
if test -z "${tomcat}"; then
	# work-around for babis (standalone tomcat, with start_tomcat.sh and stop_tomcat.sh scripts)
	stop_tomcat.sh
else
	sudo service ${tomcat} stop
fi

sudo service ${postgres} restart

# get the main version of postgres
POSTGRES_MAIN_VERSION=$(sudo service ${postgres} status | grep -o '.\..' | cut -b 1)

echo "Dropping endpoint database";
dropdb  ${DB}

echo "Creating endpoint database"
createdb  ${DB} 

# load data


curl -s http://dev.strabon.di.uoa.gr/rdf/coastline-kallikrates_30000-excludedAreas-dump.tgz | tar xz -O | psql -d ${DB}
#curl -s http://dev.strabon.di.uoa.gr/rdf/Kallikratis-Coastline-Corine-dump-postgres-${POSTGRES_MAIN_VERSION}.tgz | tar xz -O | psql -d ${DB}
psql ${DB} -c 'VACUUM ANALYZE' 

echo "starting tomcat"
if test -z "${tomcat}"; then
	# work-around for babis (standalone tomcat, with start_tomcat.sh and stop_tomcat.sh scripts)
	start_tomcat.sh
	sleep 2
else
	sudo service ${tomcat} start
fi

echo "initializing database"
echo "Timestamp	Store Municipalities DeleteInSea InvalidForFires RefineInCoast TimePersistence" > stderr.txt


#./scripts/endpoint query ${ENDPOINT} "SELECT (COUNT(*) AS ?C) WHERE {?s ?p ?o}"
#sudo -u postgres psql -d endpoint -c 'CREATE INDEX datetime_values_idx_value ON datetime_values USING btree(value)';
#sudo -u postgres psql -d endpoint -c 'VACUUM ANALYZE;';

#for y in 2007 2008 2010 2011 ;do
for y in 2007; do
	# get hotpost URLS
	for hot in $(curl -s ${HOTSPOTS_URL}/${y}/ | grep -o '>HMSG.*\.nt' | colrm 1 1); do
		file="${HOTSPOTS_URL}/${y}/${hot}"

		time_status=$(echo ${hot} | egrep -o '[[:digit:]]{6}_[[:digit:]]{4}')
		
		# get sensor
		SENSOR=$(echo ${hot} | grep -o 'MSG.')

		# get satellite
		if test "${SENSOR}" = "MSG2"; then
			SAT="METEOSAT9"
		else
			SAT="METEOSAT8"
			SENSOR="MSG1_RSS"
		fi

		# get time information for acquisition and construct timestamp
		year="20$(expr substr ${time_status} 1 2)"
		month=$(expr substr ${time_status} 3 2)
		day=$(expr substr ${time_status} 5 2)
		time2=$(expr substr ${time_status} 8 2)
		time2="${time2}:$(expr substr ${time_status} 10 2)"

		# construct timestamp
		TIMESTAMP="${year}-${month}-${day}T${time2}:00"

		# store file
		echo -n "storing " $file; echo; echo; 
		# ${countTime} ./strabon -db endpoint store $file

		# print timestamp
		echo -n "${TIMESTAMP} " >> stderr.txt

		tmr1=$(timer)
		../endpoint store ${ENDPOINT} N-Triples -u ${file}
		tmr2=$(timer)
		printf '%s ' $((tmr2-tmr1)) >> stderr.txt

		# sudo -u postgres psql -d endpoint -c 'VACUUM ANALYZE;';

		echo;echo;echo;echo "File ${file} stored!" >> ${logFile}

		# insertMunicipalities
		echo -n "inserting Municipalities " ;echo; echo; echo;
		# query=`echo "${insertMunicipalities}" `
		# ${countTime} ./strabon -db endpoint update "${query}"

		tmr1=$(timer)

		query=`echo "${insertMunicipalities}" | sed "s/TIMESTAMP/${year}-${month}-${day}T${time2}:00/g" | \
		sed "s/PROCESSING_CHAIN/DynamicThresholds/g" | \
		sed "s/SENSOR/${SENSOR}/g"`

		../endpoint update ${ENDPOINT} "${query}"
		
		tmr2=$(timer)
		printf '%s ' $((tmr2-tmr1)) >>stderr.txt
		echo;echo;echo;echo "File ${file} inserted Municipalities!"

		# execute an explicit VACUUM ANALYZE when a query takes longer than it should
		duration=$((tmr2-tmr1))
		if test ${duration} -ge 30000; then
			psql ${DB} -c 'VACUUM ANALYZE' 
			echo "Explicit VACUUM ANALYZE"
		fi
		
		# deleteSeaHotspots
		echo -n "Going to deleteSeaHotspots ${year}-${month}-${day}T${time2}:00 " ;echo; echo; echo;
		query=`echo "${deleteSeaHotspots}" | sed "s/TIMESTAMP/${year}-${month}-${day}T${time2}:00/g" | \
		sed "s/PROCESSING_CHAIN/DynamicThresholds/g" | \
		sed "s/SENSOR/${SENSOR}/g"`
		# ${countTime} ./strabon -db endpoint update "${query}"

		tmr1=$(timer)
		../endpoint update ${ENDPOINT} "${query}"

		tmr2=$(timer)
		printf '%s ' $((tmr2-tmr1)) >>stderr.txt
		echo;echo;echo;echo "File ${file} deleteSeaHotspots done!"

		# echo "Continue?"
		# read a
			# invalidForFires
		echo -n "invalidForFires ${year}-${month}-${day}T${time2}:00 "  ; echo; echo ; echo;
		query=`echo "${invalidForFires}" | sed "s/TIMESTAMP/${year}-${month}-${day}T${time2}:00/g" | \
		sed "s/PROCESSING_CHAIN/DynamicThresholds/g" | \
		sed "s/SENSOR/${SENSOR}/g" |\
		sed "s/SAT/${SAT}/g"`
		# ${countTime} ./strabon -db endpoint update "${query}"
		tmr1=$(timer)
		../endpoint update ${ENDPOINT} "${query}"
		tmr2=$(timer)
		printf '%s ' $((tmr2-tmr1)) >>stderr.txt
		echo "File ${file} invalidForFires done!"
 
		# refinePartialSeaHotspots
		echo -n "refinePartialSeaHotspots ${year}-${month}-${day}T${time2}:00 "  ; echo; echo ; echo;
		query=`echo "${refinePartialSeaHotspots}" | sed "s/TIMESTAMP/${year}-${month}-${day}T${time2}:00/g" | \
		sed "s/PROCESSING_CHAIN/DynamicThresholds/g" | \
		sed "s/SENSOR/${SENSOR}/g" |\
		sed "s/SAT/${SAT}/g"`
		# ${countTime} ./strabon -db endpoint update "${query}"
		tmr1=$(timer)
		../endpoint update ${ENDPOINT} "${query}"
		tmr2=$(timer)
		printf '%s ' $((tmr2-tmr1)) >>stderr.txt

		echo "File ${file} refinePartialSeaHotspots done!"
		# echo "Continue?"
		# read a

		# refineTimePersistence
		echo -n "Going to refineTimePersistence ${year}-${month}-${day}T${time2}:00 ";echo;echo;echo; 
		min_acquisition_time=`date --date="${year}-${month}-${day} ${time2}:00 EEST -30 minutes" +%Y-%m-%dT%H:%M:00`
		newHotspotTimestamp=`date --date="${year}-${month}-${day} ${time2}:00" +%y%m%d_%H%M`
		
		query=`echo "${refineTimePersistence}" | sed "s/TIMESTAMP/${year}-${month}-${day}T${time2}:00/g" | \
		sed "s/PROCESSING_CHAIN/DynamicThresholds/g" | \
		sed "s/SENSOR/${SENSOR}/g" | \
		sed "s/ACQUISITIONS_IN_HALF_AN_HOUR/3.0/g" | \
		sed "s/MIN_ACQUISITION_TIME/${min_acquisition_time}/g" |\
		sed "s/SAT/${SAT}/g" | \
		sed "s/NEW_HOTSPOT/${newHotspotTimestamp}/g"`

		#sudo -u postgres psql -d ${DB} -c 'VACUUM ANALYZE;';

		tmr1=$(timer)
		../endpoint update ${ENDPOINT} "${query}"
		 tmr2=$(timer)
		printf '%s \n' $((tmr2-tmr1)) >>stderr.txt
		echo;echo;echo;echo "File ${file} timePersistence done!"
		# echo "Continue?"
		# read a


		# discover
		echo -n "Going to discover ${year}-${month}-${day}T${time2}:00 ";echo;echo;echo; 
		min_acquisition_time=`date --date="${year}-${month}-${day} 00:00 EEST" +%Y-%m-%dT%H:%M:00`
		max_acquisition_time=`date --date="${year}-${month}-${day} 23:59 EEST" +%Y-%m-%dT%H:%M:00`
		query=`echo "${discover}" | \
			sed "s/PROCESSING_CHAIN/DynamicThresholds/g" | \
			sed "s/SENSOR/${SENSOR}/g" | \
			sed "s/MIN_ACQUISITION_TIME/${min_acquisition_time}/g" |\
			sed "s/MAX_ACQUISITION_TIME/${max_acquisition_time}/g"`
			
		tmr1=$(timer)
		../endpoint query ${ENDPOINT} "${query}"
		tmr2=$(timer)
		printf '%s \n' $((tmr2-tmr1)) >>discover.txt
		echo;echo;echo;echo "Discovered hotspots done!"

	done
done

