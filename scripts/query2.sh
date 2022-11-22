#!/bin/bash
#BASE=/home/strabon/experiments/data/naive/10mil
#10mil  0.01     11552771
#100mil 0.0034   99758140
#500mil 0.0015  512505143
#1bil   0.001  1153249211

DB="test"

(cd jars/target &&
java -cp $(for file in `ls -1 *.jar`; do myVar=$myVar./$file":"; done; echo $myVar;) eu.earthobservatory.runtime.monetdb.QueryOp localhost 50000 $DB monetdb monetdb "SELECT * WHERE {?s ?p ?o} LIMIT 1" XML)

