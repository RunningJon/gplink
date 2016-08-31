#!/bin/bash
GPLINK_HOME=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
export GPLINK_HOME

export GPLINK_YML=$GPLINK_HOME/yml/gplink.yml 

export GPLINK_LOG=$GPLINK_HOME/log/gplink

export PATH=$GPLINK_HOME/bin:$PATH

export GPLINK_LOCK=/tmp/gplink.lock

export GPLINK_PORT_LOWER=24000
export GPLINK_PORT_UPPER=25000

GPLINK_LOCAL_PWD=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

GPLINK_CLASS_PATH=$GPLINK_LOCAL_PWD/jar/PivotalGuruGPLink.jar
GPLINK_CLASS_PATH="$GPLINK_CLASS_PATH:""$GPLINK_LOCAL_PWD/jar/log4j.properties"
for j in $(ls $GPLINK_LOCAL_PWD/jar/*.jar | grep -v PivotalGuruGPLink.jar); do
        GPLINK_CLASS_PATH="$GPLINK_CLASS_PATH:"$j
done
export GPLINK_CLASS_PATH

GPLINK_LOG4J_PROPERTIES=$GPLINK_LOCAL_PWD/jar/log4j.properties
export GPLINK_LOG4J_PROPERTIES
