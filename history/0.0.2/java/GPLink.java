import java.sql.*;
import java.net.*;
import java.io.*;
import java.util.*;

public class GPLink
{
	public static void main(String[] args) throws SQLException
	{
		String configFile = args[0];
		String id = args[1];

		try
		{
			outputData(configFile, id);
		}
		catch (SQLException ex)
		{
			throw new SQLException(ex.getMessage());
		}
	}

	private static void outputData(String configFile, String id) throws SQLException
	{
		Connection pivotalConn = null;
		Connection sourceConn = null;

		try
		{
			//Get the SQL Statement to execute and connection information from the Pivotal Greenplum or Hawq database
			pivotalConn = connect(configFile);

			String sourceSQLText = "";
			String sourceConfigFile = "";
			String connectionUrl = "";
			String classForName = "";
			Boolean readCommitted = false;
			String userName = "";
			String password = "";
			String extraProps = "";

			String strSQL = "SELECT sql_text, connection_url, class_for_name, read_committed, source_user_name, source_password, extra_properties\n";
			strSQL += "FROM gplink.ext_tables\n";
			strSQL += "WHERE id = " + id;

			Statement stmt = pivotalConn.createStatement();
			ResultSet rs = stmt.executeQuery(strSQL);
			while (rs.next())
			{
				sourceSQLText = rs.getString(1);
				connectionUrl = rs.getString(2);
				classForName = rs.getString(3);
				readCommitted = rs.getBoolean(4);
				userName = rs.getString(5);
				password = rs.getString(6);
				extraProps = rs.getString(7);
			}

			pivotalConn.close();

			//Get the data from the SQL statement from the source database via JDBC
			sourceConn = connect(connectionUrl, classForName, readCommitted, userName, password, extraProps);

			PreparedStatement ps = sourceConn.prepareStatement(sourceSQLText);
			rs = ps.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			int numberOfColumns = rsmd.getColumnCount();

			String output;
			String columnValue = "";

			while (rs.next())
			{
				output="";
				// Get the column names; column indices start from 1
				for (int i=1; i<numberOfColumns+1; i++)
				{
					//Filter out \ and | from the columnValue for not null records.  the rest will default to "null"
					columnValue = rs.getString(i);
					if (columnValue != null)
					{
						columnValue = columnValue.replace("\\", "\\\\");
						columnValue = columnValue.replace("|", "\\|");
						columnValue = columnValue.replace("\r", " ");
						columnValue = columnValue.replace("\n", " ");
						columnValue = columnValue.replace("\0", "");
					}

					if (i == 1)
						output = columnValue;
					else
						output = output + "|" + columnValue;
				}

				System.out.println(output);
			}

			sourceConn.close();
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
		finally
		{
			if (pivotalConn != null)
				pivotalConn.close();
			if (sourceConn != null)
				sourceConn.close();
		}
	}

	private static Connection connect(String configFile) throws SQLException
	{
		try
		{
			String connectionUrl = "";
			String classForName = "";
			Boolean readCommitted = false;
			String userName = "";
			String password = "";
			String extraProps = "";
			String[] extraPropsArray = new String[20];
			String strName = "";
			String strValue = "";
			String[] myProp;

			Properties prop = new Properties();
			InputStream inputStream = new FileInputStream(configFile);

			if (inputStream != null)
			{
				prop.load(inputStream);
			}
			else
			{
				throw new FileNotFoundException(configFile + " not found!");
			}

			// set the property values
			connectionUrl = prop.getProperty("connectionUrl");
			classForName = prop.getProperty("classForName");
			readCommitted = Boolean.valueOf(prop.getProperty("readCommitted"));
			userName = prop.getProperty("userName");
			password = prop.getProperty("password");
			extraProps=prop.getProperty("extraProps");

			Connection conn = connect(connectionUrl, classForName, readCommitted, userName, password, extraProps);

			return conn;
		}
		catch (SQLException ex)
		{
			throw new SQLException(ex.getMessage());
		}
		catch (IOException iex)
		{
			throw new SQLException(iex.getMessage());
		}
	}

	private static Connection connect(String connectionUrl, String classForName, Boolean readCommitted, String userName, String password, String extraProps) throws SQLException
	{
		try
		{
			String strName = "";
			String strValue = "";
			String[] extraPropsArray = new String[20];
			String[] myProp;

			if (extraProps != null)
			{
				extraPropsArray = extraProps.split(";");
			}

			Class.forName(classForName);
			System.setProperty("java.security.egd", "file:///dev/urandom");
			Properties props = new Properties();

			if (extraPropsArray != null && extraPropsArray.length != 0)
			{
				for (int i = 0; i < extraPropsArray.length; i++)
				{
					if (extraPropsArray[i] != null)
					{
						myProp = extraPropsArray[i].split("=");
						strName = myProp[0];
						strValue = myProp[1];
						props.put(strName, strValue);
					}
				}
			}

			Connection conn = DriverManager.getConnection(connectionUrl, userName, password);

			int transactionIsolationLevel = 0;

			if (readCommitted) 
				transactionIsolationLevel = Connection.TRANSACTION_READ_COMMITTED;
			else
				transactionIsolationLevel = Connection.TRANSACTION_READ_UNCOMMITTED;

			conn.setTransactionIsolation(transactionIsolationLevel);
	
			return conn;

		}
		catch (ClassNotFoundException e)
		{
			throw new SQLException(e.getMessage());
		}
		catch (SQLException ex)
		{
			throw new SQLException(ex.getMessage());
		}
	}
}
