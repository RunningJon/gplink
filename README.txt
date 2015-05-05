README file for GPLink
##################################################################################
Site: http://www.PivotalGuru.com
Author: Jon Roberts
Email: jgronline@gmail.com
##################################################################################
GPLink links JDBC connections to Greenplum and Hawq External Tables.

Data is automatic cleansed for embedded carriage returns, newline, or null
characters.  Escape characters are retained by double escaping and embedded pipes
are retained by escaping. 

##################################################################################
#Installation:
##################################################################################
- gplink must be installed on a server that is accessible by all nodes of Greenplum
or Hawq.  A dedicated ETL server or the standby master are good candidates for 
hosting gplink.

- You will need the Greenplum or Hawq client with Loader utilities installed.

- psql must be configured with PGHOST and PGDATABASE and it is recommended to have
passwordless access configured with a .pgpass file.

1.  Unzip gplink.zip
2.  source gplink_path.sh
3.  Edit gplink.properties with correct Greenplum or Hawq connection information
4.  cd $gplink_home/sql
5.  ./runme.sh
6.  Download 3rd party JDBC drivers and place it in $gplink_home/jar

Demos are available with sample configurations for Oracle and SQL Server in 
$gplink_home/demo and can be run with the runme.sh file found in that directory.

##################################################################################
#Creating External Tables
##################################################################################
1.  Insert configuration into gplink.ext_tables
Example:

INSERT INTO gplink.ext_tables(
table_name, columns, column_datatypes,
sql_text, connection_url,
class_for_name, read_committed, source_user_name, source_password,
extra_properties, host, port)
values
('gplink_demo.ms_sqlserver', array['fname', 'lname'], array['text', 'text'],
'select ''jon'', ''roberts'' union all select ''JON'', ''ROBERTS''', 
'jdbc:sqlserver://jonnywin;CODEPAGE=65001;responseBuffering=adaptive;selectMethod=cursor;',
'com.microsoft.sqlserver.jdbc.SQLServerDriver', true, 'sa', 'sa', null, 'bigmac', 8050);

The id column is a serial which will automatically increment a sequence number.  Use the new ID value for the next step.

2.  Execute function to create External Table
Example:
select gplink.fn_create_ext_table(1);

3.  Start gpfdist process for External Table.
Note: It is HIGHLY recommended to use only 1 gpfdist process for each External Table.

Example:
gplink_start 8050
##################################################################################
#Using Externa Tables
##################################################################################
After you have created the External Table by populating gplink.ext_tables and 
starting the gpfdist process, you can select from the External Table justl like 
any other table.

Example:
SELECT * from gplink_demo.ms_sqlserver;

##################################################################################
#Dropping External Tables
##################################################################################
1.  Use the port number from gplink.ext_tables to stop the gpfdist process.
Note: you can use ps -ef | grep gpfdist to find all of the gpfdist processes 
currently running.

Example:
gplink_stop 8050

2.  Delete the row from gplink.ext_tables (Greenplum) or truncate the table 
(Hawq).

Example:
DELETE FROM gplink.ext_tables where id = 1;
