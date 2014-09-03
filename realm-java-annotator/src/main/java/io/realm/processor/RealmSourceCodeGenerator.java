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
import java.util.Locale;
import java.util.Set;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.Element;

import com.squareup.javawriter.JavaWriter;


public class RealmSourceCodeGenerator {
	private class FieldInfo
	{
		public String name;
		public String columnType;
		
		public FieldInfo(String name,String columnType)
		{
			this.name = name;
			this.columnType = columnType;
		}
	};
	
	private enum GeneratorStates 
	{
		PACKAGE,
		CLASS,
		METHODS,
	};
	

	private JavaWriter writer = null;
	private Set<String> ignoreFields = new HashSet<String>();
	private String packageName = null;
	private String className = null;
	private GeneratorStates generatorState = GeneratorStates.PACKAGE;
	private String errorMessage = "";
	private List<FieldInfo> fields = new ArrayList<FieldInfo>();
	

	
	private void setError(String message) 
    {
		errorMessage = message;
    }
	
	public String getError() 
    {
		return errorMessage;
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
	
	private String convertTypesToColumnType(String typeName)
	{
		if (typeName.compareTo("String") == 0)
		{
			typeName = "ColumnType.STRING";
		}
		else if (typeName.compareTo("Long") == 0  || typeName.compareTo("Integer") == 0)
		{
			typeName = "ColumnType.INTEGER";
		}
		else if (typeName.compareTo("Float") == 0)
		{
			typeName = "ColumnType.FLOAT";
		}
		else if (typeName.compareTo("Double") == 0)
		{
			typeName = "ColumnType.DOUBLE";
		}
		else if (typeName.compareTo("Boolean") == 0)
		{
			typeName = "ColumnType.BOOLEAN";
		}
		else if (typeName.compareTo("Date") == 0)
		{
			typeName = "ColumnType.DATE";
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
		writer.emitPackage(packageName).emitEmptyLine().
		       emitImports("io.realm.ColumnType","io.realm.Table").emitEmptyLine();
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

		fields.add(new FieldInfo(fieldId,convertTypesToColumnType(shortType)));
		
		writer.emitAnnotation("Override").beginMethod(originalType, "get"+camelCaseFieldName, EnumSet.of(Modifier.PUBLIC))
		  .emitStatement(getterStmt)
	      .endMethod();

		writer.emitAnnotation("Override").beginMethod("void", "set"+camelCaseFieldName, EnumSet.of(Modifier.PUBLIC),
				originalType, "value")
		       .emitStatement(setterStmt)
	           .endMethod().emitEmptyLine();
		return true;

	}
	
	
	public boolean generateComment(String comment) throws IOException
	{
		if (writer == null)
		{
			setError("No output writer has been defined");
			return false;
		}
		
		writer.emitSingleLineComment(comment);
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

		writer.beginMethod("Table", "initTable", EnumSet.of(Modifier.PUBLIC,Modifier.STATIC),
				"io.realm.ImplicitTransaction", "transaction").
				beginControlFlow("if(!transaction.hasTable(\""+this.className+"\"))").
				emitStatement("Table table = transaction.getTable(\""+this.className+"\")");

		for (int index=0;index<fields.size();++index)
		{
			FieldInfo field = fields.get(index);
			String fieldName = field.name.substring("index_".length());
			writer.emitStatement(field.name+"  = "+Integer.toString(index));
			writer.emitStatement("table.addColumn( %s, \"%s\" )", field.columnType, fieldName.toLowerCase(Locale.getDefault()));
		}
		
		writer.emitStatement("return table");
		writer.endControlFlow();
		writer.emitStatement("return transaction.getTable(\""+this.className+"\")");
		writer.endMethod().emitEmptyLine();

		writer.endType();
		writer.close();
		
		ignoreFields.clear();
		fields.clear();

		generatorState = GeneratorStates.PACKAGE;
		return true;
	}
	
	

}
