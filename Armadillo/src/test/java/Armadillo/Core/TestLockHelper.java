package Armadillo.Core;

import org.junit.Assert;
import org.junit.Test;

import Armadillo.Core.Concurrent.ILoopBody;
import Armadillo.Core.Concurrent.LockHelper;
import Armadillo.Core.Concurrent.Parallel;

public class TestLockHelper 
{
	private static int m_intLockCounter = 0;
	private static Object m_testLockObj = new Object(); 
	
	@Test
	public void testLockObjHelper(){
		Parallel.For(0, 10000, new ILoopBody<Integer>() {
			
			public void run(Integer i) {
				Object lockObj = LockHelper.GetLockObject("Hello");
				
				synchronized(lockObj){
					
					synchronized(m_testLockObj){
						m_intLockCounter++;
					}
					
					Assert.assertTrue(m_intLockCounter == 1);
					
					synchronized(m_testLockObj){
						m_intLockCounter--;
					}
				}
			}
		});
	}

}
