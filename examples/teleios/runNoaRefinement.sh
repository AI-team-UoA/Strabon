#! /bin/bash

#
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.
#
# Copyright (C) 2010, 2011, 2012, Pyravlos Team
#
# http://www.strabon.di.uoa.gr/
#


dataDir="/var/www/shapefiles2012all"
dataUrl="http://challenge.strabon.di.uoa.gr/shapefiles2012all/"
name="HMSG2_IR_039_s7"
suffix=".n3"

logFile="chain.log"
countTime="/usr/bin/time -ao ${logFile} -f %e"
echo > ${logFile}

insertMunicipalities="`cat insertMunicipalities.rq`"
deleteSeaHotspots="`cat deleteSeaHotspots.rq`"
landUseInvalidForFires="`cat landUseInvalidForFires.rq`"
refinePartialSeaHotspots="`cat refinePartialSeaHotspots.rq`"
refineTimePersistence="`cat refineTimePersistence.rq`"

# Initialize
#sudo service postgresql restart
#dropdb endpoint
#createdb endpoint -T template_postgis
#sudo service tomcat6 restart
#./endpoint store http://localhost:8080/endpoint N-Triples -u http://pathway.di.uoa.gr/hotspots/grid_4.nt

#./scripts/endpoint query http://localhost:8080/endpoint "SELECT (COUNT(*) AS ?C) WHERE {?s ?p ?o}"
#echo "Continue?"
#read a


for Month in `sec 7 8`; do
	for d in `sec 1 31`; do
		for h in `seq 0 12`; do
			for m in `seq 0 15 45`; do
            			time=`printf "%02d%02d\n" $h $m`
		        	time2=`printf "%02d:%02d\n" $h $m`
				date=`printf "%02d-%02d\n" $Month $d`
##            file=${dataDir}${name}_${time}$suffix
#            file=${dataUrl}${name}_${time}$suffix
#    
#            # store file
#            echo -n "storing 2007-08-25T${time2}:00 " >> ${logFile}
##            ${countTime} ./strabon -db endpoint store $file      
#            ${countTime} ./endpoint store http://localhost:8080/endpoint N-Triples -u ${file}
#            
##            echo "File ${file} stored!" >> ${logFile}
##            echo "Continue?"
##            read a
         
            # deleteSeaHotspots
            echo -n "deleteSeaHotspots 2007-${date}T${time2}:00 " >> ${logFile}
            query=`echo "${deleteSeaHotspots}" | sed "s/TIMESTAMP/2007-${date}T${time2}:00/g" | \
                sed "s/PROCESSING_CHAIN/DynamicThresholds/g" | \
                sed "s/SENSOR/MSG2/g"`
#            ${countTime} ./strabon -db endpoint update "${query}"
            ${countTime} ./endpoint update http://localhost:8080/endpoint "${query}"
            
#            echo "File ${file} deleteSeaHotspots done!"
#            echo "Continue?"
#            read a
            
            # refinePartialSeaHotspots
            echo -n "refinePartialSeaHotspots 2007-${date}T${time2}:00 " >> ${logFile}
            query=`echo "${refinePartialSeaHotspots}" | sed "s/TIMESTAMP/2007-${date}T${time2}:00/g" | \
                sed "s/PROCESSING_CHAIN/DynamicThresholds/g" | \
                sed "s/SENSOR/MSG2/g"`
#            ${countTime} ./strabon -db endpoint update "${query}"
            ${countTime} ./endpoint update http://localhost:8080/endpoint "${query}"
            
#            echo "File ${file} refinePartialSeaHotspots done!"
#            echo "Continue?"
#            read a

            # refineTimePersistence
            echo -n "refineTimePersistence 2007-${date}T${time2}:00 " >> ${logFile}
            min_acquisition_time=`date --date="2007-${date} ${time2}:00 EEST -30 minutes" +%Y-%m-%dT%H:%m:00`
            query=`echo "${refineTimePersistence}" | sed "s/TIMESTAMP/2007-${date}T${time2}:00/g" | \
                sed "s/PROCESSING_CHAIN/DynamicThresholds/g" | \
                sed "s/SENSOR/MSG2/g" | \
                sed "s/ACQUISITIONS_IN_HALF_AN_HOUR/3.0/g" | \
                sed "s/MIN_ACQUISITION_TIME/${min_acquisition_time}/g"`

#            echo "Query:"
#            echo "${query}"
#            echo "Continue?"
#            read a
#            ${countTime} ./strabon -db endpoint update "${query}"
#            ${countTime} ./endpoint update http://localhost:8080/endpoint "${query}"
            ${countTime} ./endpoint update http://localhost:8080/endpoint "${query}"
            
#            echo "File ${file} refinePartialSeaHotspots done!"
#            echo "Continue?"
#            read a
    done
done


#for f in `ls /home/ggarbis/TELEIOS/NOA_Processing_Chain/chain_msg2/data/out_triples/HMSG2_IR_039_s7_070825_*.hotspots.n3`
#do

#    echo "Store $f"
#	${countTime} ./scripts/strabon -db endpoint store $f
#	
#	
#done

