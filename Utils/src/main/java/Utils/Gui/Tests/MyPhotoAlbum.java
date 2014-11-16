package Utils.Gui.Tests;

import java.util.List;

import Armadillo.Analytics.TextMining.Searcher;
import Utils.Gui.AUiPhotoAlbumItem;

public class MyPhotoAlbum extends AUiPhotoAlbumItem
{
	@Override
	public String getDir() 
	{
		return "C:/HC.Java/workspace/HC.Products.Web.Gch/WebContent/resources/images";
	}

	@Override
	public String getReportTitle() 
	{
		return "My photo album";
	}

	@Override
	public String[] getReportTreeLabels() 
	{
		return new String[] 
				{
					"Test",
					"PhotoAlbum",
					"PhotoAlbum"
				};
	}

	@Override
	protected Class<?> getParamsClass() 
	{
		return null;
	}

	@Override
	protected Searcher generateSearcher() 
	{
		return null;
	}

	@Override
	protected List<String> generateKeys() 
	{
		return null;
	}
}
