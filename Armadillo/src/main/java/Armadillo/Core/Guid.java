package Armadillo.Core;

import java.util.UUID;

public class Guid {

	public static Object NewGuid() 
	{
		UUID uuid = UUID.randomUUID();
		return uuid;
	}

}
