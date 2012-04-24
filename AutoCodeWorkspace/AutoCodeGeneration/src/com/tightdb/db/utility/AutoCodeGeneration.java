package com.tightdb.db.utility;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

@SuppressWarnings("unchecked")
public class AutoCodeGeneration implements IConstants
{

	protected static StringBuilder autoCode = new StringBuilder();
	protected static Properties props = new Properties();

	{

		InputStream inputStream = null;

		try
		{

			inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(DEFAULT_PROPERTY_FILE);
			props.load(inputStream);

		}
		catch (Exception e)
		{
			System.out.println(" - Error reading '" + DEFAULT_PROPERTY_FILE + "' file: " + e);
			e.printStackTrace();

		}
		finally
		{

			if (inputStream != null)
			{
				try
				{
					inputStream.close();
				}
				catch (IOException e)
				{
				}
			}
		}

	}

	private String GENERATEDPKGPREFIX = getProperty(POSTFIX_FOR_GENERATED_PKG);

	public AutoCodeGeneration()
	{
	}

	public String getProperty(String key)
	{
		String value = props.getProperty(key);
		return (value == null) ? null : value.trim();
	}

	public boolean getBooleanProperty(String key)
	{
		String value = this.getProperty(key);
		if (value != null && (value.equals("true") || value.equals("1")))
		{
			return true;
		}
		return false;
	}

	public int getIntegerProperty(String key)
	{
		String value = this.getProperty(key);
		if (value != null)
			return Integer.parseInt(value);
		else
			return Integer.MIN_VALUE;
	}

	@SuppressWarnings({ "unchecked", "unused" })
	private static void findClasses(List<Class> classes, File directory, String packageName) throws ClassNotFoundException
	{
		if (!directory.exists())
		{
			System.out.println("Directory does not exists :: " + directory);
			return;
		}
		File[] files = directory.listFiles();
		for (File file : files)
		{
			if (file.isDirectory())
			{
				assert !file.getName().contains(".");
				findClasses(classes, file, packageName);
			}
			else if (file.getName().endsWith(".java"))
			{
				String fileName = file.getName();
				String filePath = file.getAbsolutePath();

				filePath = filePath.substring(filePath.indexOf("src") + 4);
				filePath = filePath.replace("\\", ".");
				filePath = filePath.replaceAll(".java", "");
				String className = filePath;
				if (className.startsWith(packageName))
				{
					classes.add(Class.forName(className));
				}

			}
		}

	}

	private File getPackageDirectory(String pckgname) throws URISyntaxException
	{
		ClassLoader cld = Thread.currentThread().getContextClassLoader();
		if (cld == null)
		{
			throw new IllegalStateException("Can't get class loader.");
		}

		URL resource = cld.getResource(pckgname.replace('.', '/'));
		if (resource == null)
		{
			throw new RuntimeException("Package " + pckgname + " not found on classpath.");
		}

		return new File(resource.toURI());
	}

	public void findPkgAndSubPkgs(List<String> lstPkgs, String scanPkgName) throws URISyntaxException
	{
		File pkgDir = getPackageDirectory(scanPkgName);

		if (scanPkgName.contains(GENERATEDPKGPREFIX))
		{
			return;
		}
		lstPkgs.add(scanPkgName);

		if (pkgDir.exists())
		{
			File[] lstSubDirs = pkgDir.listFiles();
			if (lstSubDirs != null)
			{
				for (File file : lstSubDirs)
				{
					if (file.isDirectory())
					{
						findPkgAndSubPkgs(lstPkgs, scanPkgName + "." + file.getName());
					}
				}
			}
		}
	}

	public Class[] getClassesInPackage(String pckgname) throws URISyntaxException
	{
		File directory = getPackageDirectory(pckgname);
		if (!directory.exists())
		{
			throw new IllegalArgumentException("Could not get directory resource for package " + pckgname + ".");
		}

		return getClassesInPackage(pckgname, directory);
	}

	private Class[] getClassesInPackage(String pckgname, File directory)
	{
		List<Class> classes = new ArrayList<Class>();
		for (String filename : directory.list())
		{
			if (filename.endsWith(".class"))
			{
				String classname = buildClassname(pckgname, filename);
				try
				{
					classes.add(Class.forName(classname));
				}
				catch (ClassNotFoundException e)
				{
					System.err.println("Error creating class " + classname);
				}
			}
		}
		return classes.toArray(new Class[classes.size()]);
	}

