#!/bin/bash
LOC="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

############################ CONFIGURATION #############################
ENDPOINT="http://localhost:8080/endpoint"
DB="endpoint"

HOTSPOTS_URL="http://localhost/rdf"
       URLDIR=( "msg1_rss"              "msg2")
    URLPREFIX=( "HMSG1_RSS_IR_039_s7_"  "HMSG2_IR_039_s7_")
       SENSOR=( "MSG1_RSS"              "MSG2")
     SATELITE=( "METEOSAT8"             "METEOSAT9")
ACQ_HALF_HOUR=( "7.0"                   "3.0")
SUFFIX=".hotspots.n3"
PROCESSING_CHAIN="DynamicThresholds"


# log files
logFile="chain.log"
timings="chain-times.log"
timingsDiscover="discover.txt"

# stSPARQL statements
insertMunicipalities=`cat ${LOC}/insertMunicipalities.sparql` 
deleteSeaHotspots=`cat ${LOC}/deleteSeaHotspots.sparql` 
invalidForFires=`cat ${LOC}/landUseInvalidForFires.sparql`
refinePartialSeaHotspots=`cat ${LOC}/refinePartialSeaHotspots.sparql`
refineTimePersistence=`cat ${LOC}/refineTimePersistence.sparql`
discover=`cat ${LOC}/discover.sparql`
# | sed 's/\"/\\\"/g'`
########################################################################

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
			if test -s /etc/init.d/tomcat6 ; then
				tomcat="tomcat6"
			elif test -s /etc/init.d/tomcat7 ; then
				tomcat="tomcat7"
			fi
	elif test -s /etc/debian_version ; then
			tomcat="tomcat"
	fi

	# check for service availability
	if ! test -e "/etc/init.d/${tomcat}"; then
		tomcat=
	fi
}

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
dropdb -U postgres ${DB}

echo "Creating endpoint database"
createdb -U postgres ${DB} 

# load background data
echo "initializing database"
curl -s  http://dev.strabon.di.uoa.gr/rdf/Kallikratis-Coastline-Corine-dump-postgres-${POSTGRES_MAIN_VERSION}.tgz | tar xz -O | psql -U postgres -d ${DB}
psql ${DB} -U postgres -c 'VACUUM ANALYZE '

echo "starting tomcat"
if test -z "${tomcat}"; then
	# work-around for babis (standalone tomcat, with start_tomcat.sh and stop_tomcat.sh scripts)
	start_tomcat.sh
else
	sudo service ${tomcat} start
fi

