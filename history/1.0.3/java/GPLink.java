import java.sql.*;
import java.net.*;
import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class GPLink
{
	public static Boolean debug = true;
	public static String gplinkHostName = "";
	public static String gpfdistMaxLength = "";
        public static String gplinkHome = "";
        public static String gplinkLog = "";
        public static String gplinkYml = "";
        public static Integer gplinkPortLower = 0;
        public static Integer gplinkPortUpper = 0;

	public static void main(String[] args) throws SQLException
	{
		String targetConfig = "";
		String tableName = "";

		Connection conn = null;

		try
		{
			int argsCount = args.length;
			if (argsCount == 5)
			//Creating a table
			{
				String sourceConfig = args[0];
				String sql = args[1];
				String targetTable = args[2];
				targetConfig = args[3];
				debug = Boolean.valueOf(args[4]);
				loadProperties(targetConfig);
				conn = CommonDB.connect(targetConfig);
				Listener.startMissingPorts(conn);
				tableName = TargetData.createTable(conn, targetTable, sourceConfig, sql);
				Listener.stopOrphanedPorts(conn);
				conn.close();
				System.out.println("External Table: " + tableName + " CREATED.");
			}
			else if (argsCount == 3)
			//Dropping a table
			{
				targetConfig = args[0];
				tableName = args[1];
				debug = Boolean.valueOf(args[2]);
				loadProperties(targetConfig);
				conn = CommonDB.connect(targetConfig);
				Listener.startMissingPorts(conn);
				TargetData.dropTable(conn, tableName);
				Listener.stopOrphanedPorts(conn);
				conn.close();
				System.out.println("External Table: " + tableName + " DROPPED.");
			}
			else if (argsCount == 2)
			//Starting missing gpfdist processes
			{
				targetConfig = args[0];
				debug = Boolean.valueOf(args[1]);
				loadProperties(targetConfig);
				conn = CommonDB.connect(targetConfig);
				Listener.startMissingPorts(conn);
				conn.close();
				System.out.println("Started all ports needed.");
			}
		
		}
		catch (SQLException ex)
		{
			throw new SQLException(ex.getMessage());
		}
		finally
		{
			if (conn != null)
				conn.close();
		}
	}

	private static void loadProperties(String targetConfig) throws SQLException
	{
		try
		{
			Properties prop = new Properties();
			InputStream inputStream = new FileInputStream(targetConfig);
			if (inputStream != null)
			{
				 prop.load(inputStream);
			}
			else
			{
				throw new FileNotFoundException(targetConfig + " not found!");
			}

			gplinkHostName = prop.getProperty("gplinkHostName");
			gpfdistMaxLength = prop.getProperty("gpfdistMaxLength");
			gplinkHome = prop.getProperty("gplinkHome");
			gplinkLog = prop.getProperty("gplinkLog");
			gplinkYml = prop.getProperty("gplinkYml");
			gplinkPortLower = Integer.parseInt(prop.getProperty("gplinkPortLower"));
			gplinkPortUpper = Integer.parseInt(prop.getProperty("gplinkPortUpper"));

			Validation.checkProperty("gplinkHostName", gplinkHostName);
			Validation.checkProperty("gpfdistMaxLength", gpfdistMaxLength);
			Validation.checkProperty("gplinkHome", gplinkHome);
			Validation.checkProperty("gplinkLog", gplinkLog);
			Validation.checkProperty("gplinkYml", gplinkYml);
			Validation.checkProperty("gplinkPortLower", gplinkPortLower);
			Validation.checkProperty("gplinkPortUpper", gplinkPortUpper);
		}
		catch (Exception ex)
		{
			throw new SQLException(ex.getMessage());
		}
	}
}
