<#call java_header>()
import com.tightdb.*;
import com.tightdb.lib.*;

/**
 * This class represents a TightDB query and was automatically generated.
 */
public class ${entity}Query extends AbstractQuery<${entity}Query, ${entity}, ${entity}View> {

<#foreach f in columns><#if f.code.attributes.isSubtable>    public final ${f.code.attributes.columnType}QueryColumn<${entity}, ${entity}View, ${entity}Query, ${f.code.attributes.subtype}Table> ${f.name};
<#else>    public final ${f.code.attributes.columnType}QueryColumn<${entity}, ${entity}View, ${entity}Query> ${f.name};
</#if></#foreach>
	public ${entity}Query(TableBase table, TableQuery query) {
		super(${entity}Table.TYPES, table, query);
<#foreach f in columns><#if f.code.attributes.isSubtable>        ${f.name} = new ${f.code.attributes.columnType}QueryColumn<${entity}, ${entity}View, ${entity}Query, ${f.code.attributes.subtype}Table>(${entity}Table.TYPES, table, query, ${f.code.attributes.index}, "${f.name}", ${f.code.attributes.subtype}Table.class);
<#else>        ${f.name} = new ${f.code.attributes.columnType}QueryColumn<${entity}, ${entity}View, ${entity}Query>(${entity}Table.TYPES, table, query, ${f.code.attributes.index}, "${f.name}");
</#if></#foreach>	}

}
