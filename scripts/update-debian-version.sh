#!/bin/bash

#
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.
#
# Copyright (C) 2010, 2011, 2012, 2013, 2014, 2015 Pyravlos Team
#
# http://www.strabon.di.uoa.gr/
#

#
# Script for updating the version field in the control file that is needed
# by the jdeb plugin when generating the Strabon .deb file. It is run by maven
# when the jdeb goal is invoked. This is done mainly during the package phase.
# From command-line, it is done simply by executing command
# `mvn -Ddebian=true package'.
#
# It should be run without arguments from inside the `endpoint-exec' or
# `runtime' directories. See also the respective `pom.xml' files in these
# directories.
#
# Author: Charalampos (Babis) Nikolaou <charnik@di.uoa.gr>
#

VERSION=`grep version pom.xml | head -1 | sed 's/\(.*\)<version>\(.*\)<\/version>/\2/'`
INPUT_FILE=./src/deb/control/control

BAK=

# in Mac OS X, sed expects a suffix for the bak file (when done in place)
if test `uname` = "Darwin"; then
	BAK='.bak'
fi

sed -i ${BAK} "s/^Version.*$/Version: ${VERSION}/" "${INPUT_FILE}"

# remove the backup file
if ! test -z "${BAK}"; then
	rm -f "${INPUT_FILE}${BAK}"
fi
