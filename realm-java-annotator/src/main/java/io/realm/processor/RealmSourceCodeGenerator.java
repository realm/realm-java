package io.realm.processor;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;

import com.squareup.javawriter.JavaWriter;

import io.realm.ColumnType;

public class RealmSourceCodeGenerator {
	private BufferedWriter _bw;
	private HashMap<String, String> _values = new HashMap<String, String>();
	private HashMap<String, Element> _methods = new LinkedHashMap<String, Element>();
	private HashSet<String> ignoreFields = new HashSet<String>();
	
	public void setBufferedWriter(BufferedWriter bw) 
	{
		_bw = bw;
	}


	public void set_packageName(String packageName) 
	{
		_values.put("package", packageName);
	}
	
	public void set_implements(String name) 
	{
		_values.put("implements", name);
	}

	public void set_className(String className) 
	{
		_values.put("class", className);
	}

	public void add_Field(String fieldName, Element element) 
	{
		_methods.put(fieldName, element);
	}

	public void add_Ignore(String symbolName) 
	{
		ignoreFields.add(symbolName);
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
	
	public boolean generate() throws IOException
	{
		JavaWriter writer = new JavaWriter(_bw);

		writer.emitPackage(_values.get("package")).beginType(_values.get("package")+
				           "."+_values.get("class")+"RealmProxy", "class", 
				           EnumSet.of(Modifier.PUBLIC,Modifier.FINAL),_values.get("class")).emitEmptyLine();

		Iterator<String> methodNamesIterator = _methods.keySet().iterator();
		int fieldIndex = 0;

		while (methodNamesIterator.hasNext())
		{
			String fieldName = methodNamesIterator.next();
			
			// For now ignore fields is not implemented at runtime
//			if (ignoreFields.contains(fieldName))
//			{
//				continue;
//			}
			
			Element e = _methods.get(fieldName);
			String originalType = e.asType().toString();
			String fullType =  convertSimpleTypesToObject(originalType);
			String shortType = fullType.substring(fullType.lastIndexOf(".") + 1);
			
			String returnCast = "";
			String camelCase = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
			String fieldId = "rowIndex"+camelCase;

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

			writer.emitField("int", fieldId, EnumSet.of(Modifier.PRIVATE,Modifier.FINAL,Modifier.STATIC),
					Integer.toString(fieldIndex));
			fieldIndex++;

			writer.beginMethod(originalType, "get"+camelCase, EnumSet.of(Modifier.PUBLIC))
    		  .emitStatement(getterStmt)
    	      .endMethod();

			writer.beginMethod("void", "set"+camelCase, EnumSet.of(Modifier.PUBLIC),
					originalType, "value")
  		       .emitStatement(setterStmt)
  	           .endMethod().emitEmptyLine();
			
		}
		
		writer.endType();
		writer.close();
		
		_values.clear();
		_methods.clear();
		ignoreFields.clear();

		
		return true;
	}

}
