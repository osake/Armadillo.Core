package Armadillo.Core.Reflection;

import java.util.Set;

import org.reflections.Reflections;

import Armadillo.Core.Logger;

public class ClassHelper {
	
	
	public static <T> Set<Class<? extends T>> getSubTypes(Class<T> classToBe){
		
		try{
		     Reflections reflections = new Reflections("HC");
		     Set<Class<? extends T>> subTypes = 
		               reflections.getSubTypesOf(classToBe);
		     return subTypes;
		}
		catch(Exception ex){
			Logger.log(ex);
		}
		return null;
	}
}
