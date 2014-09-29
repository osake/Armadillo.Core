package Armadillo.Core.Cache;

import java.sql.Connection;
import java.sql.PreparedStatement;
import org.apache.commons.lang.StringUtils;
import Armadillo.Core.Logger;

public class SqliteBulkInsert {
	
	private static final int COMMIT_MAX = 10000;
	
	public static void bulkInsert(
			String strTableName,
			String[] columns,
			Connection connection,
			Object[][] objs)
	{
		try
		{
			if(objs == null ||
			   objs.length == 0 ||
			   columns == null ||
			   columns.length == 0)
			{
				return;
			}
			
		    String strSql = getSql(strTableName, columns);
			doInsert(
					connection,
					strSql,
					objs);
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}

	private static String getSql(
			String strTableName,
			String[] columns) {
		StringBuilder sb = new StringBuilder("?");
	    for (int i = 1; i < columns.length; i++) {
	        sb.append(",");
	        sb.append("?");
	    }
	    
		String strSql = 
				"insert into " + strTableName + "(" + 
						StringUtils.join(columns, ",") +
					") values (" + sb.toString() + ")";
		return strSql;
	}
	
	private static void doInsert(
			Connection connection,
			String strSql,
			Object[][] objs){
		
		try {
			
			if(objs == null ||
			   objs.length == 0)
			{
				return;
			}
			connection.setAutoCommit(false);
			int intCounter = 0;
			PreparedStatement preparedStatement = 
					connection.prepareStatement(strSql);
			try
			{
				for (int i = 0; i < objs.length; i++) {
					
					Object[] objects = objs[i];
					
					if(objects == null){
						throw new Exception("Null object array");
					}
					
					for(int j = 0; j < objects.length; j++){
						
						try
						{
							preparedStatement.setObject(
									j+1,
									objects[j]);
							objects[j] = null;
						}
						catch(Exception e){
							Logger.log(e);
						}
					}
					objs[i] = null;
					preparedStatement.addBatch();
					intCounter++;
					
					if(intCounter == COMMIT_MAX){
						intCounter = 0;
						preparedStatement.executeBatch();
					}
				}
			}
			catch(Exception ex2){
				Logger.log(ex2);
			}
			finally
			{
				if(intCounter > 0){
					preparedStatement.executeBatch();
				}
				
				preparedStatement.close();
				connection.setAutoCommit(true);
			}
			
		} catch (Exception ex) {
			Logger.log(ex);
		}
	}
}