	private static String buildClassname(String pckgname, String filename)
	{
		return pckgname + '.' + filename.replace(".class", "");
	}

	@SuppressWarnings("unused")
	public void generatedAutoCode()
	{

		String fileName = "";
		String packageName = "";
		int noOfPackages = 0;
		String basePath = "";
		try
		{
			noOfPackages = getIntegerProperty(NO_OF_PKG);
			basePath = getProperty(BASE_PATH);

			System.out.println("Base path :: " + basePath);

			if (noOfPackages > 0 && !basePath.equals(""))
			{
				for (int i = 0; i < noOfPackages; i++)
				{
					String scanPkgName = getProperty("PKG" + (i + 1));
					System.out.println("Name of " + i + "th package ::" + scanPkgName);

					List<String> lstPkgs = new ArrayList<String>();
					findPkgAndSubPkgs(lstPkgs, scanPkgName);

					System.out.println("Packages to scan :: " + lstPkgs);

					ArrayList<Class> allClasses = new ArrayList<Class>();

					for (String pkg : lstPkgs)
					{
						Class[] clzs = getClassesInPackage(pkg);
						if (clzs != null)
						{
							allClasses.addAll(Arrays.asList(clzs));
						}
					}

					// findClasses(allClasses, new File(basePath), scanPkgName);

					System.out.println("All Classes ::" + allClasses);

					allClasses = removeUnwantedClasses(allClasses);
					System.out.println("Classes only which have annotation::" + allClasses);

					if (allClasses != null && !allClasses.isEmpty())
					{
						for (Class cls : allClasses)
						{

							autoCode = new StringBuilder();

							fileName = cls.getSimpleName();
							packageName = cls.getPackage().getName();

							// Generate Package Name
							autoCode.append("package " + packageName + "." + GENERATEDPKGPREFIX).append(";").append(NEW_LINE);

							// Generate Class Name and Variables
							generateClassNameAndDefinedVariable(cls);

							Field[] fields = cls.getDeclaredFields();

							for (Field field : fields)
							{
								String fieldName = field.getName();
								String fieldType = field.getType().getSimpleName();
								System.out.println("Name  " + fieldName + "\tTye " + fieldType);
								generateGetter(fieldName, fieldType, autoCode);
								generateSetter(fieldName, fieldType, autoCode);
							}

							autoCode.append(CLOSE_CURLY_BRACKET).append(NEW_LINE);

							String postFix = getProperty(POSTFIX_FOR_CLASSNAME);

							String path = (packageName + "." + GENERATEDPKGPREFIX).replaceAll("\\.", "/");
							File baseFolder = new File(basePath + "src/" + path);
							if (!baseFolder.exists())
							{
								baseFolder.mkdirs();
							}
							File sourceFile = new File(baseFolder, cls.getSimpleName() + postFix + ".java");

							System.out.println("File Path::" + sourceFile);
							writeStringToFile(sourceFile, autoCode.toString());
						}
					}

				}
			}
			else
			{
				System.out.println("No classes define in property file.");
			}

		}
		catch (Exception e)
		{
			System.out.println("Error ::" + e);
			e.printStackTrace();
		}

	}

	private void writeStringToFile(File srcFile, String content) throws IOException
	{
		BufferedWriter wr = new BufferedWriter(new FileWriter(srcFile));
		wr.write(content);
		wr.close();
	}

	private ArrayList<Class> removeUnwantedClasses(ArrayList<Class> classes)
	{
		ArrayList<Class> tmpClasses = new ArrayList<Class>();
		try
		{
			String annotation = getProperty("Annotation.Intefaces");

			if (classes != null && !classes.isEmpty())
			{
				for (Class orgClass : classes)
				{
					Annotation[] classArray = orgClass.getAnnotations();
					if (classArray != null)
					{
						for (Annotation annoted : classArray)
						{
							if (annoted.annotationType().getSimpleName().equals(annotation))
							{
								tmpClasses.add(orgClass);
							}
						}
					}
				}

			}
			System.out.println("list of class::" + tmpClasses);
		}
		catch (Exception e)
		{
			System.out.println("Error while removing unwanted classes");
			e.printStackTrace();
		}
		return tmpClasses;
	}

