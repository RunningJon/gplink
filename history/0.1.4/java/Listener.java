import java.sql.*;
import java.net.*;
import java.io.*;
import java.util.*;

public class Listener
{
	public static String strName = "";
	public static String gplinkHome = "";
	public static String gplinkLog = "";
	public static String gplinkYml = "";
	public static Integer gplinkPortLower = 0;
	public static Integer gplinkPortUpper = 0;

	public static Integer getPort(Connection conn, String targetConfig) throws SQLException
	{
		try
		{
			loadProperties(targetConfig);

			Integer port = 0;
			//make sure all ports created with GPLink have been started so it isn't re-used

			for (int i=gplinkPortLower; i<gplinkPortUpper+1; i++)
			{
				if (!(checkPort(i)))
				{
					startPort(i);
					port = i;
					break;	
				}
			}

			return port;
		}
		catch (Exception ex)
		{
			throw new SQLException(ex.getMessage());
		}
	}

	public static void stopOrphanedPorts(Connection conn, String targetConfig) throws SQLException
	{
		try
		{
			loadProperties(targetConfig);
			//list of ports currently being used
			String strCommand = "ps -ef | grep gpfdist | grep -v grep | awk -F ' ' '{print $2 \",\" $12}'";
			String strSession = executeShell(strCommand);
		
			String[] sessions = strSession.split("\n", -1);
			String[] parts;

			Integer pid = 0;
			Integer sessionPort = 0;

			for (int i = 0; i < sessions.length-1; i++)
			{
				parts = sessions[i].split(",", -1);
				if (!parts[0].equals(""))
				{
					pid = Integer.parseInt(parts[0]);
				}
				if (GPLink.debug)
					System.out.println("pid: " + pid);

				if (!parts[1].equals(""))
				{
					sessionPort = Integer.parseInt(parts[1]);
				}
				if (GPLink.debug)
					System.out.println("sessionPort: " + sessionPort);

				if (sessionPort >= gplinkPortLower && sessionPort <= gplinkPortUpper && pid > 0)
				{
					if (!checkPort(conn, sessionPort))
						killPid(pid);
				} 
			}
		}
		catch (Exception ex)
		{
			throw new SQLException(ex.getMessage());
		}
	}

	public static void startMissingPorts(Connection conn, String targetConfig) throws SQLException
	{
		try
		{
			loadProperties(targetConfig);

			Integer portCheck = 0;
			String strSQL = getSQLForPorts(portCheck);
			if (GPLink.debug)
				System.out.println("strSQL: " + strSQL);

			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(strSQL);
			ResultSetMetaData rsmd = rs.getMetaData();
			int numberOfColumns = rsmd.getColumnCount();

			while (rs.next())
			{
				portCheck = rs.getInt(1);
				if (!checkPort(portCheck))
				{
					startPort(portCheck);
				}
			}

		}
		catch (SQLException ex)
		{
			String exceptionMessage = ex.getMessage();
			SQLException nextException = ex.getNextException();

			while (nextException != null)
			{
				exceptionMessage = exceptionMessage + " " + nextException.getMessage();
				nextException = nextException.getNextException();
			}

			throw new SQLException(exceptionMessage);
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

			gplinkHome = prop.getProperty("gplinkHome");
			gplinkLog = prop.getProperty("gplinkLog");
			gplinkYml = prop.getProperty("gplinkYml");
			gplinkPortLower = Integer.parseInt(prop.getProperty("gplinkPortLower"));
			gplinkPortUpper = Integer.parseInt(prop.getProperty("gplinkPortUpper"));

			Validation.checkProperty("gplinkHome", gplinkHome);
			Validation.checkProperty("gplinklog", gplinkLog);
			Validation.checkProperty("gplinkYml", gplinkYml);
			Validation.checkProperty("gplinkPortLower", gplinkPortLower);
			Validation.checkProperty("gplinkPortUpper", gplinkPortUpper);
			
		}
		catch (Exception ex)
		{
			throw new SQLException(ex.getMessage());
		}
	}

