${java_header}
<#if packageName?has_content>
package ${packageName};
</#if>

import com.tightdb.*;
import com.tightdb.typed.*;

/**
 * This class represents a TightDB query and was automatically generated.
 */
public class ${name} extends AbstractQuery<${name}, ${cursorName}, ${viewName}> {

<#foreach f in columns><#if f.isSubtable>    public final ${f.type}QueryColumn<${cursorName}, ${viewName}, ${name}, ${f.subTableName}> ${f.name};
<#else>    public final ${f.type}QueryColumn<${cursorName}, ${viewName}, ${name}> ${f.name};
</#if></#foreach>
	public ${name}(Table table, TableQuery query) {
		super(${tableName}.TYPES, table, query);
<#foreach f in columns><#if f.isSubtable>        ${f.name} = new ${f.type}QueryColumn<${cursorName}, ${viewName}, ${name}, ${f.subTableName}>(${tableName}.TYPES, table, query, ${f.index}, "${f.name}", ${f.subTableName}.class);
<#else>        ${f.name} = new ${f.type}QueryColumn<${cursorName}, ${viewName}, ${name}>(${tableName}.TYPES, table, query, ${f.index}, "${f.name}");
</#if></#foreach>	}

}
