${java_header}
<#if packageName?has_content>
package ${packageName};
</#if>

import com.tightdb.*;
import com.tightdb.lib.*;

/**
 * This class represents a TightDB view and was automatically generated.
 */
public class ${viewName} extends AbstractView<${cursorName}, ${viewName}, ${queryName}> {

<#foreach f in columns><#if f.isSubtable>	public final ${f.type}RowsetColumn<${cursorName}, ${viewName}, ${queryName}, ${f.subTableName}> ${f.name} = new ${f.type}RowsetColumn<${cursorName}, ${viewName}, ${queryName}, ${f.subTableName}>(${tableName}.TYPES, rowset, ${f.index}, "${f.name}", ${f.subTableName}.class);
<#else>	public final ${f.type}RowsetColumn<${cursorName}, ${viewName}, ${queryName}> ${f.name} = new ${f.type}RowsetColumn<${cursorName}, ${viewName}, ${queryName}>(${tableName}.TYPES, rowset, ${f.index}, "${f.name}");
</#if></#foreach>
	public ${viewName}(TableViewBase viewBase) {
		super(${tableName}.TYPES, viewBase);
	}

}
