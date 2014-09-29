package Armadillo.Core;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Random;

import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.junit.Test;

import Armadillo.Core.Console;
import Armadillo.Core.Logger;
import Armadillo.Core.ObjectWrapper;
import Armadillo.Core.Cache.SqliteCacheFullSchema;
import Armadillo.Core.Cache.SqliteConstants;
import Armadillo.Core.Cache.SqliteJdbcWrapper;
import Armadillo.Core.Concurrent.ILoopBody;
import Armadillo.Core.Concurrent.Parallel;
import Armadillo.Core.Concurrent.ProducerConsumerQueue;
import Armadillo.Core.Concurrent.Task;
import Armadillo.Core.Io.FileHelper;

public class SqliteTests 
{
	@Test
	public void testAParallelInsert()
	{
		try
		{
			Console.WriteLine("Running parallel insert test...");
			final int[] intItemCounter =new int[1];			
			final int[] intParallelCounter = new int[1];
			final int[] intRows = new int[1];
			final int intFooSize = 50;
			final String strFileName = "c:\\hc.java\\dbTestFullSchemaParallel4.db";
			if(FileHelper.exists(strFileName))
			{
				assertTrue("File could not be deleted", 
						FileHelper.delete(strFileName));
			}
			String strTableName = "Foo";
			String strDefaultIndex = SqliteConstants.KEY_COL_NAME;
			final SqliteCacheFullSchema<Foo> fooDb = new SqliteCacheFullSchema<Foo>(
					strFileName, 
					strTableName,
					strDefaultIndex,
					Foo.class);
			final int intParallelSize = 50;
			final Object lockObj = new Object();
			Parallel.For(0, intParallelSize, new ILoopBody<Integer>() 
					{
				
				public void run(Integer i0) 
				{
					
					try
					{
						synchronized(lockObj)
						{
							intParallelCounter[0]++;
							System.out.println("Parallel import " +
									intParallelCounter[0]);
						}
						
				        ArrayList<Foo> fooList = Foo.getFooList(intFooSize, false);
				        Hashtable<String,Foo> map = new Hashtable<String,Foo>();
						
				        for (int i = 0; i < intFooSize; i++) 
				        {
							synchronized(lockObj)
							{
								intItemCounter[0]++;
							}
				        	Foo currFoo = fooList.get(i);
				        	currFoo.m_string = currFoo.m_string + "_" + intItemCounter[0];
							map.put("key_" + i, 
									currFoo);
						}
						Task task = fooDb.insertMap(map);
				        task.waitTask();
						
				        synchronized(lockObj)
				        {
							intParallelCounter[0]--;
							intRows[0]+=intFooSize;
						}
					}
					catch(Exception ex)
					{
						Logger.log(ex);
					}
				}
			}, 10);

			Console.writeLine("Parallel counter " + intParallelCounter[0]);
			
	        ArrayList<String> keysToLoad = new ArrayList<String>();
	        for (int i = 0; i < intFooSize; i++) 
	        {
	        	String strCurrKey = "key_" + i;
	        	keysToLoad.add(strCurrKey);
	            boolean blnContainsKey = fooDb.containsKey(strCurrKey);
	            assertTrue("Key not found", blnContainsKey);
	            Foo[] fooArr = fooDb.loadDataFromKey(strCurrKey);
	            
	            if(fooArr.length != intParallelSize)
	            {
	            	throw new Exception("invalid number of rows " + intParallelSize + "=" + fooArr.length);
	            }
	            
	            assertTrue("invalid number of rows " + i + "=" + fooArr.length, 
	            		fooArr.length == intParallelSize);
			}
	        
	        String strQuery = "SELECT count(*) FROM FOO";
	        
	        int intRowCount = fooDb.executeScalar(strQuery);
	        assertTrue("invalid number of rows", intRowCount == intRows[0]);
			
	        
	        fooDb.delete(keysToLoad);
	        intRowCount = fooDb.executeScalar(strQuery);
	        assertTrue("invalid number of rows", intRowCount == 0);
		}
		catch(Exception ex)
		{
			Logger.log(ex);
			assertTrue("Exception occured", false);
		}
	}
		
	@Test
	public void testFile()
	{
		String strFileName = "c:\\hc.java\\dbTest.db";
		SqliteJdbcWrapper<Foo> dbWrapper = new  SqliteJdbcWrapper<Foo>(
				strFileName,
				null);
		
		assertTrue("File not found", FileHelper.exists(strFileName));
		
		System.out.println(dbWrapper);
	}

