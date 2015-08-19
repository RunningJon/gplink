#!/bin/bash
gplink_home=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
export gplink_home

gplink_yml=$gplink_home/yml/gplink.yml 
export gplink_yml

gplink_log=$gplink_home/log/gplink
export gplink_log

export PATH=$gplink_home/bin:$PATH
