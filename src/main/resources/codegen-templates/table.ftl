<#call java_header>()
import com.tightdb.*;
import com.tightdb.lib.*;

/**
 * This class represents a TightDB table and was automatically generated.
 */
<#if isNested>public class ${entity}Table extends AbstractSubtable<${entity}, ${entity}View, ${entity}Query> {
<#else>public class ${entity}Table extends AbstractTable<${entity}, ${entity}View, ${entity}Query> {
</#if>
	public static final EntityTypes<${entity}Table, ${entity}View, ${entity}, ${entity}Query> TYPES = new EntityTypes<${entity}Table, ${entity}View, ${entity}, ${entity}Query>(${entity}Table.class, ${entity}View.class, ${entity}.class, ${entity}Query.class); 

<#foreach f in columns><#if f.code.attributes.isSubtable>	public final ${f.code.attributes.columnType}RowsetColumn<${entity}, ${entity}View, ${entity}Query, ${f.code.attributes.subtype}Table> ${f.name} = new ${f.code.attributes.columnType}RowsetColumn<${entity}, ${entity}View, ${entity}Query, ${f.code.attributes.subtype}Table>(TYPES, table, ${f.code.attributes.index}, "${f.name}", ${f.code.attributes.subtype}Table.class);
<#else>	public final ${f.code.attributes.columnType}RowsetColumn<${entity}, ${entity}View, ${entity}Query> ${f.name} = new ${f.code.attributes.columnType}RowsetColumn<${entity}, ${entity}View, ${entity}Query>(TYPES, table, ${f.code.attributes.index}, "${f.name}");
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
	@Override
	protected void specifyStructure(TableSpec spec) {
<#foreach f in columns><#if f.code.attributes.isSubtable>        add${f.code.attributes.columnType}Column(spec, "${f.name}", new ${f.code.attributes.subtype}Table(null));
<#else>        add${f.code.attributes.columnType}Column(spec, "${f.name}");
</#if></#foreach>    }

<#call class_members>()

}