# the chain :)
echo "Store Municipalities DeleteInSea InvalidForFires RefineInCoast TimePersistence" > ${timings}
for (( i = 0 ; i < ${#URLDIR[@]} ; i++ )) do
	dir=${URLDIR[$i]}
	prefix=${URLPREFIX[$i]}
	sensor=${SENSOR[$i]}
	satelite=${SATELITE[$i]}
    acquisitions=${ACQ_HALF_HOUR[$i]}
	# get hotpost URLS
	for hot in $(curl -s ${HOTSPOTS_URL}/${dir}/ | grep -o ">${prefix}.*\.n3" | colrm 1 1); do
		echo $hot
		file="${HOTSPOTS_URL}/${dir}/${hot}"
		echo $file

		# get time information for acquisition
		offset=$(( ${#prefix} + 1 ))
		year=$(expr substr ${hot} ${offset} 2)
		month=$(expr substr ${hot} $(( ${offset} + 2 )) 2)
		day=$(expr substr ${hot} $(( ${offset} + 4 )) 2)
		t1=$(expr substr ${hot} $(( ${offset} + 7 )) 2)
		t2=$(expr substr ${hot} $(( ${offset} + 9 )) 2)
		time2="${t1}:${t2}"

		printf "$hot " >> ${timings}

		# store file
		echo -n "storing " $file; echo; echo; 
		# ${countTime} ./strabon -db endpoint store $file

		tmr1=$(timer)
		../endpoint store ${ENDPOINT} N-Triples -u ${file}
		tmr2=$(timer)
		printf '%s ' $((tmr2-tmr1)) >>  ${timings}

		# sudo -u postgres psql -d endpoint -c 'VACUUM ANALYZE;';

		echo;echo;echo;echo "File ${file} stored!" >> ${logFile}

		# insertMunicipalities
		echo -n "inserting Municipalities " ;echo; echo; echo;
		# query=`echo "${insertMunicipalities}" `
		# ${countTime} ./strabon -db endpoint update "${query}"

		tmr1=$(timer)

		query=`echo "${insertMunicipalities}" | sed "s/TIMESTAMP/20${year}-${month}-${day}T${time2}:00/g" | \
		sed "s/PROCESSING_CHAIN/${PROCESSING_CHAIN}/g" | \
		sed "s/SENSOR/${sensor}/g"`

		../endpoint update ${ENDPOINT} "${query}"
		
		tmr2=$(timer)
printf '%s ' $((tmr2-tmr1)) >> ${timings}
		echo;echo;echo;echo "File ${file} inserted Municipalities!"
		
		# deleteSeaHotspots
		echo -n "Going to deleteSeaHotspots 20${year}-${month}-${day}T${time2}:00 " ;echo; echo; echo;
		query=`echo "${deleteSeaHotspots}" | sed "s/TIMESTAMP/20${year}-${month}-${day}T${time2}:00/g" | \
		sed "s/PROCESSING_CHAIN/${PROCESSING_CHAIN}/g" | \
		sed "s/SENSOR/${sensor}/g"`
		# ${countTime} ./strabon -db endpoint update "${query}"

		tmr1=$(timer)
		../endpoint update ${ENDPOINT} "${query}"

		tmr2=$(timer)
		printf '%s ' $((tmr2-tmr1)) >> ${timings}
		echo;echo;echo;echo "File ${file} deleteSeaHotspots done!"

		# echo "Continue?"
		# read a
			# invalidForFires
		echo -n "invalidForFires 20${year}-${month}-${day}T${time2}:00 "  ; echo; echo ; echo;
		query=`echo "${invalidForFires}" | sed "s/TIMESTAMP/20${year}-${month}-${day}T${time2}:00/g" | \
		sed "s/PROCESSING_CHAIN/${PROCESSING_CHAIN}/g" | \
		sed "s/SENSOR/${sensor}/g" |\
		sed "s/SAT/${satelite}/g"`
		# ${countTime} ./strabon -db endpoint update "${query}"
		tmr1=$(timer)
		../endpoint update ${ENDPOINT} "${query}"
		tmr2=$(timer)
		printf '%s ' $((tmr2-tmr1)) >> ${timings}
		echo "File ${file} invalidForFires done!"
 
		# refinePartialSeaHotspots
		echo -n "refinePartialSeaHotspots 20${year}-${month}-${day}T${time2}:00 "  ; echo; echo ; echo;
		query=`echo "${refinePartialSeaHotspots}" | sed "s/TIMESTAMP/20${year}-${month}-${day}T${time2}:00/g" | \
		sed "s/PROCESSING_CHAIN/${PROCESSING_CHAIN}/g" | \
		sed "s/SENSOR/${sensor}/g" |\
		sed "s/SAT/${satelite}/g"`
		# ${countTime} ./strabon -db endpoint update "${query}"
		tmr1=$(timer)
		../endpoint update ${ENDPOINT} "${query}"
		tmr2=$(timer)
		printf '%s ' $((tmr2-tmr1)) >> ${timings}

		echo "File ${file} refinePartialSeaHotspots done!"
		# echo "Continue?"
		# read a

		# refineTimePersistence
		echo -n "Going to refineTimePersistence 20${year}-${month}-${day}T${time2}:00 ";echo;echo;echo; 
		min_acquisition_time=`date --date="20${year}-${month}-${day} ${time2}:00 EEST -30 minutes" +%Y-%m-%dT%H:%m:00`
		query=`echo "${refineTimePersistence}" | sed "s/TIMESTAMP/20${year}-${month}-${day}T${time2}:00/g" | \
		sed "s/PROCESSING_CHAIN/${PROCESSING_CHAIN}/g" | \
		sed "s/SENSOR/${sensor}/g" | \
		sed "s/ACQUISITIONS_IN_HALF_AN_HOUR/${acquisitions}/g" | \
		sed "s/MIN_ACQUISITION_TIME/${min_acquisition_time}/g" |\
		sed "s/SAT/${satelite}/g"`

		#sudo -u postgres psql -d ${DB} -c 'VACUUM ANALYZE;';

		tmr1=$(timer)
		../endpoint update ${ENDPOINT} "${query}"
		 tmr2=$(timer)
		printf '%s \n' $((tmr2-tmr1)) >> ${timings}
		echo;echo;echo;echo "File ${file} timePersistence done!"
		# echo "Continue?"
		# read a


		# discover
		echo -n "Going to discover 20${year}-${month}-${day}T${time2}:00 ";echo;echo;echo; 
		min_acquisition_time=`date --date="20${year}-${month}-${day} 00:00 EEST" +%Y-%m-%dT%H:%m:00`
		max_acquisition_time=`date --date="20${year}-${month}-${day} 23:59 EEST" +%Y-%m-%dT%H:%m:00`
		query=`echo "${discover}" | \
			sed "s/PROCESSING_CHAIN/${PROCESSING_CHAIN}/g" | \
			sed "s/SENSOR/${sensor}/g" | \
			sed "s/MIN_ACQUISITION_TIME/${min_acquisition_time}/g" |\
			sed "s/MAX_ACQUISITION_TIME/${max_acquisition_time}/g"`
			
		tmr1=$(timer)
		../endpoint query ${ENDPOINT} "${query}"
		tmr2=$(timer)
		printf '%s \n' $((tmr2-tmr1)) >> ${timingsDiscover}
		echo;echo;echo;echo "Discovered hotspots done!"
	done
done
