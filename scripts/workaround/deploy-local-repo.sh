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
# Script for deploying a local maven repository to a remote one
#
# Author: Kostis Kyzirakos <kk@di.uoa.gr>
#


# server id that maps on the <id> under <server> section of ~/.m2/settings.xml.
# In most cases, this parameter will be required for authentication.
DEPLOY_REPO_ID="strabon"

# the url of the remote repository
DEPLOY_REPO_URL="http://maven.strabon.di.uoa.gr/content/repositories/strabon.sesame/"

# temporary folder
TEMP_DIR="/tmp/deploy-local-repo-$$"

# command name
CMD="$(basename ${0})"

function help() {
        echo "Usage: ${CMD} [OPTIONS] [DIR]"
        echo
        echo "Deploy a local maven repository to a remote one"
        echo
        echo "  DIR     : resume the deployment of the local repository, starting from this directory"
        echo
        echo "OPTIONS can be any of the following (variable names and values are case sensitive)"
        echo "  --help           : Print this menu"
		echo ""
}

if [[ ${#} -gt "0" ]] ; then
	ARRAY=(${@})
	ELEMENTS=${#ARRAY[@]}
	for (( i = 0; i < ${ELEMENTS}; i++ )); do
		if [[ "${ARRAY[${i}]}" = "--help" ]] || [[ "${ARRAY[${i}]}" = "-help" ]] || [[ "${ARRAY[${i}]}" = "help" ]]; then
			help
			exit 0
		fi
	done
fi

mkdir ${TEMP_DIR}
if [[ ! -d "${TEMP_DIR}" ]] ; then
	echo "Could not create temporary directory."
	echo "Aborting..."
	exit
fi

found=false;
for d in `find ${HOME}/.m2/repository -type d|sort` ;
do
	if [[ ! -z "$1" ]] && [[ "${found}" = "false" ]] && [[ "$d" != "$1" ]] ; then
		echo "Skipping ${d}"
		continue;
	fi
	# resuming
	found=true;

	#for each directory
	cd ${d}
	children=`find . -type d|grep -v '^.$'|wc -l`
	if [[ "${children}" -ne "0" ]] ; then
		# if the directory has more subdirectories, move one
		continue;
	fi

	countPoms=`ls -1 *.pom 2>/dev/null|wc -l`
	countJars=`ls -1 *.jar 2>/dev/null|wc -l`

	if [[ "${countPoms}" -gt "1" ]] && [[ "${countJars}" -gt "1" ]] ; then
		echo "Found ${countPoms} poms and ${countJars} jars in directory '${d}'."
		echo "Aborting..."
		exit;
	elif [[ "${countPoms}" -eq "0" ]] ; then
		echo "No .pom file found in directory '${d}' (${children} children)."
		echo "Aborting..."
		exit;
	fi

	if [[ "${countPoms}" -eq "1" ]] && [[ "${countJars}" -eq "1" ]] ; then
		pomFile=`ls -1 *.pom 2>/dev/null`
		jarFile=`ls -1 *.jar 2>/dev/null`
		cp ${pomFile} ${TEMP_DIR}/${pomFile} 2>/dev/null
		cp ${jarFile} ${TEMP_DIR}/${jarFile} 2>/dev/null
		# deploy the local jar file to the remote repo
		mvn deploy:deploy-file \
			-DrepositoryId=${DEPLOY_REPO_ID} \
			-Durl=${DEPLOY_REPO_URL} \
			-DpomFile=${TEMP_DIR}/${pomFile} \
			-Dfile=${TEMP_DIR}/${jarFile};
		if [[ "$?" -ne "0" ]] ; then echo "Error occured while processing directory '${d}' (temp dir is '${TEMP_DIR}')"; exit; fi
	elif [[ "${countPoms}" -eq "1" ]] && [[ "${countJars}" -eq "0" ]] ; then
		pomFile=`ls -1 *.pom 2>/dev/null`
		cp ${pomFile} ${TEMP_DIR}/${pomFile} 2>/dev/null
		# deploy the local pom file to the remote repo
		mvn deploy:deploy-file \
			-DrepositoryId=${DEPLOY_REPO_ID} \
			-Durl=${DEPLOY_REPO_URL} \
			-DpomFile=${TEMP_DIR}/${pomFile} \
			-Dfile=${TEMP_DIR}/${pomFile};
		if [[ "$?" -ne "0" ]] ; then echo "Error occured while processing directory '${d}' (temp dir is '${TEMP_DIR}'"; exit; fi
	elif [[ "${countPoms}" -gt "1" ]] && [[ "${countJars}" -eq "0" ]] ; then
		# deploy the local pom files to the remote repo
		for pom in `ls -1 *.pom` ; do
			pomFile=${pom};
			cp ${pomFile} ${TEMP_DIR}/${pomFile} 2>/dev/null;
			mvn deploy:deploy-file \
				-DrepositoryId=${DEPLOY_REPO_ID} \
				-Durl=${DEPLOY_REPO_URL} \
				-DpomFile=${TEMP_DIR}/${pomFile} \
				-Dfile=${TEMP_DIR}/${pomFile};
			if [[ "$?" -ne "0" ]] ; then echo "Error occured while processing directory '${d}' (temp dir is '${TEMP_DIR}'"; exit; fi
		done
	else
		echo "Found ${countPoms} poms and ${countJars} jars in directory '${d}' (temp dir is '${TEMP_DIR}')."
		echo "What should I do?"
		echo "Aborting..."
		exit;
	fi

	# grooming
	rm ${TEMP_DIR}/*
done


# grooming
rm -rf ${TEMP_DIR}