	@Test
	public void testFooBar(){
		
		try
		{
			final int[] m_intItemCounter = new int[1];
			final Object lockObj = new Object();
			final int intQueueCapacity = 500;
			final Random rng = new Random();
			ProducerConsumerQueue<ObjectWrapper> queue = 
					new ProducerConsumerQueue<ObjectWrapper>(5){
				
				@Override
				public void runTask(ObjectWrapper item){
					
					try{
					
						double dblRng;
						
						synchronized(lockObj){
							dblRng = rng.nextDouble();
						}
						boolean blnDoFoo = dblRng > 0.5; 
						synchronized(lockObj){
							dblRng = rng.nextDouble();
						}
						int intImportValues = (int) (1000 * dblRng);
						
						if(blnDoFoo){
							int intDbNumber = (int) (200 * dblRng);
							testFoo(dblRng, intImportValues, intDbNumber,
									m_intItemCounter);
						}
						else{
							int intDbNumber = (int) (10 * dblRng);
							testBar(dblRng, intImportValues, intDbNumber,
									m_intItemCounter);
						}
						
						//Thread.sleep(1000);
					}
					catch(Exception ex){
						
						Logger.log(ex);
					}
				}


			}; 
			
			int intEnqueued = 0;
			DateTime m_prevVerbose = DateTime.now();
			
			for(int i = 0; i < 500; i++){
				
				queue.add(new ObjectWrapper());
				int intQueueSize = queue.getSize();
				intEnqueued++;
				while(intQueueSize > intQueueCapacity){
					
					Thread.sleep(10);
					intQueueSize = queue.getSize();
				}
				
				if(Seconds.secondsBetween(
						m_prevVerbose, DateTime.now()).getSeconds() > 5){
					
					m_prevVerbose = DateTime.now();
					
					int intDone = queue.getDone();
					int intToDo = (intEnqueued - intDone);
					
					System.out.println("tasks in queue [" + intToDo + "]");
				}
			}
		}
		catch(Exception ex){
			
			Logger.log(ex);
		}
	}
	