	private static String getSQLForPorts(Integer myPort) throws SQLException
	{
		try
		{
			if (GPLink.debug)
				System.out.println("MyPort: " + myPort);
			String strSQL = "select (split_part(split_part(e.location[1], '/', 3), ':', 2))::int as port\n";
			strSQL += "from pg_class c\n";
			strSQL += "join pg_namespace n on c.relnamespace = n.oid\n";
			strSQL += "join pg_exttable e on c.oid = e.reloid\n";
			strSQL += "where e.location is not null\n";
			strSQL += "and writable is false\n";
			strSQL += "and lower(location[1]) like 'gpfdist%'\n";
			strSQL += "and split_part(split_part(e.location[1], '/', 3), ':', 1) = '" + GPLink.hostName + "'\n";
			strSQL += "and (split_part(split_part(e.location[1], '/', 3), ':', 2))::int >= " + gplinkPortLower + "\n";
			strSQL += "and (split_part(split_part(e.location[1], '/', 3), ':', 2))::int <= " + gplinkPortUpper + "\n";

			if (myPort > 0)
			{
				strSQL += "and (split_part(split_part(e.location[1], '/', 3), ':', 2))::int = " + myPort + "\n";
			}
			strSQL += "order by 1";

			return strSQL;
		}
		catch (Exception ex)
		{
			throw new SQLException(ex.getMessage());
		}
	}

	private static void startPort(Integer myPort) throws SQLException
	{
		try
		{

			gplinkLog += "_" + myPort;	
			String strCommand="gpfdist -d " + gplinkHome + " -p " + myPort + " -c " + gplinkYml + " >> " + gplinkLog+ " 2>&1 < " + gplinkLog + " &";
			String result = executeShell(strCommand);
		
		}
		catch (Exception ex)
		{
			throw new SQLException(ex.getMessage());
		}
	}

	private static boolean checkPort(Integer myPort) throws SQLException
	{
		try
		{
			boolean result = false;

			String strCommand = "ps -ef | grep gpfdist | grep -v grep | grep " + myPort + " | wc -l";
			String count = executeShell(strCommand);
			count = count.trim();

			if (count.equals("1"))
				result = true;
			else if (count.equals("0"))
				result = false;

			if (GPLink.debug)	
				System.out.println("Result: " + result);
			return result;	
		}
		catch (Exception ex)
		{
			throw new SQLException(ex.getMessage());
		}
	}

	private static boolean checkPort(Connection conn, Integer myPort) throws SQLException
	{
		try
		{
			boolean result = false;

			String strSQL = getSQLForPorts(myPort);
			if (GPLink.debug)
				System.out.println("strSQL: " + strSQL);

			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(strSQL);

			while (rs.next())
			{
				result = true;
			}

			if (GPLink.debug)
				System.out.println("Result: " + result);
			return result;	
		}
		catch (Exception ex)
		{
			throw new SQLException(ex.getMessage());
		}
	}

	private static void killPid(Integer pid) throws SQLException
	{
		try
		{
			String strCommand = "kill " + pid;
			executeShell(strCommand);

			strCommand = "sleep .4";
			executeShell(strCommand);
		}
		catch (Exception ex)
		{
			throw new SQLException(ex.getMessage());
		}
	}

	private static String executeShell(String strCommand) throws SQLException
	{
		try
		{
			if (GPLink.debug)
				System.out.println("strCommand: " + strCommand);
			String[] cmds = {"/bin/bash", "-c", strCommand};

			Runtime run = Runtime.getRuntime();
			Process pr = run.exec(cmds);
			pr.waitFor();
			BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
			String line = "";
			String output = "";
			while ((line=buf.readLine())!=null) 
			{
				output = output + line + "\n";
			}

			if (GPLink.debug)
				System.out.println("Output: " + output);
			return output;
		}
		catch (Exception ex)
		{
			throw new SQLException(ex.getMessage());
		}
	}
}
