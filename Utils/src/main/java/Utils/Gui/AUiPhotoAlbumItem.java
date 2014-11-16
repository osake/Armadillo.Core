package Utils.Gui;

import java.util.ArrayList;
import java.util.List;

import Armadillo.Core.Logger;
import Armadillo.Core.Io.PathHelper;
import Armadillo.Core.Text.StringHelper;

public abstract class AUiPhotoAlbumItem extends AUiItem
{
	private final Object m_lockObj = new Object();
	private List<String> m_fileList;
	
	public String getDir()
	{
		return "";
	}
	
	public List<String> getFileList()
	{
		try
		{
			if(m_fileList == null)
			{
				synchronized(m_lockObj)
				{
					if(m_fileList == null)
					{
						m_fileList = generateFileList();
						if(m_fileList == null)
						{
							m_fileList = new ArrayList<String>();
						}
					}
				}
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return m_fileList;
	}
	
	public List<String> generateFileList()
	{
		try
		{
			String strDir = getDir();
			if(StringHelper.IsNullOrEmpty(strDir))
			{
				return new ArrayList<String>();
			}
			List<String> fileList = PathHelper.getFileList(
					strDir,
					new String[] 
							{
								"jpg",
								"png",
								"bmp",
								"tif"
							},
					true,
					false);
			return fileList;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return new ArrayList<String>();
	}

	public void setFileList(List<String> object) 
	{
		m_fileList = object;
	}
}
