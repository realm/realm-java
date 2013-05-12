${java_header}
<#if packageName?has_content>
package ${packageName};
</#if>

import com.tightdb.*;
import com.tightdb.typed.*;

/**
 * This class represents a TightDB table and was automatically generated.
 */
<#if isNested>public class ${tableName} extends AbstractSubtable<${cursorName}, ${viewName}, ${queryName}> {
<#else>public class ${tableName} extends AbstractTable<${cursorName}, ${viewName}, ${queryName}> {
</#if>
	public static final EntityTypes<${tableName}, ${viewName}, ${cursorName}, ${queryName}> TYPES = new EntityTypes<${tableName}, ${viewName}, ${cursorName}, ${queryName}>(${tableName}.class, ${viewName}.class, ${cursorName}.class, ${queryName}.class);

<#foreach f in columns><#if f.isSubtable>	public final ${f.type}TableColumn<${cursorName}, ${viewName}, ${queryName}, ${f.subTableName}> ${f.name} = new ${f.type}TableColumn<${cursorName}, ${viewName}, ${queryName}, ${f.subTableName}>(TYPES, table, ${f.index}, "${f.name}", ${f.subTableName}.class);
<#else>	public final ${f.type}TableColumn<${cursorName}, ${viewName}, ${queryName}> ${f.name} = new ${f.type}TableColumn<${cursorName}, ${viewName}, ${queryName}>(TYPES, table, ${f.index}, "${f.name}");
</#if></#foreach>
<#if isNested>	public ${tableName}(Table subtable) {
		super(TYPES, subtable);
	}
<#else>	public ${tableName}() {
		super(TYPES);
	}

	public ${tableName}(Group group) {
		super(TYPES, group);
	}
</#if>
	public static void specifyStructure(TableSpec spec) {
<#foreach f in columns><#if f.isSubtable>        add${f.type}Column(spec, "${f.name}", new ${f.subTableName}(null));
<#else>        add${f.type}Column(spec, "${f.name}");
</#if></#foreach>    }

${add}

${insert}

}