	private static void testFoo(
			double dblRng, 
			int intImportValues,
			int intDbNumber,
			int[] m_intItemCounter) 
	{
		try
		{
			String strFooFileName = "c:\\hc.java\\dbtest\\foo\\sdbTestFullSchemaParallel_" +
					intDbNumber + ".db";
			
			String strTableName = "Foo";
			String strDefaultIndex = SqliteConstants.KEY_COL_NAME;
			SqliteCacheFullSchema<Foo> db = new SqliteCacheFullSchema<Foo>(
					strFooFileName, 
					strTableName,
					strDefaultIndex,
					Foo.class);
			
			ArrayList<Foo> list = Foo.getFooList(intImportValues, false);
	
			Hashtable<String,Foo> map = new Hashtable<String,Foo>();
			ArrayList<String> keyList = new ArrayList<String>();
			for (int i = 0; i < intImportValues; i++) {
				
				Foo currItem = list.get(i);
				currItem.m_string = currItem.m_string + "_" + m_intItemCounter[0];
				String strKey = "key_" + (new Integer(i).toString());
				map.put(strKey, 
						currItem);
				keyList.add(strKey);
			}
			
			ArrayList<Task> tasks = new ArrayList<Task>();
			
			//for (int i = 0; i < 20; i++) {
				
				Task currTasks = db.insertMap(map);
				currTasks.waitTask();
			//}
			Task.waitAll(tasks);
			Foo[] list2 = db.loadDataFromKeys(keyList);
			Arrays.sort(list2, new Comparator<Foo>(){
			    public int compare( Foo a, Foo b ){
			        return a.m_j - b.m_j;
			    }
			});	     
			
			int intListSize = list2.length;
			System.out.println("[" + intListSize + "] items already in db");
	
	
			for (int i = 0; i < intListSize; i++) {
				
			    Foo currItem = list2[i];
				String strCurrKey = currItem.getKey();
			    assertTrue("Items not the same", 
			    		currItem.compareSimple(map.get(strCurrKey)));
			}
			
			boolean blnRemoveItems = dblRng > 0.7;
			if(blnRemoveItems || intListSize > 5000){
				
				db.clear();
				int intAfterSize = db.getSize();
				
				if(intAfterSize > 0){
					
					System.out.println("!!!!!!! still in db [" +
							intAfterSize + "]");
				}
				else{
					System.out.println("All items deleted");
				}
			}
			map.clear();
			db.close();
			list.clear();
			list2 = null;
			keyList.clear();
		}
		catch(Exception ex){
			Logger.log(ex);
		}
	}	
	
	
	private static void testBar(
			double dblRng, 
			int intImportValues,
			int intDbNumber,
			int[] m_intItemCounter) 
	{
		
		try{
			
			String strBarFileName = "c:\\hc.java\\dbtest\\bar\\sdbTestFullSchemaParallel_" +
					intDbNumber + ".db";
			
			String strTableName = "Bar";
			String strDefaultIndex = SqliteConstants.KEY_COL_NAME;
			SqliteCacheFullSchema<Bar> db = new SqliteCacheFullSchema<Bar>(
					strBarFileName, 
					strTableName,
					strDefaultIndex,
					Bar.class);
			ArrayList<Bar> list = Bar.getBarList(intImportValues);
	
			Hashtable<String,Bar> map = new Hashtable<String,Bar>();
			ArrayList<String> keyList = new ArrayList<String>();
			for (int i = 0; i < intImportValues; i++) {
				
				Bar currItem = list.get(i);
				currItem.m_string = currItem.m_string + "_" + m_intItemCounter[0];
				String strKey = "key_" + (new Integer(i).toString());
				map.put(strKey, 
						currItem);
				keyList.add(strKey);
			}
			
			//ArrayList<Task> tasks = db.insertMap(map);
			//Task.waitAll(tasks);
			Bar[] list2 = db.loadDataFromKeys(keyList);
			Arrays.sort(list2, new Comparator<Bar>(){
			    public int compare( Bar a, Bar b ){
			        return a.m_j - b.m_j;
			    }
			});	     
			
			int intListSize = list2.length;
			System.out.println("[" + intListSize + "] items already in db");
		
			for (int i = 0; i < intListSize; i++) {
				
			    Bar currItem = list2[i];
				String strCurrKey = currItem.getKey();
			    assertTrue("Items not the same", 
			    		currItem.compareSimple(map.get(strCurrKey)));
			}
			
			boolean blnRemoveItems = dblRng > 0.7;
			if(blnRemoveItems || intListSize > 5000){
				db.clear();
				int intAfterSize = db.getSize();
				
				if(intAfterSize > 0){
					
					System.out.println("!!!!!!! still in db [" +
							intAfterSize + "]");
				}
				else{
					System.out.println("All items deleted");
				}
			}
			
			list2 = null;
			map.clear();
			db.close();
			list.clear();
			keyList.clear();
		}
		catch(Exception ex){
			Logger.log(ex);
		}
	}		
	@Test
	public void testSchemaValues()
	{
		try
		{
			String strFileName = "c:\\hc.java\\dbTestFullSchema2.db";
		
			assertTrue("File could not be deleted", 
					FileHelper.delete(strFileName));
					
			String strTableName = "FOO";
			String strDefaultIndex = SqliteConstants.KEY_COL_NAME;
			SqliteCacheFullSchema<Foo> fooDb = new SqliteCacheFullSchema<Foo>(
					strFileName, 
					strTableName,
					strDefaultIndex,
					Foo.class);
			
			assertTrue("File not found", FileHelper.exists(strFileName));
			
	        String strQuery = "SELECT EXISTS(SELECT name FROM sqlite_master WHERE name='" +
	        		strTableName.toUpperCase() + "')";
			
	        int intExistsTable = fooDb.executeScalar(strQuery);
	        assertTrue("table not found", intExistsTable == 1);
	        
	        String strIndex = strTableName + "_" + strDefaultIndex + "_INDEX";
	        strQuery = "SELECT EXISTS(SELECT name FROM sqlite_master WHERE name='" +
	        		strIndex + "')";
	        
	        int intExistsIndex = fooDb.executeScalar(strQuery);
	        assertTrue("table not found", intExistsIndex == 1);
	        
	        final int intFooSize = 20000; 
	        
	        ArrayList<Foo> fooList = Foo.getFooList(intFooSize, false);
	        Hashtable<String,Foo> map = new Hashtable<String,Foo>();
	        
	        for (int i = 0; i < intFooSize; i++) {
	        	
				map.put("key_" + new Integer(i).toString(), 
						fooList.get(i));
			}
	        
	        fooDb.insertMap(map).waitTask();
	        
	        strQuery = "SELECT count(*) FROM FOO";
	        
	        int intRowCount = fooDb.executeScalar(strQuery);
	        assertTrue("invalid number of rows", intRowCount == intFooSize);
	        
	        String strKey = "key_0";
	        boolean blnContainsKey = fooDb.containsKey(strKey);
	        assertTrue("key not found", blnContainsKey);
	        
	        Foo[] data = fooDb.loadDataFromKey(strKey);
	        boolean blnEquals = fooList.get(0).compare(data[0]);
	        assertTrue("rows not equal", blnEquals);
	        
	        ArrayList<String> keysToLoad = new ArrayList<String>();
	        for (int i = 0; i < intFooSize / 2; i++) {
	        	keysToLoad.add("key_" + i);
			}
	        data = fooDb.loadDataFromKeys(keysToLoad);
	        
	        Arrays.sort(data, new Comparator<Foo>(){
	            public int compare( Foo a, Foo b ){
	                return a.m_j - b.m_j;
	            }
	        });	        
	        
	        ArrayList<String> keysToDelete = new ArrayList<String>();
	        int intSampleSize = Math.min(500, intFooSize / 2);
	        for (int i = 0; i < intSampleSize; i++) {
	        	
	        	String strCurrKey = keysToLoad.get(i);
		        blnContainsKey = fooDb.containsKey(strCurrKey);
		        assertTrue("key not found", blnContainsKey);
		        blnEquals = fooList.get(i).compare(data[i]);
		        assertTrue("rows not equal", blnEquals);
		        keysToDelete.add(strCurrKey);
	        }
	        
	        fooDb.delete(strKey);
	        blnContainsKey = fooDb.containsKey(strKey);
	        assertTrue("key found", !blnContainsKey);
	        
	        fooDb.delete(keysToDelete);
	        data = fooDb.loadDataFromKeys(keysToDelete);
	        assertTrue("Rows found", data == null || data.length == 0);
		}
		catch(Exception ex)
		{
			Logger.log(ex);
			assertTrue("Exception occured", false);
		}
	}
}
