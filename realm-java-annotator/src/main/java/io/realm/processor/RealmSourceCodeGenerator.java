package io.realm.processor;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.Element;

import com.squareup.javawriter.JavaWriter;


public class RealmSourceCodeGenerator {
	private JavaWriter writer = null;
	private HashSet<String> ignoreFields = new HashSet<String>();
	private String packageName = null;
	private String className = null;
	private GeneratorStates generatorState = GeneratorStates.PACKAGE;
	private String _errorMessage = "";
	private ArrayList<String> fields = new ArrayList<String>();
	

	private enum GeneratorStates 
	{
		PACKAGE,
		CLASS,
		METHODS,
	};
	
	
	private void setError(String message) 
    {
		_errorMessage = message;
    }
	
	public String getError() 
    {
		return _errorMessage;
    }
	
	private String convertSimpleTypesToObject(String typeName)
	{
		if (typeName.compareTo("int") == 0)
		{
			typeName = "Integer";
		}
		else if (typeName.compareTo("long") == 0  || typeName.compareTo("float") == 0  || 
				typeName.compareTo("double") == 0  || typeName.compareTo("boolean") == 0)
		{
			typeName = Character.toUpperCase(typeName.charAt(0)) + typeName.substring(1);
		}
		return typeName;
	}
	
	private boolean checkState(GeneratorStates checkState)
	{
		if (writer == null)
		{
			setError("No output writer has been defined");
			return false;
		}
		
		if (generatorState != checkState)
		{
			setError("Annotations received in wrong order");
			return false;
		}
		return true;
	}
	
	public void setBufferedWriter(BufferedWriter bw) 
	{
		writer = new JavaWriter(bw);
	}

	public boolean setPackageName(String packageName) throws IOException
	{
		if (!checkState(GeneratorStates.PACKAGE)) return false;
		
		this.packageName = packageName;
		writer.emitPackage(packageName).emitEmptyLine().emitImports("java.lang.reflect.Field");
		generatorState = GeneratorStates.CLASS;
		return true;
	}
	
	public boolean setClassName(String className) throws IOException
	{
		if (!checkState(GeneratorStates.CLASS)) return false;
		
		this.className = className;

		writer.beginType(packageName+"."+className+"RealmProxy", "class", 
		           EnumSet.of(Modifier.PUBLIC,Modifier.FINAL),className).emitEmptyLine();
		generatorState = GeneratorStates.METHODS;
		return true;
	}

	public boolean setField(String fieldName, Element e) throws IOException
	{
		if (!checkState(GeneratorStates.METHODS)) return false;

		String originalType = e.asType().toString();
		String fullType =  convertSimpleTypesToObject(originalType);
		String shortType = fullType.substring(fullType.lastIndexOf(".") + 1);
		
		String returnCast = "";
		String camelCaseFieldName = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
		String fieldId = "index_"+fieldName;

		if (originalType.compareTo("int") == 0)
		{
			fullType = "long";
			shortType = "Long";
			returnCast ="("+originalType+")";
		}
		
		if (shortType.compareTo("Integer") == 0)
		{
			fullType = "long";
			shortType = "Long";
			returnCast ="(int)";
		}
		
		String getterStmt = "return "+returnCast+"row.get"+shortType+"( "+fieldId+" )";
		
		String setterStmt = "row.set"+shortType+"( "+fieldId+", value )";

		writer.emitField("int", fieldId, EnumSet.of(Modifier.PRIVATE/*,Modifier.FINAL*/,Modifier.STATIC));

		fields.add(fieldId);
		
		writer.emitAnnotation("Override").beginMethod(originalType, "get"+camelCaseFieldName, EnumSet.of(Modifier.PUBLIC))
		  .emitStatement(getterStmt)
	      .endMethod();

		writer.emitAnnotation("Override").beginMethod("void", "set"+camelCaseFieldName, EnumSet.of(Modifier.PUBLIC),
				originalType, "value")
		       .emitStatement(setterStmt)
	           .endMethod().emitEmptyLine();
		return true;

	}

	public boolean add_Ignore(String symbolName) 
	{
		ignoreFields.add(symbolName);		
		return true;
	}
	
	
	public boolean generate() throws IOException
	{
		if (!checkState(GeneratorStates.METHODS)) return false;
		
		writer.beginInitializer(true).emitStatement("Field[] fields="+className+".class.getDeclaredFields()").emitStatement("int i = 0");
		
		writer.beginControlFlow("for (Field f : fields)");
		
		for (String field : fields)
		{
			String fieldName = field.substring("index_".length());
			writer.beginControlFlow("if (f.getName().compareTo(\"%s\") == 0)",fieldName)
			        .emitStatement("%s = i", field).endControlFlow();
		}
		writer.emitStatement("++i;");
		writer.endControlFlow();
		writer.endInitializer();
		writer.endType();
		writer.close();
		
		ignoreFields.clear();
		fields.clear();

		generatorState = GeneratorStates.PACKAGE;
		return true;
	}

}
