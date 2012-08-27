${java_header}
<#if packageName?has_content>
package ${packageName};
</#if>

import com.tightdb.*;
import com.tightdb.lib.*;

/**
 * This class represents a TightDB cursor and was automatically generated.
 */
public class ${entity} extends AbstractCursor<${entity}> {

<#foreach f in columns><#if f.isSubtable>    public final ${f.type}CursorColumn<${entity}, ${entity}View, ${entity}Query, ${f.subtype}, ${f.subtype}Table> ${f.name};
<#else>    public final ${f.type}CursorColumn<${entity}, ${entity}View, ${entity}Query> ${f.name};
</#if></#foreach>
	public ${entity}(IRowsetBase rowset, long position) {
		super(${entity}Table.TYPES, rowset, position);

<#foreach f in columns><#if f.isSubtable>        ${f.name} = new ${f.type}CursorColumn<${entity}, ${entity}View, ${entity}Query, ${f.subtype}, ${f.subtype}Table>(${entity}Table.TYPES, this, ${f.index}, "${f.name}", ${f.subtype}Table.class);
<#else>        ${f.name} = new ${f.type}CursorColumn<${entity}, ${entity}View, ${entity}Query>(${entity}Table.TYPES, this, ${f.index}, "${f.name}");
</#if></#foreach>	}

<#foreach f in columns><#if f.isSubtable>	public ${f.subtype}Table get${f.name?cap_first}() {
<#else>	public ${f.fieldType} get${f.name?cap_first}() {
</#if>		return this.${f.name}.get();
	}

<#if f.isSubtable>	public void set${f.name?cap_first}(${f.subtype}Table ${f.name}) {
<#else>	public void set${f.name?cap_first}(${f.fieldType} ${f.name}) {
</#if>		this.${f.name}.set(${f.name});
	}

</#foreach>	@Override
	public AbstractColumn<?, ?, ?, ?>[] columns() {
		return getColumnsArray(<#foreach f in columns>${f.name}<#if f_has_next>, </#if></#foreach>);
	}

}
