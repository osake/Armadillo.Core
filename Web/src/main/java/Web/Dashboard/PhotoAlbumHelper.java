package Web.Dashboard;

import java.io.File;
import java.util.List;

import javax.faces.component.UIComponent;

import org.apache.commons.io.FileUtils;
import org.primefaces.component.galleria.Galleria;
import org.primefaces.component.graphicimage.GraphicImage;

import Armadillo.Core.Console;
import Armadillo.Core.Logger;
import Armadillo.Core.Io.FileHelper;
import Armadillo.Core.Text.StringHelper;
import  Utils.Gui.AUiItem;
import  Utils.Gui.AUiPhotoAlbumItem;
import Web.Base.WebHelper;

public class PhotoAlbumHelper 
{
	public static UIComponent loadPhotoAlbumUiComponent(
			String strTabName,
			DynamicGuiInstanceWrapper tabInstanceWrapper) 
	{
		try
		{
			if(tabInstanceWrapper == null)
			{
				return null;
			}
			AUiPhotoAlbumItem uiItem =  tabInstanceWrapper.getPhotoAlbumUiItem();
			if(uiItem == null)
			{
				return null;
			}
			List<String> fileList = uiItem.generateFileList();
			
			if(fileList == null || fileList.size() == 0)
			{
				return null;
			}
			
			Galleria galleria = new Galleria();
			for(String strFileName : fileList)
			{
				GraphicImage graphicImage = new GraphicImage();
				//strFileName =  "//resources//images//" + strFileName;
				String strName = FileHelper.getName(strFileName);
				if(!FileHelper.exists(strName))
				{
					FileUtils.copyFile(new File(strFileName), new File(strName));
					
					if(!FileHelper.exists(strName))
					{
						Console.WriteLine("File not found [" + strFileName + "]");
						continue;
					}
				}
				
				strFileName = strName;
				String strId = "a_" + StringHelper.CleanString(strName)
						.trim()
						.replace(".", "_")
						.replace(" ", "");
				graphicImage.setId(strId + "_photoAlbum");
				//graphicImage.setCache(false);
//	        	BufferedImage image = ImageIO.read(
//	        			new File(strFileName)); 
//	        	ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
//	        	ImageIO.write(image, "png", baos); 
//	        	byte[] res = baos.toByteArray();
//	        	DefaultStreamedContent defaultStreamedContent = new DefaultStreamedContent(new ByteArrayInputStream(res));	        	
//	        	graphicImage.setValue(defaultStreamedContent);
				//graphicImage.setName(strFileName);
				graphicImage.setValueExpression("value",
						WebHelper.createValueExpression(strFileName,
								String.class));
				graphicImage.setName(strFileName);
				graphicImage.setAlt(strName);
				graphicImage.setTitle(strName);
//				graphicImage.setValueExpression("value",
//					WebHelper.createValueExpression("#{" + strBeanName + ".dynamicImage}",
//							StreamedContent.class));
				galleria.getChildren().add(graphicImage);
			}
			
			galleria.setValueExpression("value",
					WebHelper.createValueExpression("#{dashboardBean.galleriaFiles}",
							List.class));
			return galleria;
			
		} 
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
		return null;
	}

	public static void reloadPhotoAlbum(AUiItem currUiItem) 
	{
		try
		{
			AUiPhotoAlbumItem aUiPhotoAlbumItem = (AUiPhotoAlbumItem)currUiItem;
			aUiPhotoAlbumItem.setFileList(null);
			aUiPhotoAlbumItem.getFileList();
		} 
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
	}
}
