import java.sql.*;
import java.net.*;
import java.io.*;
import java.util.*;

public class CommonDB
{
	public static Connection connect(String configFile) throws SQLException
	{
		try
		{
			String connectionUrl = "";
			String classForName = "";
			Boolean readCommitted = false;
			String userName = "";
			String password = "";
			String extraProps = "";
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

			Validation.checkProperty("connectionUrl", connectionUrl);
			Validation.checkProperty("classForName", classForName);
			Validation.checkProperty("readCommitted", readCommitted);
			Validation.checkProperty("userName", userName);
			Validation.checkProperty("password", password);
			//skipping extraProps because it can be an empty string
			
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

			if (extraProps != null && (!extraProps.equals("")))
			{
				extraPropsArray = extraProps.split(";");
			}


			Class.forName(classForName);
			System.setProperty("java.security.egd", "file:///dev/urandom");
			Properties props = new Properties();
			props.put("user", userName);
			props.put("password", password);

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

			Connection conn = DriverManager.getConnection(connectionUrl, props);

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
