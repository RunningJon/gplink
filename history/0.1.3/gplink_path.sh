#!/bin/bash
GPLINK_HOME=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
export GPLINK_HOME

export GPLINK_YML=$GPLINK_HOME/yml/gplink.yml 

export GPLINK_LOG=$GPLINK_HOME/log/gplink

export PATH=$GPLINK_HOME/bin:$PATH

export GPLINK_LOCK=/tmp/gplink.lock

export GPLINK_PORT_LOWER=24000
export GPLINK_PORT_UPPER=25000
