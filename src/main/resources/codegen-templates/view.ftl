${java_header}
<#if packageName?has_content>
package ${packageName};
</#if>

import com.tightdb.*;
import com.tightdb.lib.*;

/**
 * This class represents a TightDB view and was automatically generated.
 */
public class ${entity}View extends AbstractView<${entity}, ${entity}View, ${entity}Query> {

<#foreach f in columns><#if f.isSubtable>	public final ${f.type}RowsetColumn<${entity}, ${entity}View, ${entity}Query, ${f.subtype}Table> ${f.name} = new ${f.type}RowsetColumn<${entity}, ${entity}View, ${entity}Query, ${f.subtype}Table>(${entity}Table.TYPES, rowset, ${f.index}, "${f.name}", ${f.subtype}Table.class);
<#else>	public final ${f.type}RowsetColumn<${entity}, ${entity}View, ${entity}Query> ${f.name} = new ${f.type}RowsetColumn<${entity}, ${entity}View, ${entity}Query>(${entity}Table.TYPES, rowset, ${f.index}, "${f.name}");
</#if></#foreach>
	public ${entity}View(TableViewBase viewBase) {
		super(${entity}Table.TYPES, viewBase);
	}

}
