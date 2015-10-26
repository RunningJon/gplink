INSERT INTO gplink.ext_tables(
table_name, columns, column_datatypes, 
sql_text, connection_url, 
class_for_name, read_committed, source_user_name, source_password, 
extra_properties, host, port)
values 
('gplink_demo.ms_sqlserver', array['fname', 'lname'], array['text', 'text'],
'select ''jon'', ''roberts'' union all select ''JON'', ''ROBERTS''', 'jdbc:sqlserver://jonnywin;CODEPAGE=65001;responseBuffering=adaptive;selectMethod=cursor;',
'com.microsoft.sqlserver.jdbc.SQLServerDriver', true, 'sa', 'sa', null, 'bigmac', 8050);


INSERT INTO gplink.ext_tables(
table_name, columns, column_datatypes, 
sql_text, connection_url, 
class_for_name, read_committed, source_user_name, source_password, 
extra_properties, host, port)
values 
('gplink_demo.oracle', array['fname', 'lname'], array['text', 'text'],
'select ''jon'', ''roberts'' from dual union all select ''JON'', ''ROBERTS'' from dual', 'jdbc:oracle:thin:@jonnywin:1521/XE',
'oracle.jdbc.driver.OracleDriver', true, 'scott', 'tiger', 'defaultRowPrefetch=2000', 'bigmac', 8051);

