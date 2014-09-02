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
	private HashMap<String, Element> _methods = new HashMap<String, Element>();
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
	

	
/***	
	private final String _codeHeader =   "package <+package>;\n"+
								         "\n\n"+
								         "public class <+class>RealmProxy extends <+class> \n"+
								         "{\n"+
								         "    public static final String implName=\"<+class>\";\n\n";

	private final String _fieldTableHeader =   "    private static String[] fieldNames = {";
	private final String _fieldTableFooter =   "};\n"+
	                                           "    public String[] getTableRowNames() {return fieldNames;}\n\n";

	private final String _typeTableHeader =   "    private static int[] fieldTypes = {";
	private final String _typeTableFooter =   "};\n"+
                                              "    public int[] getTableRowTypes() {return fieldTypes;}\n\n";
	private final String _getTableName    =   "    public String getTableName() {return implName;}\n";

	
	
	private final String _codeGetter =   "    final static int <+field>Index = <+index>;\n\n"+
										 "    public <+type> get<+camelField>()\n"+
								         "    {\n"+
                                         "        return <+cast>row.get<+etter_type>(<+field>Index);\n"+
							             "    }\n"+
								         "\n";
	private final String _codeSetter =   "    public void set<+camelField>(<+type> value)\n"+
								         "    {\n"+
                                         "        row.set<+etter_type>(<+field>Index, value);\n"+
								         "    }\n"+
								         "\n";

	private final String _codeFooter =   	
								        "}\n"+
									    "\n";
								   
	public String generateFragment(String fragment) 
	{
		Set<String> keys = _values.keySet();
		Iterator<String> it = keys.iterator();
		
		while (it.hasNext())
		{
			String fieldName = it.next();
			fragment = fragment.replace("<+"+fieldName+">", _values.get(fieldName));
		}
		
		return fragment;
				
	}
	

	public String generateMethod(String fragment, String name, int methodIndex) 
	{
		Element e = _methods.get(name);
		
		String camelCase = Character.toUpperCase(name.charAt(0)) + name.substring(1);
		
		
		
		fragment = fragment.replace("<+index>",Integer.toString(methodIndex));


		fragment = fragment.replace("<+field>",name);
		
		fragment = fragment.replace("<+camelField>",camelCase);

		String fullType = e.asType().toString();
		fragment = fragment.replace("<+type>",fullType);
		
		while (fullType.indexOf('.') >= 0) fullType = fullType.substring(fullType.indexOf('.')+1);

		if (fullType.compareTo("int") == 0 || fullType.compareTo("Integer") == 0)
		{
			fullType = "Long";
			fragment = fragment.replace("<+cast>","(int)");
		}
		else
		{
			fragment = fragment.replace("<+cast>","");
		}
		
		
		if (fullType.compareTo("long") == 0  || fullType.compareTo("float") == 0  || 
		    fullType.compareTo("double") == 0  || fullType.compareTo("long") == 0  || 
		    fullType.compareTo("boolean") == 0)
		{
			fullType = Character.toUpperCase(fullType.charAt(0)) + fullType.substring(1);
		}
		fragment = fragment.replace("<+etter_type>", fullType);

		return fragment;
	}

	public boolean generate() throws Exception
	{
		_bw.append(generateFragment(_codeHeader));
		
		Set<String> keys = _methods.keySet();
		Iterator<String> it = keys.iterator();
		String _fieldTable = "";
		String _typeTable = "";
		
		int fieldIndex = 0;

		while (it.hasNext())
		{
			String fieldName = it.next();
			
			
			if (ignoreFields.contains(fieldName))
			{
				continue;
			}

			_bw.append(generateMethod(_codeGetter, fieldName, fieldIndex));
			_bw.append(generateMethod(_codeSetter, fieldName, fieldIndex));
			
			fieldIndex++;
			
			Element e = _methods.get(fieldName);
			if (_fieldTable.length() > 0) _fieldTable += " ,";
			_fieldTable += "\""+fieldName+"\"";
			
			if (_typeTable.length() > 0) _typeTable += " ,";
			
			if (e.asType().toString().compareTo("java.lang.String") == 0)
			{
				_typeTable += ColumnType.STRING.getValue();
			}
			else if (e.asType().toString().compareTo("int") == 0 || e.asType().toString().compareTo("long") == 0 || 
					 e.asType().toString().compareTo("java.lang.Integer") == 0 || e.asType().toString().compareTo("java.lang.Long") == 0)
			{
				_typeTable += ColumnType.INTEGER.getValue();
			}
			else if (e.asType().toString().compareTo("double") == 0 || e.asType().toString().compareTo("java.lang.Double") == 0)
			{
				_typeTable += ColumnType.DOUBLE.getValue();
			}
			else if (e.asType().toString().compareTo("float") == 0 || e.asType().toString().compareTo("java.lang.Float") == 0)
			{
				_typeTable += ColumnType.FLOAT.getValue();
			}
			else if (e.asType().toString().compareTo("boolean")  == 0 || e.asType().toString().compareTo("java.lang.Boolean") == 0)
			{
				_typeTable += ColumnType.BOOLEAN.getValue();				
			}
			else if (e.asType().toString().compareTo("java.util.Date") == 0)
			{
				_typeTable += ColumnType.DATE.getValue();
			}
//			else if (e.asType().equals(byte[].class) )
//			{
//				_typeTable += ColumnType.BINARY.getValue();				
//			}
			else
			{
				_typeTable += e.asType().toString()+" - "+String.class.toString();				
			}
		}
		
		_bw.append(_fieldTableHeader);
		_bw.append(_fieldTable);
		_bw.append(_fieldTableFooter);

		_bw.append(_typeTableHeader);
		_bw.append(_typeTable);
		_bw.append(_typeTableFooter);
		
		_bw.append(_getTableName);
		
		_bw.append(generateFragment(_codeFooter));
		
		_methods.clear();

		return true;
	}
*/
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

		writer.emitPackage(_values.get("package")).beginType(_values.get("package")+"."+_values.get("class")+"RealmProxy", "class", EnumSet.of(Modifier.PUBLIC,Modifier.FINAL),_values.get("class"));

		Iterator<String> methodNamesIterator = _methods.keySet().iterator();

		while (methodNamesIterator.hasNext())
		{
			String fieldName = methodNamesIterator.next();
			if (ignoreFields.contains(fieldName))
			{
				continue;
			}
			
			Element e = _methods.get(fieldName);
			String fullType =  convertSimpleTypesToObject(e.asType().toString());
			String returnCast = "";
			String camelCase = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
			String fieldId = camelCase+"Index";

			if (fullType.compareTo("Integer") == 0)
			{
				fullType = "Long";
				returnCast ="(int)";
			}
			
			String getterStmt = "return "+returnCast+"row.get"+fullType+"( "+fieldId+" )";

			writer.emitField("int", fieldId, EnumSet.of(Modifier.PUBLIC,Modifier.FINAL,Modifier.STATIC));

			writer.beginMethod(fullType, "get"+camelCase, EnumSet.of(Modifier.PUBLIC))
    		  .emitStatement(getterStmt)
    	      .endMethod();
		}
		
		writer.endType();
		writer.close();
		
		return true;
	}

}
