${java_header}
package ${packageName};

import com.tightdb.*;
import com.tightdb.lib.*;

/**
 * This class represents a TightDB table and was automatically generated.
 */
<#if isNested>public class ${entity}Table extends AbstractSubtable<${entity}, ${entity}View, ${entity}Query> {
<#else>public class ${entity}Table extends AbstractTable<${entity}, ${entity}View, ${entity}Query> {
</#if>
	public static final EntityTypes<${entity}Table, ${entity}View, ${entity}, ${entity}Query> TYPES = new EntityTypes<${entity}Table, ${entity}View, ${entity}, ${entity}Query>(${entity}Table.class, ${entity}View.class, ${entity}.class, ${entity}Query.class); 

<#foreach f in columns><#if f.isSubtable>	public final ${f.type}RowsetColumn<${entity}, ${entity}View, ${entity}Query, ${f.subtype}Table> ${f.name} = new ${f.type}RowsetColumn<${entity}, ${entity}View, ${entity}Query, ${f.subtype}Table>(TYPES, table, ${f.index}, "${f.name}", ${f.subtype}Table.class);
<#else>	public final ${f.type}RowsetColumn<${entity}, ${entity}View, ${entity}Query> ${f.name} = new ${f.type}RowsetColumn<${entity}, ${entity}View, ${entity}Query>(TYPES, table, ${f.index}, "${f.name}");
</#if></#foreach>
<#if isNested>	public ${entity}Table(TableBase subtableBase) {
		super(TYPES, subtableBase);
	}
<#else>	public ${entity}Table() {
		super(TYPES);
	}
	
	public ${entity}Table(Group group) {
		super(TYPES, group);
	}
</#if>
	public static void specifyStructure(TableSpec spec) {
<#foreach f in columns><#if f.isSubtable>        add${f.type}Column(spec, "${f.name}", new ${f.subtype}Table(null));
<#else>        add${f.type}Column(spec, "${f.name}");
</#if></#foreach>    }

${add}

${insert}

}
