import java.sql.*;
import java.net.*;
import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class TargetData
{
	public static String createTable(Connection conn, String targetTable, String sourceConfig, String sql) throws SQLException
	{
		try
		{
			String tableName = "";
			String columns = "";
			Integer myPort = Listener.getPort(conn);
			if (GPLink.debug)
				System.out.println("Port: " + myPort);

			Properties prop = new Properties();
			InputStream inputStream = new FileInputStream(targetTable);

			if (inputStream != null)
			{
				prop.load(inputStream);
			}
			else
			{
				throw new FileNotFoundException(targetTable + " not found!");
			}

			tableName = prop.getProperty("tableName");
			tableName = tableName.toLowerCase();
			columns= prop.getProperty("columns");
			Validation.checkProperty("tableName", tableName);
			Validation.checkProperty("columns", columns);

			String strSQL = "DROP EXTERNAL TABLE IF EXISTS " + tableName + ";";
			if (GPLink.debug)
				System.out.println(strSQL);
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(strSQL);

			strSQL = "CREATE EXTERNAL TABLE " + tableName + "\n";
			strSQL += "(" + columns + ")\n";
			strSQL += "LOCATION ('gpfdist://" + GPLink.hostName + ":" + myPort + "/" + sourceConfig + "+" + sql + "#transform=gplink')\n";
			strSQL += "FORMAT 'TEXT' (delimiter '|' null 'null');";
			if (GPLink.debug)
				System.out.println(strSQL);
			stmt.executeUpdate(strSQL);

			return tableName;

		}
		catch (IOException iex)
		{
			throw new SQLException(iex.getMessage());
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

	public static void dropTable(Connection conn, String tableName) throws SQLException
	{
		try
		{
			tableName = tableName.toLowerCase();
			String strSQL = "DROP EXTERNAL TABLE IF EXISTS " + tableName + ";";
			if (GPLink.debug)
				System.out.println(strSQL);
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(strSQL);

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
}
