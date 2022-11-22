#!/bin/bash

#
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.
#
# Copyright (C) 2010, 2011, 2012, Pyravlos Team
#
# http://www.strabon.di.uoa.gr/
#

#
# Script for instantiating NOA refinement queries.
#
# Author: Charalampos (Babis) Nikolaou <charnik@di.uoa.gr>
#

# command name
CMD="$(basename ${0})"

# absolute directory name of this command
LOC="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

function help() {
	echo "Usage: ${CMD} [OPTIONS] QUERY_FILE..."
	echo
	echo "Script for instantiating NOA refinement queries."
	echo
	echo "OPTIONS can be any of the following (variable names and values are case sensitive)"
	echo "	-s, --sensor		: the sensor to use, e.g., \`MSG1' or \`MSG2'"
	echo "				  (determines the value for satellite, template variable \`SAT'"
	echo "				   and number of acquisitions in an hour, template variable"
	echo "				   \`ACQUISITIONS_IN_HALF_AN_HOUR')"
	echo "	-c, --chain		: the processing chain to use, e.g., \`DynamicThresholds'"
	echo "	-t, --timestamp		: the timestamp to use, e.g., \`2010-08-21T19:50:00'"
	echo "	-m, --min_acq_time	: the minimum acquisition time (used in a persistence query only)"
	echo "	-M, --max_acq_time	: the maximum acquisition time (used in a discovery query only)"
	echo "	-p, --persistence	: "
	echo "	-r, --repeat_in_persistence	: "
	echo
	echo "Example run:"
	echo "	./instantiate.sh -s MSG1 -t '2010-08-21T19:50:00' -c "DynamicThresholds" -m '2010-08-21T19:50:00' -M '2010-08-21T19:50:00' *.rq"
}

SENSOR=
CHAIN=
SAT=
N_ACQUISITIONS=
N_ACQUISITIONS_PER_HOUR=
TIMESTAMP=
MIN_ACQ_TIME=
MAX_ACQ_TIME=
PERSISTENCE=
REPEAT_IN_PERS= 

if test $# -eq 0; then
	help
	exit 1
fi

# read script options
while test $# -gt 0 -a "X${1:0:1}" == "X-"; do
	case "${1}" in
		-h|--help)
			help
			exit 0
			;;
		-s|--sensor)
			shift
			SENSOR="${1}"

			# determine satellite and number of acquisitions per hour
			if test "${SENSOR}" = "MSG2"; then
				SAT="METEOSAT9"
				N_ACQUISITIONS=3.0
				N_ACQUISITIONS_PER_HOUR=5.0
			else
				SAT="METEOSAT8"
				N_ACQUISITIONS=7.0
				N_ACQUISITIONS_PER_HOUR=13.0 
				
				# change MSG1 to MSG1_RSS (for whatever reason NOA uses it :-))
				SENSOR="MSG1_RSS"
			fi
			shift
			;;
		-c|--chain)
			shift
			CHAIN="${1}"
			shift
			;;
		-t|--timestamp)
			shift
			TIMESTAMP="${1}"
			shift
			;;
		-m|--min_acq_time)
			shift
			MIN_ACQ_TIME="${1}"
			shift
			;;
		-M|--max_acq_time)
			shift
			MAX_ACQ_TIME="${1}"
			shift
			;;
		-p|--persistence)
			shift
			PERSISTENCE="${1}"
			shift
			;;
		-r|--repeat_in_persistence)
			shift
			REPEAT_IN_PERS="${1}"
			shift
			;;
		-*)
			echo "${CMD}: unknown option \"${1}\""
			help
			exit 1
			;;
	esac
done

# build sed expression
ARGS=

if test ! -z "${CHAIN}"; then
	ARGS="${ARGS} -e 's/PROCESSING_CHAIN/${CHAIN}/g'"
fi

if test ! -z "${SENSOR}"; then
	ARGS="${ARGS} -e 's/SENSOR/${SENSOR}/g'"
fi

if test ! -z "${SAT}"; then
	ARGS="${ARGS} -e 's/SAT/${SAT}/g'"
fi

if test ! -z "${N_ACQUISITIONS}"; then
	ARGS="${ARGS} -e 's/ACQUISITIONS_IN_HALF_AN_HOUR/${N_ACQUISITIONS}/g'"
fi

if test ! -z "${N_ACQUISITIONS_PER_HOUR}"; then
	ARGS="${ARGS} -e 's/ACQUISITIONS_IN_AN_HOUR/${N_ACQUISITIONS_PER_HOUR}/g'"
fi

if test ! -z "${TIMESTAMP}"; then
	ARGS="${ARGS} -e 's/TIMESTAMP/${TIMESTAMP}/g'"
fi

if test ! -z "${MIN_ACQ_TIME}"; then
	ARGS="${ARGS} -e 's/MIN_ACQUISITION_TIME/${MIN_ACQ_TIME}/g'"
fi

if test ! -z "${MAX_ACQ_TIME}"; then
	ARGS="${ARGS} -e 's/MAX_ACQUISITION_TIME/${MAX_ACQ_TIME}/g'"
fi

if test ! -z "${PERSISTENCE}"; then
	ARGS="${ARGS} -e 's/PERSISTENCE/${PERSISTENCE}/g'"
fi

if test ! -z "${REPEAT_IN_PERS}"; then
	ARGS="${ARGS} -e 's/REPEAT_IN_PERS/${REPEAT_IN_PERS}/g'"
fi

if test -z "${ARGS}"; then
	echo "${CMD}: You would be so kind to provide at least one OPTION."
	help
	exit 2
fi

QUERY="`eval sed ${ARGS} ${@}`"
#echo "$QUERY"
#echo eval sed ${ARGS} ${@}

# check for unbounded variables
GREP_RESULT=`echo "${QUERY}" | egrep -o 'PROCESSING_CHAIN|SENSOR|"SAT"|ACQUISITIONS_IN_HALF_AN_HOUR|TIMESTAMP|MIN_ACQUISITION_TIME|MAX_ACQUISITION_TIME|PERSISTENCE|REPEAT_IN_PERS'`

if ! test $? -eq 0; then
	echo "${QUERY}"
else
	echo -e "${CMD}: WARNING: found unbounded variables "$(echo "${GREP_RESULT}"|sort -u)""
	echo
	help
fi
