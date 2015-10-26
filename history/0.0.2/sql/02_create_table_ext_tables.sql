CREATE table gplink.ext_tables
(id serial not null,
 table_name text not null,
 columns text[] not null,
 column_datatypes text[] not null,
 sql_text text not null,
 connection_url text not null,
 class_for_name text not null,
 read_committed boolean default true not null,
 source_user_name text not null,
 source_password text not null,
 extra_properties text null,
 host text not null,
 port int not null
 )
DISTRIBUTED BY (id);
