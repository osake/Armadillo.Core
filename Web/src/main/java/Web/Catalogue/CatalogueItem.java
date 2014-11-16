package Web.Catalogue;

import Armadillo.Core.UI.ImageWrapper;

public class CatalogueItem 
{
	public String Id;
	public String Description;
	public ImageWrapper Image;
	
	@Override
	public String toString() 
	{
		return Id;
	}
}
