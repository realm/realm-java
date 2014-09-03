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
import javax.tools.Diagnostic;

import com.squareup.javawriter.JavaWriter;

import io.realm.ColumnType;

public class RealmSourceCodeGenerator {
	private JavaWriter writer = null;
	private HashSet<String> ignoreFields = new HashSet<String>();
	
	private String packageName = null;
	private int state = 0;
	private int fieldIndex = 0;

	String _errorMessage = "";
	
	private void error(String message) 
    {
		_errorMessage = message;
    }
	
	public String getError() 
    {
		return _errorMessage;
    }
	
	private boolean checkState(int checkState)
	{
		if (writer == null)
		{
			error("No output writer has been defined");
			return false;
		}
		
		if (state != checkState)
		{
			error("Annotations received in wrong order");
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
		if (!checkState(0)) return false;
		
		this.packageName = packageName;
		writer.emitPackage(packageName).emitEmptyLine();
		state = 1;
		return true;
	}
	
	public boolean setClassName(String className) throws IOException
	{
		if (!checkState(1)) return false;

		writer.beginType(packageName+"."+className+"RealmProxy", "class", 
		           EnumSet.of(Modifier.PUBLIC,Modifier.FINAL),className).emitEmptyLine();
		state = 2;
		fieldIndex = 0;
		return true;
	}

	public boolean add_Field(String fieldName, Element e) throws IOException
	{
		if (!checkState(2)) return false;

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

		writer.emitAnnotation("Override").beginMethod(originalType, "get"+camelCase, EnumSet.of(Modifier.PUBLIC))
		  .emitStatement(getterStmt)
	      .endMethod();

		writer.emitAnnotation("Override").beginMethod("void", "set"+camelCase, EnumSet.of(Modifier.PUBLIC),
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
		if (!checkState(2)) return false;
		writer.endType();
		writer.close();
		ignoreFields.clear();
		state = 0;
		return true;
	}

}
