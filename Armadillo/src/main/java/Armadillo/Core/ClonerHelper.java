package Armadillo.Core;

import java.io.Serializable;

import org.apache.commons.lang.SerializationUtils;


public class ClonerHelper {

	public static Object Clone(Object objValue) {
		return SerializationUtils.clone((Serializable)objValue);
	}

}
