import java.sql.*;
import java.io.*;

public class Validation
{

	public static void checkProperty(String strPropertyName, String strPropertyValue) throws SQLException
	{
		try
		{
			if (strPropertyValue == null)
				throw new SQLException("Property Value for: " + strPropertyName + " not found!");
		}
		catch (Exception ex)
		{
			throw new SQLException(ex.getMessage());
		}
	}

	public static void checkProperty(String intPropertyName, Integer intPropertyValue) throws SQLException
	{
		try
		{
			if (intPropertyValue == null)
				throw new SQLException("Property Value for: " + intPropertyName + " not found!");
		}
		catch (Exception ex)
		{
			throw new SQLException(ex.getMessage());
		}
	}

	public static void checkProperty(String booleanPropertyName, Boolean booleanPropertyValue) throws SQLException
	{
		try
		{
			if (booleanPropertyValue == null)
				throw new SQLException("Property Value for: " + booleanPropertyName + " not found!");
		}
		catch (Exception ex)
		{
			throw new SQLException(ex.getMessage());
		}
	}
}
