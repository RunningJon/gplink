README file for GPLink
########################################################################################
Site: http://www.PivotalGuru.com
Author: Jon Roberts
Email: jgronline@gmail.com
########################################################################################
GPLink links JDBC connections to Greenplum and Hawq External Tables.

Data is automatically cleansed for embedded carriage returns, newline, and/or null
characters.  Escape characters are retained by double escaping and embedded pipes
are retained by escaping. 

########################################################################################
#Installation:
########################################################################################
- gplink must be installed on a server that is accessible by all nodes of Greenplum
or Hawq.  A dedicated ETL server or the standby master are good candidates for 
hosting gplink.

1.  Download latest version from PivotalGuru.com
2.  Unzip <version>.zip
3.  source gplink_path.sh and add this to your .bashrc file
4.  Edit gplink.properties with correct Greenplum or Hawq connection information
5.  Download 3rd party JDBC drivers and place it in $GPLINK_HOME/jar
6.  Define source configurations in $GPLINK_HOME/connections/ 
7.  Define external table names and columns in $GPLINK_HOME/tables/
8.  Define SQL statements to execute in the source in $GPLINK_HOME/sql/
9.  Create the External Table with gpltable

########################################################################################
#Creating External Tables
########################################################################################
gpltable -s <source_config> -t <target_config> -f <sql> -a <source_table>
example:
gpltable -s sqlserver.properties -t $GPLINK_HOME/gplink.properties -f example.sql -a $GPLINK_HOME/tables/public.test.sql

########################################################################################
#Dropping External Tables
########################################################################################
gpldrop -t <target_config> -n <table_name>
example:
gpldrop -t $GPLINK_HOME/gplink.properties -n public.test

########################################################################################
#Start the gpfdist processes
########################################################################################
gplstart -t <target_config>
example:
gplstart -t $GPLINK_HOME/gplink.properties

Note: this is useful when the host is restarted and you need to start all of the gpfdist
processes needed by gplink External Tables.

########################################################################################
#Debugging
########################################################################################
export GPLINK_DEBUG=true

Turn off debugging:
export GPLINK_DEBUG=

Note: this will show all debug messages from gplstart, gpltable, and gpldrop.
