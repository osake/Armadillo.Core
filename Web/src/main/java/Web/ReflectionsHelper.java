package Web;

import java.util.Set;

import org.junit.Test;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import Armadillo.Core.Console;

public class ReflectionsHelper 
{
	public static void main(String[] args)
	{
		new ReflectionsHelper().doTest();
	}
	
	@Test
	public void doTest()
	{
		String basePackageName = "Web.a";
		Reflections reflections = new Reflections(new ConfigurationBuilder()
        .setScanners(new SubTypesScanner(false /* don't exclude Object.class */), new ResourcesScanner())
        .setUrls(ClasspathHelper.forPackage(basePackageName))
        /* and maybe */
        .filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix(basePackageName))));
        
		Set<Class<? extends Object>> classSet = reflections.getSubTypesOf(Object.class);
		Console.WriteLine(classSet);
		
//	      Reflections reflections = new Reflections("Web");
//	     Set<Class<? extends AUiItem>> subtypes = reflections.getSubTypesOf(AUiItem.class);
//		 Console.WriteLine("" + subtypes.size());

		
//		 List<ClassLoader> classLoadersList = new LinkedList<ClassLoader>();
//         classLoadersList.add(ClasspathHelper.contextClassLoader());
//         classLoadersList.add(ClasspathHelper.staticClassLoader());
//                        
//         Reflections reflections = new Reflections(new ConfigurationBuilder()
//             .setScanners(new SubTypesScanner(false /* don't exclude Object.class */), new ResourcesScanner())
//             .setUrls(ClasspathHelper.forClassLoader(classLoadersList.toArray(
//            		 new ClassLoader[] {ReflectionsHelper.class})))); /*forPackage(basePackageName))); */
//             /* and maybe */
//             /*.filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix(basePackageName))));*/
//         
//         Set<Class<? extends Object>> classSet = reflections.getSubTypesOf(Object.class);		
		

		
//		ClassPath ClassPath = new ClassPath();
//		var topLevelClasses = ClassPath.getTopLevelClassesRecursive("Web");
		
		//	      Reflections reflections = new Reflections(new ConfigurationBuilder()
//          .setUrls(ClasspathHelper.forPackage("Web"))
//          .setScanners(new ResourcesScanner()));
	      
//        Reflections reflections = new Reflections(new ConfigurationBuilder()
//        .addUrls(ClasspathHelper.forPackage("your.package.here"),
//                 ClasspathHelper.forClass(Entity.class), 
//                 ClasspathHelper.forClass(Module.class))
//        .setScanners(new ResourcesScanner(), 
//                     new TypeAnnotationsScanner(), 
//                     new SubTypesScanner()));
        
	}
}
