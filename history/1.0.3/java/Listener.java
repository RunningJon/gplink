import java.sql.*;
import java.net.*;
import java.io.*;
import java.util.*;

public class Listener
{
	private static String getSQLForVersion() throws SQLException
	{
		try
		{
			String strSQL = "SELECT CASE WHEN POSITION ('Greenplum Database 4.3' IN version) > 0 THEN 'gpdb_4_3'\n";
			strSQL += "WHEN POSITION ('Greenplum Database 5' IN version) > 0 THEN 'gpdb_5'\n";
			strSQL += "ELSE 'OTHER' END\n";
			strSQL += "FROM version()";
			return strSQL;
		}
		catch (Exception ex)
		{
			throw new SQLException(ex.getMessage());
		}
	}
	private static String getVersion(Connection conn) throws SQLException
	{
		try
		{
			String version = "gpdb_4_3";
			String strSQL = getSQLForVersion();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(strSQL);
			ResultSetMetaData rsmd = rs.getMetaData();

			while (rs.next())
			{
				version = rs.getString(1);
			}
			return version;

		}	
		catch (Exception ex)
		{
			throw new SQLException(ex.getMessage());
		}
	}
	public static Integer getPort(Connection conn) throws SQLException
	{
		try
		{
			Integer port = 0;
			//make sure all ports created with GPLink have been started so it isn't re-used

			for (int i=GPLink.gplinkPortLower; i<GPLink.gplinkPortUpper+1; i++)
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
	public static void stopOrphanedPorts(Connection conn) throws SQLException
	{
		try
		{
			//list of ports currently being used
			String strCommand = "ps -ef 2> /dev/null | grep gpfdist | grep -v grep | awk -F ' ' '{print $2 \",\" $12}'";
			String strSession = executeShell(strCommand);
		
			String[] sessions = strSession.split("\n", -1);
			String[] parts;

			Integer pid = 0;
			Integer sessionPort = 0;

			for (int i = 0; i < sessions.length-1; i++)
			{
				pid = 0;
				sessionPort = 0;

				parts = sessions[i].split(",", -1);
				if (!parts[0].equals(""))
				{
					try
					{
						pid = Integer.parseInt(parts[0]);
					}
					catch(NumberFormatException nfe)
					{
						pid = 0;
					}
				}
				if (GPLink.debug)
					System.out.println("pid: " + pid);

				if (!parts[1].equals(""))
				{
					try
					{
						sessionPort = Integer.parseInt(parts[1]);
					}
					catch(NumberFormatException nfe)
					{
						sessionPort = 0;
					}
				}
				if (GPLink.debug)
					System.out.println("sessionPort: " + sessionPort);

				if (sessionPort >= GPLink.gplinkPortLower && sessionPort <= GPLink.gplinkPortUpper && pid > 0)
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
	public static void startMissingPorts(Connection conn) throws SQLException
	{
		try
		{
			Integer portCheck = 0;
			String version = getVersion(conn);
			if (GPLink.debug)
				System.out.println("version:" + version);
			String strSQL = getSQLForPorts(version, portCheck);
			if (GPLink.debug)
				System.out.println("strSQL: " + strSQL);

			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(strSQL);
			ResultSetMetaData rsmd = rs.getMetaData();

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
	private static String getSQLForPorts(String version, Integer myPort) throws SQLException
	{
		try
		{
			if (GPLink.debug)
				System.out.println("MyPort: " + myPort);
			String strSQL = "";
			if (version.equals("gpdb_4_3"))
			{
				strSQL = "select (split_part(split_part(e.location[1], '/', 3), ':', 2))::int as port\n";
				strSQL += "from pg_class c\n";
				strSQL += "join pg_namespace n on c.relnamespace = n.oid\n";
				strSQL += "join pg_exttable e on c.oid = e.reloid\n";
				strSQL += "where e.location is not null\n";
				strSQL += "and writable is false\n";
				strSQL += "and lower(location[1]) like 'gpfdist%'\n";
				strSQL += "and split_part(split_part(e.location[1], '/', 3), ':', 1) = '" + GPLink.gplinkHostName + "'\n";
				strSQL += "and (split_part(split_part(e.location[1], '/', 3), ':', 2))::int >= " + GPLink.gplinkPortLower + "\n";
				strSQL += "and (split_part(split_part(e.location[1], '/', 3), ':', 2))::int <= " + GPLink.gplinkPortUpper + "\n";

				if (myPort > 0)
				{
					strSQL += "and (split_part(split_part(e.location[1], '/', 3), ':', 2))::int = " + myPort + "\n";
				}
				strSQL += "order by 1";
			} else if (version.equals("gpdb_5"))
			{
				strSQL = "select (split_part(split_part(e.urilocation[1], '/', 3), ':', 2))::int as port\n";
				strSQL += "from pg_class c\n";
				strSQL += "join pg_namespace n on c.relnamespace = n.oid\n";
				strSQL += "join pg_exttable e on c.oid = e.reloid\n";
				strSQL += "where e.urilocation is not null\n";
				strSQL += "and writable is false\n";
				strSQL += "and lower(urilocation[1]) like 'gpfdist%'\n";
				strSQL += "and split_part(split_part(e.urilocation[1], '/', 3), ':', 1) = '" + GPLink.gplinkHostName + "'\n";
				strSQL += "and (split_part(split_part(e.urilocation[1], '/', 3), ':', 2))::int >= " + GPLink.gplinkPortLower + "\n";
				strSQL += "and (split_part(split_part(e.urilocation[1], '/', 3), ':', 2))::int <= " + GPLink.gplinkPortUpper + "\n";

				if (myPort > 0)
				{
					strSQL += "and (split_part(split_part(e.urilocation[1], '/', 3), ':', 2))::int = " + myPort + "\n";
				}
				strSQL += "order by 1";
			}
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

			GPLink.gplinkLog += "_" + myPort;	
			String strCommand="gpfdist -d " + GPLink.gplinkHome + " -p " + myPort + " -c " + GPLink.gplinkYml + " -m " + GPLink.gpfdistMaxLength + " >> " + GPLink.gplinkLog+ " 2>&1 < " + GPLink.gplinkLog + " &";
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

			String strCommand = "ps -ef 2>/dev/null | grep gpfdist | grep -v grep | grep " + myPort + " | wc -l";
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

			String version = getVersion(conn);
			String strSQL = getSQLForPorts(version, myPort);
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
			InputStream stderr = null;
			InputStream stdout = null;

			if (GPLink.debug)
				System.out.println("strCommand: " + strCommand);
			String[] cmds = {"/bin/bash", "-c", strCommand};

			Runtime run = Runtime.getRuntime();
			Process pr = run.exec(cmds);
			stdout = pr.getInputStream();
			stderr = pr.getErrorStream();
			pr.waitFor();

			//BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
			BufferedReader buf = new BufferedReader(new InputStreamReader(stdout));
			String line = "";
			String output = "";
			while ((line = buf.readLine()) != null) 
			{
				output = output + line + "\n";
			}

			if (GPLink.debug)
				System.out.println("Output: " + output);

      			buf.close();

			String stdErrOutput = "";
			buf = new BufferedReader (new InputStreamReader (stderr));
			while ((line = buf.readLine()) != null) 
			{
				stdErrOutput += stdErrOutput;
      			}
      			buf.close();

			if (!(stdErrOutput.equals("")))
				throw new SQLException(stdErrOutput);

			return output;
		}
		catch (Exception ex)
		{
			throw new SQLException(ex.getMessage());
		}
	}
}
