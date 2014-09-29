package Armadillo.Core.Io;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;

import Armadillo.Core.Logger;

public class PathHelper {

	public static void createDir(String strDir) {
		File file = new File(strDir);
		if (!file.exists()) {
			file.mkdir();
		}
	}

	public static boolean Exists(String strDir) {
		File file = new File(strDir);
		return file.exists() && file.isDirectory();
	}

	public static String combinePaths(String... paths) 
	{
		try
		{
			if (paths.length == 0) 
			{
				return "";
			}
	
			File combined = new File(paths[0]);
			int i = 1;
			while (i < paths.length) {
				combined = new File(combined, paths[i]);
				++i;
			}
	
			return combined.getPath();
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return "";
	}

	public static List<String> getFileList(
			String strDir,
			String[] filter,
			boolean blnRecursive,
			boolean onlyName) 
	{
		try
		{
			if(!Exists(strDir))
			{
				return new ArrayList<String>();
			}
			Collection<File> fileCollection = FileUtils.listFiles(new File(strDir), filter, blnRecursive);
			ArrayList<String> fileList = new ArrayList<String>();
			for(File file : fileCollection)
			{
				if(onlyName)
				{
					fileList.add(file.getName());
				}
				else
				{
					fileList.add(file.getAbsolutePath());
				}
			}
			return fileList;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return new ArrayList<String>();
	}
}