	@SuppressWarnings("unused")
	private void generateClassNameAndDefinedVariable(Class c)
	{

		String INTF = "";
		String EXTN = "";
		String CLASS_TMPLT = " public class #className# #interfce# ";

		String postFix = getProperty(POSTFIX_FOR_CLASSNAME);

		CLASS_TMPLT = CLASS_TMPLT.replaceAll("#className#", c.getSimpleName() + postFix);

		Class[] intfs = c.getInterfaces();

		int i = 0;
		if (intfs != null && intfs.length > 0)
		{
			for (Class tmpIntfs : intfs)
			{
				autoCode.append("import ").append(tmpIntfs.getName()).append(";").append(NEW_LINE);

				if (tmpIntfs.isInterface())
				{
					if (i > 0)
						INTF += ",";
					INTF = tmpIntfs.getSimpleName();
				}
				else
				{
					EXTN = tmpIntfs.getSimpleName();
				}
				i++;
			}

			if (INTF.endsWith(""))
			{
				CLASS_TMPLT = CLASS_TMPLT.replaceAll("#interfce#", "implements " + INTF);
			}
			else
			{
				CLASS_TMPLT = CLASS_TMPLT.replaceAll("#interfce#", "extends " + EXTN);
			}
		}

		CLASS_TMPLT = CLASS_TMPLT.replaceAll("#interfce#", "");

		autoCode.append(CLASS_TMPLT).append(START_CURLY_BRACKET);
		autoCode.append(NEW_LINE);

		Field fieldlist[] = c.getDeclaredFields();
		for (Field fld : fieldlist)
		{
			String VAR_TMPLT = " #modifiers# #type# #name# ; ";
			VAR_TMPLT = VAR_TMPLT.replaceAll("#type#", fld.getType().getSimpleName());
			VAR_TMPLT = VAR_TMPLT.replaceAll("#name#", fld.getName());
			int mod = fld.getModifiers();
			VAR_TMPLT = VAR_TMPLT.replaceAll("#modifiers#", Modifier.toString(mod));
			autoCode.append(TAB_SPACE).append(VAR_TMPLT).append(NEW_LINE);
		}
	}

	@SuppressWarnings("unused")
	private void generateSetter(String name, String dataType, StringBuilder autoCode)
	{
		String SETTER_TEMPLATE = "\tpublic void #settername# (#dataType# #name#) {\n\t this.#name# = #name# ; \n\t}";

		String setterName = name.replace(name.charAt(0), name.toUpperCase().charAt(0));

		setterName = SET + setterName;

		SETTER_TEMPLATE = SETTER_TEMPLATE.replaceAll("#settername#", setterName);
		SETTER_TEMPLATE = SETTER_TEMPLATE.replaceAll("#name#", name);
		SETTER_TEMPLATE = SETTER_TEMPLATE.replaceAll("#dataType#", dataType);
		autoCode.append(SETTER_TEMPLATE);
		autoCode.append(NEW_LINE);
		autoCode.append(NEW_LINE);
	}

	@SuppressWarnings("unused")
	private void generateGetter(String name, String dataType, StringBuilder autoCode)
	{
		String GETTER_TEMPLATE = "\tpublic #returnType# #gettername# () {\n\t return this.#name# ; \n\t}";

		String getterName = name.replace(name.charAt(0), name.toUpperCase().charAt(0));

		if (dataType.equalsIgnoreCase("Boolean"))
		{
			getterName = IS + getterName;
		}
		else
		{
			getterName = GET + getterName;
		}

		GETTER_TEMPLATE = GETTER_TEMPLATE.replaceAll("#gettername#", getterName);
		GETTER_TEMPLATE = GETTER_TEMPLATE.replaceAll("#name#", name);
		GETTER_TEMPLATE = GETTER_TEMPLATE.replaceAll("#returnType#", dataType);
		autoCode.append(GETTER_TEMPLATE);
		autoCode.append('\n');
		autoCode.append('\n');
	}

}
