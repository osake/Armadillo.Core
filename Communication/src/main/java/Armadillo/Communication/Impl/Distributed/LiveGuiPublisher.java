package Armadillo.Communication.Impl.Distributed;

import Armadillo.Core.Console;
import Armadillo.Core.Logger;

public class LiveGuiPublisher {

	public static final LiveGuiPublisher OwnInstance = new LiveGuiPublisher();

	public void PublishGui(
			String string, 
			String string2, 
			String string3,
			String strKey, 
			Object jobLog) {
		
		String strMessage = jobLog.toString();
		Logger.log(strMessage);
		Console.writeLine(strMessage);
	}

	public void PublishLog(String string, String string2, String string3,
			String string4, String strLog) {
		Logger.log(strLog);
		Console.writeLine(strLog);
	}

}
