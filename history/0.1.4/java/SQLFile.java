import java.sql.*;
import java.io.*;

public class SQLFile
{
	public static String getSQLText(String sqlFile) throws SQLException
	{
		String sqlText = "";
		String line = null;
		try
		{
			FileReader fileReader = new FileReader(sqlFile);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			while((line = bufferedReader.readLine()) != null) 
			{
				if (sqlText.equals(""))
					sqlText=line;
				else		
					sqlText= sqlText + " " + line;
			}

			bufferedReader.close();

			return sqlText;
		}
		catch (Exception ex)
		{
			throw new SQLException(ex.getMessage());
		}
	}
}
