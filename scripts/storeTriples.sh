#! /bin/bash

echo > log.out

for f in `ls /home/ggarbis/out_triples/HMSG2_IR_039_s7_070825_*n3 | sort`; do
	echo "Storing: $f" >> log.out
	echo "Executing: ./endpoint store http://pathway.di.uoa.gr:8080/strabonTest/ N3 -u file://$f" >> log.out
	./endpoint store http://localhost:8080/strabonTest/ N3 -u file://$f  >> log.out
done

