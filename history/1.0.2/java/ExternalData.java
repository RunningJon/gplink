import java.sql.*;
import java.net.*;
import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class ExternalData
{
	public static void main(String[] args) throws SQLException
	{
		String configFile = args[0];
		String sqlFile = args[1];
		Connection conn = null;
		String sqlText = "";

		try
		{
			conn = CommonDB.connect(configFile);
			sqlText = SQLFile.getSQLText(sqlFile);
			outputData(conn, sqlText);
			conn.close();
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

	private static void outputData(Connection conn, String sqlText) throws SQLException
	{
		try
		{
			Statement stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			ResultSet rs = stmt.executeQuery(sqlText);
			ResultSetMetaData rsmd = rs.getMetaData();
			int numberOfColumns = rsmd.getColumnCount();

			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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
						if (rsmd.getColumnTypeName(i) == "DATE" || rsmd.getColumnTypeName(i) == "TIMESTAMP")
						{
							columnValue = df.format(rs.getTimestamp(i));
						}
				
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
