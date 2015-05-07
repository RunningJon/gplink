README_CONFIG file for GPLink
##################################################################################
Site: http://www.PivotalGuru.com
Author: Jon Roberts
Email: jgronline@gmail.com
##################################################################################
Recommendation connection configuration by database.

##################################################################################
#Oracle
##################################################################################
- connection_url=jdbc:oracle:thin:@<host>:<port>/<instance_name>
The thin driver will give you the best performance for unloading data.

- class_for_name=oracle.jdbc.driver.OracleDriver

- read_committed=true
Oracle uses rollback segments to provide read consistent view of the data and 
although you can set this value to false, Oracle won't do a dirty read.  

- extra_properties=defaultRowPrefetch=2000
This is how many rows of data will be retrieved on each fetch.  The higher the 
number, the better the performance but it will use more memory.  Continously
increasing this will also give you diminishing returns.  Try 2000 to start with.

##################################################################################
#MS SQL Server
##################################################################################
- connection_url=jdbc:sqlserver://<host>;CODEPAGE=65001;responseBuffering=adaptive;selectMethod=cursor;
Change <host> to the SQL Server name.

If you are using a named instance, make the connection_url value look like this:
- connection_url=jdbc:sqlserver://<host>;instanceName=<instance_name>;CODEPAGE=65001;responseBuffering=adaptive;selectMethod=cursor;

CODEPAGE=65001 means the connection will convert the server character set 
(Microsoft calls this a codepage) to UTF-8.  Hawq and Greenplum are set in UTF-8
already.  This is a good way to make sure data is transferred correctly by 
leveraging the JDBC driver to convert the data to UTF-8.

responseBuffering=adaptive;
This is a feature in the MS driver which reduces the memory needed to query
data and greatly enhances the performance of dumping large amounts of data.  Be
sure to include this in your connection_url.

selectMethod=cursor;
This also reduces the memory footprint needed to dump data.

- class_for_name=com.microsoft.sqlserver.jdbc.SQLServerDriver

- read_committed=true
Note: By default, Microsoft provides read consistency by locking data.  This means
a reader will block a writer and a writer will block a reader.  A long running 
query will prevent other sessions from inserting, updating, or deleting data.  To 
avoid this, you can either use a dirty read, read_committed=false, or set the SQL 
Server database to use "Read Committed Snapshot Isolation" or RCSI.  RCSI provides 
a non-blocking and read consistent way to query data by using tempdb to store the 
"old" data.  This is the preferred method to query SQL Server and prevents doing
a dirty read.

- user_name and password
The easiest way to handle this is to use SQL Server authentication rather than
using an Active Directory account.

##################################################################################
#Hawq and Greenplum
##################################################################################
- connection_url=jdbc:postgresql://<host>:<port>/<database>

- class_for_name=org.postgresql.Driver

- read_committed=true
Hawq and Greenplum do not allow dirty reads and ignores the setting when set to 
false.

Note: the JDBC driver is already proviced with gplink in the jar directory.

##################################################################################
#PostgreSQL
##################################################################################
- connection_url=jdbc:postgresql://<host>:<port>/<database>

- class_for_name=org.postgresql.Driver

- read_committed=true
PostgreSQL does not allow dirty reads and ignores the setting when set to false.

Note: the Greenplum/Hawq JDBC driver will work with PostgreSQL but it is 
recommended to download the PostgreSQL driver that matches the version of 
PostgreSQL you are using.

##################################################################################
#Teradata
##################################################################################
- connection_url=jdbc:teradata://<host>/<database>,charset=UTF8,type=FASTEXPORT
Setting the charset to UTF8 allows the Teradata driver to convert the source 
data to the same character set as the target database (Greenplum or Hawq).  
type=FASTEXPORT optimizes the output of data at the expense of using more 
resources in Teradata.  You will need permissions in the database to use this 
option.  Omit this option if you need to.

- class_for_name=com.teradata.jdbc.TeraDriver

- read_committed=false
Teradata uses blocking locks to provide read consistency which means you will
need to use dirty read (read_committed=false) in order to prevent blocking locks.

##################################################################################
#DB2
##################################################################################
- connection_url=jdbc:db2://<host>:<port>/<database>

- class_for_name=com.ibm.db2.jcc.DB2Driver

- read_committed=false
DB2 is another database that provides read consistency with blocking locks.  The
solution to prevent long running queries from blocking other operations is to 
perform dirty reads.  Do this with setting read_committed=false.

Note: Not sure how to set UTF8.

##################################################################################
#MySQL
##################################################################################
- connection_url=jdbc://mysql//<host>:<port>/database

- class_for_name=com.mysql.jdbc.Driver

- read_committed=true
MySQL has several database engines to choose from but InnoDB allows for read 
consistency without blocking locks.  MyISAM doesn't.  Be sure you know your data-
base engine and pick the right read_committed value.

- extra_properties=useUnicode=yes;characterEncoding=utf8;
This sets the driver connection to use UTF8 to match Greenplum or Hawq.

