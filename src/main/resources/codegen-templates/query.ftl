${java_header}
<#if packageName?has_content>
package ${packageName};
</#if>

import com.tightdb.*;
import com.tightdb.lib.*;

/**
 * This class represents a TightDB query and was automatically generated.
 */
public class ${entity}Query extends AbstractQuery<${entity}Query, ${entity}, ${entity}View> {

<#foreach f in columns><#if f.isSubtable>    public final ${f.type}QueryColumn<${entity}, ${entity}View, ${entity}Query, ${f.subtype}Table> ${f.name};
<#else>    public final ${f.type}QueryColumn<${entity}, ${entity}View, ${entity}Query> ${f.name};
</#if></#foreach>
	public ${entity}Query(TableBase table, TableQuery query) {
		super(${entity}Table.TYPES, table, query);
<#foreach f in columns><#if f.isSubtable>        ${f.name} = new ${f.type}QueryColumn<${entity}, ${entity}View, ${entity}Query, ${f.subtype}Table>(${entity}Table.TYPES, table, query, ${f.index}, "${f.name}", ${f.subtype}Table.class);
<#else>        ${f.name} = new ${f.type}QueryColumn<${entity}, ${entity}View, ${entity}Query>(${entity}Table.TYPES, table, query, ${f.index}, "${f.name}");
</#if></#foreach>	}

}
