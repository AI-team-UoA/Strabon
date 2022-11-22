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


dataDir="/home/ggarbis/TELEIOS/NOA_Processing_Chain/chain_msg2/data/out_triples/"
dataUrl="http://pathway.di.uoa.gr/hotspots/out_triples/"
name="HMSG2_IR_039_s7_070825"
suffix=".hotspots.n3"

logFile="chain.log"
countTime="/usr/bin/time -ao ${logFile} -f %e"
echo > ${logFile}

deleteSeaHotspots="PREFIX noa: <http://teleios.di.uoa.gr/ontologies/noaOntology.owl#> 
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
PREFIX strdf: <http://strdf.di.uoa.gr/ontology#> 
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> 

DELETE {?h ?property ?object} 
WHERE {
  ?h rdf:type noa:Hotspot; 
     noa:hasAcquisitionTime \"TIMESTAMP\"^^xsd:dateTime; 
     noa:producedFromProcessingChain \"PROCESSING_CHAIN\"^^xsd:string; 
     noa:isDerivedFromSensor \"SENSOR\"^^xsd:string;
     noa:hasGeometry ?hGeo;
     ?property ?object . 
  OPTIONAL {
    ?c rdf:type noa:Coastline;
       noa:hasGeometry ?cGeo . 
    FILTER(strdf:mbbIntersects(?hGeo, ?cGeo)) .
  } 
  FILTER(!bound(?c)) . 
}"

refinePartialSeaHotspots="PREFIX noa: <http://teleios.di.uoa.gr/ontologies/noaOntology.owl#> 
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
PREFIX strdf: <http://strdf.di.uoa.gr/ontology#> 
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> 

