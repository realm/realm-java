<#call java_header>()
import com.tightdb.*;
import com.tightdb.lib.*;

/**
 * This class represents a TightDB view and was automatically generated.
 */
public class ${entity}View extends AbstractView<${entity}, ${entity}View, ${entity}Query> {

<#foreach f in columns><#if f.code.attributes.isSubtable>	public final ${f.code.attributes.columnType}RowsetColumn<${entity}, ${entity}View, ${entity}Query, ${f.code.attributes.subtype}Table> ${f.name} = new ${f.code.attributes.columnType}RowsetColumn<${entity}, ${entity}View, ${entity}Query, ${f.code.attributes.subtype}Table>(${entity}Table.TYPES, rowset, ${f.code.attributes.index}, "${f.name}", ${f.code.attributes.subtype}Table.class);
<#else>	public final ${f.code.attributes.columnType}RowsetColumn<${entity}, ${entity}View, ${entity}Query> ${f.name} = new ${f.code.attributes.columnType}RowsetColumn<${entity}, ${entity}View, ${entity}Query>(${entity}Table.TYPES, rowset, ${f.code.attributes.index}, "${f.name}");
</#if></#foreach>
	public ${entity}View(TableViewBase viewBase) {
		super(${entity}Table.TYPES, viewBase);
	}

}
