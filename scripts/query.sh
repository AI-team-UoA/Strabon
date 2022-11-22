#!/bin/bash
#BASE=/home/strabon/experiments/data/naive/10mil
#10mil  0.01     11552771
#100mil 0.0034   99758140
#500mil 0.0015  512505143
#1bil   0.001  1153249211

DB="noa-d4_1"
LOGPATH="/home/strabon/experiments/logs"
STEP="0.01"
TOTALTRIPLES="11552771"

(cd jars/target &&
java -cp $(for file in `ls -1 *.jar`; do myVar=$myVar./$file":"; done; echo $myVar;) eu.earthobservatory.runtime.monetdb.QueryOp localhost 50000 $DB monetdb monetdb 
"PREFIX noa: <http://teleios.di.uoa.gr/ontologies/noaOntology.owl#> PREFIX clc: <http://teleios.di.uoa.gr/ontologies/clcOntology.owl#> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> PREFIX strdf: <http://strdf.di.uoa.gr/ontology#> PREFIX teleios: <http://teleios.di.uoa.gr/ontologies/noaOntology.owl#> PREFIX gag: <http://www.semanticweb.org/ontologies/2011/gagKallikratis.rdf#> PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#> PREFIX georss: <http://www.georss.org/georss/> PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> PREFIX lgdo: <http://linkedgeodata.org/ontology/> PREFIX gn: <http://www.geonames.org/ontology#> PREFIX iman: <http://teleios.di.uoa.gr/ontologies/imageAnnotationOntology.owl#> PREFIX eolo: <http://www.dlr.de/ontologies/EOLO.owl#> SELECT ?h ?hGeo ?hAcqTime ?hConfidence ?hProvider ?hConfirmation ?hSensor WHERE { ?h a noa:Hotspot ;    noa:hasGeometry ?hGeo ;    noa:hasAcquisitionTime ?hAcqTime ;    noa:hasConfidence ?hConfidence ;    noa:isProducedBy ?hProvider ;    noa:hasConfirmation ?hConfirmation ;    noa:isDerivedFromSensor ?hSensor ;    FILTER( \"2007-08-23T00:00:00\" <= str(?hAcqTime)    && str(?hAcqTime) <= \"2007-08-26T23:59:59\" ) .    FILTER( strdf:contains(\"POLYGON((21.027 38.36, 23.77 38.36, 23.77 36.05, 21.027 36.05, 21.027 38.36))\"^^strdf:WKT, ?hGeo) ) .}" 
XML)

