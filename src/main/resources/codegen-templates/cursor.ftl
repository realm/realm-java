<#call java_header>()
import com.tightdb.*;
import com.tightdb.lib.*;

/**
 * This class represents a TightDB cursor and was automatically generated.
 */
public class ${entity} extends AbstractCursor<${entity}> {

<#foreach f in columns><#if f.code.attributes.isSubtable>    public final ${f.code.attributes.columnType}CursorColumn<${entity}, ${entity}View, ${entity}Query, ${f.code.attributes.subtype}, ${f.code.attributes.subtype}Table> ${f.name};
<#else>    public final ${f.code.attributes.columnType}CursorColumn<${entity}, ${entity}View, ${entity}Query> ${f.name};
</#if></#foreach>
	public ${entity}(IRowsetBase rowset, long position) {
		super(${entity}Table.TYPES, rowset, position);

<#foreach f in columns><#if f.code.attributes.isSubtable>        ${f.name} = new ${f.code.attributes.columnType}CursorColumn<${entity}, ${entity}View, ${entity}Query, ${f.code.attributes.subtype}, ${f.code.attributes.subtype}Table>(${entity}Table.TYPES, this, ${f.code.attributes.index}, "${f.name}", ${f.code.attributes.subtype}Table.class);
<#else>        ${f.name} = new ${f.code.attributes.columnType}CursorColumn<${entity}, ${entity}View, ${entity}Query>(${entity}Table.TYPES, this, ${f.code.attributes.index}, "${f.name}");
</#if></#foreach>	}

<#foreach f in columns><#if f.code.attributes.isSubtable>	public ${f.code.attributes.subtype}Table get${f.name.capitalized}() {
<#else>	public ${f.type.canonicalName} get${f.name.capitalized}() {
</#if>		return this.${f.name}.get();
	}

<#if f.code.attributes.isSubtable>	public void set${f.name.capitalized}(${f.code.attributes.subtype}Table ${f.name}) {
<#else>	public void set${f.name.capitalized}(${f.type.canonicalName} ${f.name}) {
</#if>		this.${f.name}.set(${f.name});
	}

</#foreach>	@Override
	public AbstractColumn<?, ?, ?, ?>[] columns() {
		return getColumnsArray(<#call list>(${columns}, '${it.name}', ', '));
	}

}
