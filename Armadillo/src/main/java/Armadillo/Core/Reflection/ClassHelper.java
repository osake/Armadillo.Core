package Armadillo.Core.Reflection;

import java.util.ArrayList;
import java.util.Set;

import org.reflections.Reflections;

import Armadillo.Core.Logger;

public class ClassHelper {
	
	
	@SuppressWarnings("unchecked")
	public static <T> Set<Class<? extends T>> getSubTypes(Class<T> classToBe){
		
		try
		{
			
		     Reflections reflections = new Reflections("Armadillo");
		     Set<Class<? extends T>> subTypes = 
		               reflections.getSubTypesOf(classToBe);
		     
		     reflections = new Reflections("HC");
		     subTypes.addAll(
		               reflections.getSubTypesOf(classToBe));
		     
//		     reflections = new Reflections("Web.Tests");
//		     subTypes.addAll(
//		               reflections.getSubTypesOf(classToBe));
		     
		     reflections = new Reflections("Utils");
		     subTypes.addAll(
		               reflections.getSubTypesOf(classToBe));
		     
		     reflections = new Reflections("Analytics");
		     subTypes.addAll(
		               reflections.getSubTypesOf(classToBe));

		     //
		     // find old way
		     //
				ArrayList<Class<?>> classSet = null;
				try {
					classSet = ClassFinder.getClassesForPackage("Web");
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				for(Class<? extends Object> item : classSet)
				{
					if(classToBe.isAssignableFrom(item))
					{
						subTypes.add((Class<? extends T>) item);
					}
				}
		     
		     return subTypes;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return null;
	}
}