DELETE { ?h noa:hasGeometry ?hGeo} 
INSERT { ?h noa:hasGeometry ?dif} 
WHERE { 
  SELECT DISTINCT ?h ?hGeo (strdf:intersection(?hGeo, strdf:union(?cGeo)) AS ?dif) 
  WHERE { 
    ?h rdf:type noa:Hotspot; 
       noa:hasAcquisitionTime ?hAcqTime; 
       noa:producedFromProcessingChain \"PROCESSING_CHAIN\"^^xsd:string; 
       noa:isDerivedFromSensor \"SENSOR\"^^xsd:string;
       noa:hasGeometry ?hGeo .
    FILTER(\"TIMESTAMP\"^^xsd:dateTime = ?hAcqTime) .
    ?c rdf:type noa:Coastline;
       noa:hasGeometry ?cGeo .
    FILTER(strdf:mbbIntersects(?hGeo, ?cGeo)) . 
  }
  GROUP BY ?h ?hGeo 
  HAVING strdf:overlap(?hGeo, strdf:union(?cGeo))
}"

refineTimePersistence="PREFIX noa: <http://teleios.di.uoa.gr/ontologies/noaOntology.owl#> 
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
PREFIX strdf: <http://strdf.di.uoa.gr/ontology#> 
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> 

INSERT {
  ?newHotspot rdf:type noa:Hotspot .
  ?newHotspot noa:hasConfidence ?hConfidence .
  ?newHotspot noa:hasGeometry ?hGeometry1 .
  ?newHotspot noa:hasAcquisitionTime \"TIMESTAMP\"^^xsd:dateTime .
  ?newHotspot noa:isDerivedFromSensor \"SENSOR\"^^xsd:string .
  ?newHotspot noa:hasConfirmation noa:unknown .
  ?newHotspot noa:producedFromProcessingChain ?hProcessingChain .
  ?newHotspot noa:isProducedBy noa:noa .
}
WHERE {
  SELECT (BNODE() AS ?newHotspot)
  (SUM(?hConfidence1)/ACQUISITIONS_IN_HALF_AN_HOUR AS ?hConfidence)
  ?hGeometry1
  (CONCAT(\"PROCESSING_CHAIN\"^^xsd:string,\"-TimePersistence\") AS ?hProcessingChain)
  WHERE {
    ?H1 rdf:type noa:Hotspot .
    ?H1 noa:hasConfidence ?hConfidence1 .
    ?H1 noa:hasGeometry ?hGeometry1 .
    ?H1 noa:hasAcquisitionTime ?hAcquisitionTime1 .
    ?H1 noa:isDerivedFromSensor \"SENSOR\"^^xsd:string .
    ?H1 noa:hasConfirmation noa:unknown .
    ?H1 noa:producedFromProcessingChain \"PROCESSING_CHAIN\"^^xsd:string .
    ?H1 noa:isProducedBy noa:noa .
    FILTER( \"MIN_ACQUISITION_TIME\"^^xsd:dateTime <= ?hAcquisitionTime1 && ?hAcquisitionTime1 < \"TIMESTAMP\"^^xsd:dateTime ) .
    OPTIONAL {
      ?H2 rdf:type noa:Hotspot .
      ?H2 noa:hasGeometry ?HGEO2 .
      ?H2 noa:hasAcquisitionTime ?hAcquisitionTime2 .
      ?H2 noa:isDerivedFromSensor \"SENSOR\"^^xsd:string .
      ?H2 noa:producedFromProcessingChain ?hProcessingChain2 .
      FILTER(regex(\"PROCESSING_CHAIN\"^^xsd:string, ?hProcessingChain2)).
      FILTER( strdf:equals(?hGeometry1, ?HGEO2) ) .
      FILTER(?hAcquisitionTime2 = \"TIMESTAMP\"^^xsd:dateTime) .
    }
    FILTER( !BOUND(?H2) ) .
  }
  GROUP BY ?hGeometry1
  HAVING(SUM(?hConfidence1)>0.0)
}
"

# Initialize
sudo service postgresql restart
dropdb endpoint
createdb endpoint -T template_postgis
sudo service tomcat6 restart
./endpoint store http://localhost:8080/endpoint N-Triples -u http://pathway.di.uoa.gr/hotspots/grid_4.nt

#./scripts/endpoint query http://localhost:8080/endpoint "SELECT (COUNT(*) AS ?C) WHERE {?s ?p ?o}"
#echo "Continue?"
#read a



for h in `seq 0 12`; do
    for m in `seq 0 15 45`; do
            time=`printf "%02d%02d\n" $h $m`
            time2=`printf "%02d:%02d\n" $h $m`
#            file=${dataDir}${name}_${time}$suffix
            file=${dataUrl}${name}_${time}$suffix
    
            # store file
            echo -n "storing 2007-08-25T${time2}:00 " >> ${logFile}
#            ${countTime} ./strabon -db endpoint store $file      
            ${countTime} ./endpoint store http://localhost:8080/endpoint N-Triples -u ${file}
            
#            echo "File ${file} stored!" >> ${logFile}
#            echo "Continue?"
#            read a
         
            # deleteSeaHotspots
            echo -n "deleteSeaHotspots 2007-08-25T${time2}:00 " >> ${logFile}
            query=`echo "${deleteSeaHotspots}" | sed "s/TIMESTAMP/2007-08-25T${time2}:00/g" | \
                sed "s/PROCESSING_CHAIN/DynamicThresholds/g" | \
                sed "s/SENSOR/MSG2/g"`
#            ${countTime} ./strabon -db endpoint update "${query}"
            ${countTime} ./endpoint update http://localhost:8080/endpoint "${query}"
            
#            echo "File ${file} deleteSeaHotspots done!"
#            echo "Continue?"
#            read a
            
            # refinePartialSeaHotspots
            echo -n "refinePartialSeaHotspots 2007-08-25T${time2}:00 " >> ${logFile}
            query=`echo "${refinePartialSeaHotspots}" | sed "s/TIMESTAMP/2007-08-25T${time2}:00/g" | \
                sed "s/PROCESSING_CHAIN/DynamicThresholds/g" | \
                sed "s/SENSOR/MSG2/g"`
#            ${countTime} ./strabon -db endpoint update "${query}"
            ${countTime} ./endpoint update http://localhost:8080/endpoint "${query}"
            
#            echo "File ${file} refinePartialSeaHotspots done!"
#            echo "Continue?"
#            read a

            # refineTimePersistence
            echo -n "refineTimePersistence 2007-08-25T${time2}:00 " >> ${logFile}
            min_acquisition_time=`date --date="2007-08-25 ${time2}:00 EEST -30 minutes" +%Y-%m-%dT%H:%m:00`
            query=`echo "${refineTimePersistence}" | sed "s/TIMESTAMP/2007-08-25T${time2}:00/g" | \
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

