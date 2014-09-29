package Armadillo.Core.Io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLDecoder;

import Armadillo.Core.Logger;
import Armadillo.Core.Text.TextHelper;

public class FileHelper {

	public static boolean isFileLocked(String strFileName) {
		try {
			if (!exists(strFileName)) {
				return false;
			}
			File file = new File(strFileName);
			boolean fileIsNotLocked = file.renameTo(file);
			return !fileIsNotLocked;
		} catch (Exception ex) {
			System.out.println(ex.getStackTrace());
		}
		return true;
	}

	public static boolean exists(String strFileName) 
	{
		File file = new File(strFileName);
		return file.exists() && !file.isDirectory();
	}

	public static boolean existsDir(String strFileName) 
	{
		File file = new File(strFileName);
		return file.exists() && file.isDirectory();
	}
	
	public static String getExecutionLocation() 
	{
		try 
		{
			String path = FileHelper.class.getProtectionDomain()
					.getCodeSource().getLocation().getPath();
			String decodedPath = URLDecoder.decode(path, "UTF-8");
			return decodedPath;
		} 
		catch (Exception ex) 
		{
			System.out.println(ex);
		}
		return "";
	}

	public static String getExtension(String strFileName) 
	{
		String strExtension = "";

		int i = strFileName.lastIndexOf('.');
		int p = Math.max(strFileName.lastIndexOf('/'),
				strFileName.lastIndexOf('\\'));

		if (i > p) 
		{
			strExtension = strFileName.substring(i + 1);
		}
		return strExtension;
	}

	public static String getDirectory(String strFileName) 
	{
		return org.apache.commons.io.FilenameUtils.getFullPath(strFileName);
	}

	public static String getName(String strFileName) 
	{
		return new File(strFileName).getName();
	}

	public static String cleanFileName(String strFileName) 
	{
		strFileName = TextHelper.replaceCaseInsensitive(strFileName, "con",
				"coen_");

		strFileName = strFileName.replace("&", "").replace("|", "")
				.replace("*", "").replace("?", "").replace("<", "")
				.replace(">", "").replace(";", "").replace(",", "")
				.replace("=", "").replace("*", "").replace('\b' + "", "")
				.replace('\t' + "", "").replace('\n' + "", "")
				.replace('\f' + "", "").replace('\r' + "", "");
		return strFileName;
	}

	public static String getDriveLetter(String strFileName) 
	{
		try
		{
			strFileName = strFileName.replace("/", "\\");
			return getDriveName(strFileName).replace(":\\", "");
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return "";
	}
	
    public static String getDriveName(String strPath)
    {
    	try
    	{
	        int intPathLength = strPath.length();
	        String strOutPath = "";
	        for (int i = 0; i < intPathLength; i++)
	        {
	            char currentChar = strPath.charAt(i);
	            strOutPath = strOutPath + currentChar;
	            if (currentChar == "\\".charAt(0))
	            {
	                return strOutPath;
	            }
	        }
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return "";
    }

	public static boolean  moveFile(String strOldFile, String strNewFile) 
	{
		try
		{
    		 
     	   File afile =new File(strOldFile);
     	   return afile.renameTo(new File(strNewFile));
  
     	}
		catch(Exception e)
		{
     		Logger.log(e);
     	}
     	return false;
	}

	public static boolean delete(String strFileName) 
	{
		try
		{
			if(!exists(strFileName))
			{
				return true;
			}
			
			boolean blnIsDeleted = new File(strFileName).delete();
			return blnIsDeleted;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return false;
	}

	public static void saveTextToFile(
			String string,
			String strFileName) {
		
			BufferedWriter writer = null;
			try
			{
			    writer = new BufferedWriter( new FileWriter( strFileName));
			    writer.write( string);
	
			}
			catch ( IOException ex)
			{
				Logger.log(ex);
			}
			finally
			{
			    try
			    {
			        if ( writer != null)
			        writer.close( );
			    }
			    catch ( IOException ex)
			    {
					Logger.log(ex);
			    }
			}
		}

	public static void checkDirectory(String strFileName) 
	{
		try
		{
			String strDir = getDirectory(strFileName);
			if(!existsDir(strDir))
			{
				new File(strDir).mkdirs();
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}	
}
