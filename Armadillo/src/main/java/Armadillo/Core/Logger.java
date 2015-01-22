package Armadillo.Core;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.FileAppender;
import org.apache.log4j.PatternLayout;

import Armadillo.Core.Concurrent.EfficientProducerConsumerQueue;
import Armadillo.Core.Io.FileHelper;
import Armadillo.Core.Io.PathHelper;
import Armadillo.Core.Text.StringHelper;
import Armadillo.Core.UI.LiveGuiPublisher;

public class Logger 
{
	static org.apache.log4j.Logger m_logger;
	private static EfficientProducerConsumerQueue<Exception> m_exceptionQueue;

	static 
	{
		String strLogFileName = getLogFileName();
		loadLogger(strLogFileName);
		
		m_exceptionQueue = new EfficientProducerConsumerQueue<Exception>(1){
			@Override
			public void runTask(Exception exception) 
			{
				try
				{
					LiveGuiPublisher.PublishGui(
							"Admin", 
							"Exceptions", 
							"Exceptions", 
							Guid.NewGuid().toString(), 
							exception.toString());
				}
				catch(Exception ex)
				{
					exception.printStackTrace();
					ex.printStackTrace();
				}
			}
			
		};
	}

	public static void log(Exception ex) 
	{
		ex.printStackTrace();
		m_logger.info(ex.getMessage() + " - " + 
				ExceptionUtils.getStackTrace(ex));
		enqueueException(ex);
	}
	
	private static void enqueueException(Exception ex)
	{
		try
		{
			if(m_exceptionQueue.getSize() > 500)
			{
				return;
			}
			m_exceptionQueue.add(ex.toString().hashCode() + "", ex);		
		}
		catch(Exception ex2)
		{
			ex.printStackTrace();
			ex2.printStackTrace();
		}
	}

	public static void log(String strLog) 
	{
		Console.writeLine(strLog);
		m_logger.info(strLog);
	}

	private static String getLogFileName() 
	{
		try 
		{
			Date logDate = Calendar.getInstance().getTime();
			DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
			String strLogDate = dateFormat.format(logDate);
			String strLogPath = Config.getConfig(Logger.class)
					.getStr("LogPath");
			String strPath = PathHelper.combinePaths(strLogPath, strLogDate);

			if (!PathHelper.Exists(strPath)) 
			{
				PathHelper.createDir(strPath);
			}

			if(StringHelper.IsNullOrEmpty(LoggerHelper.LogFileName))
			{
				LoggerHelper.LogFileName = Config.getConfig(Logger.class).getStr("LogFile");
			}
			String strLogFile = PathHelper.combinePaths(strPath, 
					LoggerHelper.LogFileName) + ".log";

			if (FileHelper.isFileLocked(strLogFile)) 
			{
				strLogFile += RuntimeHelper.getProcessId();
			}

			int intFileCounter = 0;
			String strOldFileName = strLogFile;
			boolean blnMoveOldFile = false;
			while (FileHelper.exists(strOldFileName)) 
			{
				intFileCounter++;
				String strDir = FileHelper.getDirectory(strLogFile);
				String strName = FileHelper.getName(strLogFile) + "_"
						+ intFileCounter;
				strOldFileName = PathHelper.combinePaths(strDir, strName);
				blnMoveOldFile= true;
			}
			
			if(blnMoveOldFile)
			{
				if(!FileHelper.moveFile(
						strLogFile, strOldFileName))
				{
					strLogFile = strOldFileName + "_tmp";
				}
			}

			return strLogFile;
			
		} 
		catch (Exception ex) 
		{
			System.out.println(ex.getStackTrace());
		}
		
		return "";
	}

	private static void loadLogger(String strLogFile) 
	{
		try 
		{
			m_logger = org.apache.log4j.Logger.getLogger(Logger.class);
			PatternLayout layout = new PatternLayout();
			layout.setConversionPattern("%d [%t] %-5p %c - %m%n");

			FileAppender appender = new FileAppender(layout, strLogFile, false);
			appender.setAppend(true);

			m_logger.addAppender(appender);
			m_logger.setLevel(org.apache.log4j.Level.INFO);
		} 
		catch (Exception ex) 
		{
			System.out.println(ExceptionUtils.getStackTrace(ex));
		}
	}

	public static void log(Exception ex, boolean b) 
	{
		log(ex);
	}

	public static void log(String strMessage, boolean b, boolean c, boolean d) 
	{
		log(strMessage);
	}

	public static void log(String strMessage, boolean b, boolean c) 
	{
		log(strMessage);
	}

	public static void Log(String strMessage) 
	{
		log(strMessage);
	}

	public static void Log(Exception ex) 
	{
		log(ex);
	}
}