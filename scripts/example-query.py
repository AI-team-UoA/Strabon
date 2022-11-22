#!/usr/bin/env python

import sys, re, os
import os
import glob
import time
import shutil
import httplib, urllib

def main(argv):

    query = 'PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX strdf: <http://strdf.di.uoa.gr/ontology#> PREFIX noa: <http://teleios.di.uoa.gr/ontologies/noaOntology.owl#> SELECT ?H (strdf:transform(?HGEO, <http://www.opengis.net/def/crs/EPSG/0/4326>) AS ?GEO) WHERE { ?H rdf:type noa:Hotspot . ?H noa:hasAcquisitionTime ?HAT . FILTER(str(?HAT) = "2010-08-21T21:20:00") . ?H noa:isDerivedFromSensor ?HS . FILTER(str(?HS) = "MSG1_RSS" ) . ?H noa:hasGeometry ?HGEO . }'
    params = urllib.urlencode({'SPARQLQuery': query, 'format': "XML"})
    headers = {"Content-type": "application/x-www-form-urlencoded", "Accept": "text/xml"}

    #conn = httplib.HTTPConnection("papos.space.noa.gr:8080")
    #conn.request("POST", "/endpoint/Query", params, headers)

    conn = httplib.HTTPConnection("test.strabon.di.uoa.gr")
    conn.request("POST", "/NOA/Query", params, headers)

    response = conn.getresponse()
    print response.status, response.reason
    print response.msg
    print response.read()
    return 0

if __name__ == "__main__":
    sys.exit(main(sys.argv))
