${java_header}
<#if packageName?has_content>
package ${packageName};
</#if>

import com.tightdb.*;
import com.tightdb.lib.*;

/**
 * This class represents a TightDB cursor and was automatically generated.
 */
public class ${name} extends AbstractCursor<${name}> {

<#foreach f in columns><#if f.isSubtable>    public final ${f.type}CursorColumn<${name}, ${viewName}, ${queryName}, ${f.subCursorName}, ${f.subTableName}> ${f.name};
<#else>    public final ${f.type}CursorColumn<${cursorName}, ${viewName}, ${queryName}> ${f.name};
</#if></#foreach>
	public ${cursorName}(TableOrViewBase tableOrView, long position) {
		super(${tableName}.TYPES, tableOrView, position);

<#foreach f in columns><#if f.isSubtable>        ${f.name} = new ${f.type}CursorColumn<${cursorName}, ${viewName}, ${queryName}, ${f.subCursorName}, ${f.subTableName}>(${tableName}.TYPES, this, ${f.index}, "${f.name}", ${f.subTableName}.class);
<#else>        ${f.name} = new ${f.type}CursorColumn<${cursorName}, ${viewName}, ${queryName}>(${tableName}.TYPES, this, ${f.index}, "${f.name}");
</#if></#foreach>	}

<#foreach f in columns><#if f.isSubtable>	public ${f.subTableName} get${f.name?cap_first}() {
<#else>	public ${f.fieldType} get${f.name?cap_first}() {
</#if>		return this.${f.name}.get();
	}

<#if f.isSubtable>	public void set${f.name?cap_first}(${f.subTableName} ${f.name}) {
<#else>	public void set${f.name?cap_first}(${f.fieldType} ${f.name}) {
</#if>		this.${f.name}.set(${f.name});
	}

</#foreach>	@Override
	public AbstractColumn<?, ?, ?, ?>[] columns() {
		return getColumnsArray(<#foreach f in columns>${f.name}<#if f_has_next>, </#if></#foreach>);
	}

}
