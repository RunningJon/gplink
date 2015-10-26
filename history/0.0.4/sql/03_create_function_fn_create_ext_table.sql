CREATE OR REPLACE FUNCTION gplink.fn_create_ext_table(p_id integer)
  RETURNS void AS
$$
DECLARE
        v_function_name text := 'gplink.fn_create_ext_table';
        v_location int;
        v_sql text;
        v_rec gplink.ext_tables%rowtype;
        
        v_count int;
BEGIN
        v_location := 1000;
        SELECT LOWER(table_name) as table_name, columns, column_datatypes, host, port
        INTO v_rec.table_name, v_rec.columns, v_rec.column_datatypes, v_rec.host, v_rec.port
        FROM gplink.ext_tables
        WHERE id = p_id;

        GET DIAGNOSTICS v_count = ROW_COUNT;
        
        v_location := 1500;
        IF v_count = 0 THEN
                RAISE EXCEPTION 'ID: "%" is not valid', p_id;
        END IF;

        v_location := 2000;
        v_sql := 'DROP EXTERNAL TABLE IF EXISTS ' || v_rec.table_name;
        RAISE INFO '%', v_sql;
        EXECUTE v_sql;

        v_location := 3000;
        FOR i IN array_lower(v_rec.columns, 1)..array_upper(v_rec.columns,1) LOOP
                IF i = 1 THEN
                        v_sql := 'CREATE EXTERNAL TABLE ' || v_rec.table_name || E' \n' ||
                                '("' || v_rec.columns[i] || '" ' || v_rec.column_datatypes[i];
                ELSE
                        v_sql := v_sql || E', \n' || '"' || v_rec.columns[i] || '" ' || v_rec.column_datatypes[i];
                END IF;

        END LOOP;

        v_location := 3500;
        v_sql := v_sql || E')\n';

        v_location := 4000;
        v_sql := v_sql || 'LOCATION (''gpfdist://' || v_rec.host || ':' || v_rec.port || '/gplink.properties+' || p_id || E'#transform=gplink'')\n';

        v_location := 5000;
        v_sql := v_sql || 'FORMAT ''TEXT'' (delimiter ''|'' null ''null'')';
        RAISE INFO '%', v_sql;
        EXECUTE v_sql;
EXCEPTION
        WHEN OTHERS THEN
                RAISE EXCEPTION '(%:%:%)', v_function_name, v_location, sqlerrm;
END;
$$
  LANGUAGE plpgsql;
