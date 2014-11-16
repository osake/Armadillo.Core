package Web.Authentication;

import org.junit.Test;

import Armadillo.Core.Console;
import Armadillo.Core.Logger;
import Armadillo.Core.Cache.SqliteCacheFullSchema;
import Armadillo.Core.Text.StringHelper;
import Web.Base.AWebConstants;

public class UserCacheHelper 
{

	private static SqliteCacheFullSchema<UserItem> m_userDb;
	
	static
	{
		try
		{
			AWebConstants constants = AWebConstants.getOwnInstance();
			if(constants != null)
			{
				m_userDb = new SqliteCacheFullSchema<UserItem>(
						constants.getUsersDbFileName(), 
						UserItem.class);
			}
		}
		catch(Exception ex){
			Logger.log(ex);
		}
	}
	
	@Test
	public void doTestUser()
	{
		try
		{
			UserItem user = new UserItem();
			user.setUser("a");
			user.setPsw("a");
			user.setEmail("camacho_horacio@hotmail.com");
			addUserToDb(user);
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}
	
	public static boolean containsKey(String strKey)
	{
		return m_userDb.containsKey(strKey);		
	}

	@Test
	public void getUser()
	{
			m_userDb = new SqliteCacheFullSchema<UserItem>(
					"C:\\HC.Java\\data\\Users\\Users.db", 
					UserItem.class);
		
		UserItem[] users = m_userDb.loadDataFromKey("cesar");
		Console.writeLine(users);
	}
	private static void addUserToDb(UserItem user) 
	{
		try
		{
			if(user == null)
			{
				return;
			}
			String strKey = getUserKey(user);
			if(m_userDb.containsKey(strKey))
			{
				m_userDb.delete(strKey);
			}
			m_userDb.insert(strKey, user).waitTask();
		}
		catch(Exception ex){
			Logger.log(ex);
		}
	}
	
	@Test
	public void getAllUsers()
	{
		try 
		{
			SqliteCacheFullSchema<UserItem> userDb = 
					new SqliteCacheFullSchema<UserItem>(
							"C:\\HC.Java\\data\\Users\\Users.db", 
						UserItem.class);
			
			UserItem[] users = userDb.loadAllData();
			Console.writeLine(users.length);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	private static String getUserKey(UserItem user)
	{
		try
		{
			if(user == null)
			{
				return "";
			}
			return user.getUser();
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return "";
	}

	public static UserItem getUserFromCache(String strKey) 
	{
		try
		{
			if(StringHelper.IsNullOrEmpty(strKey))
			{
				return null;
			}
			UserItem user = m_userDb.loadDataFromKey(strKey)[0];
			return user;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return null;
	}
}
