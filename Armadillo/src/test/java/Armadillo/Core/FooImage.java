package Armadillo.Core;

import Armadillo.Core.UI.ImageWrapper;

public class FooImage extends Foo
{
	public ImageWrapper Image;
	
	@Override
	public String toString() 
	{
		String str = super.toString() + "_" + m_j + "_" +  m_d;
		return str;
	}
}
