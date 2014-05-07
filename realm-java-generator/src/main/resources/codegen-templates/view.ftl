${java_header}
<#if packageName?has_content>
package ${packageName};
</#if>

import io.realm.*;
import io.realm.typed.*;

/**
 * This class represents a TightDB view and was automatically generated.
 */
public class ${viewName} extends AbstractView<${cursorName}, ${viewName}, ${queryName}> {

<#foreach f in columns><#if f.isSubtable>	public final ${f.type}ViewColumn<${cursorName}, ${viewName}, ${queryName}, ${f.subTableName}> ${f.name} = new ${f.type}ViewColumn<${cursorName}, ${viewName}, ${queryName}, ${f.subTableName}>(${tableName}.TYPES, tableOrView, ${f.index}, "${f.name}", ${f.subTableName}.class);
<#else>	public final ${f.type}ViewColumn<${cursorName}, ${viewName}, ${queryName}> ${f.name} = new ${f.type}ViewColumn<${cursorName}, ${viewName}, ${queryName}>(${tableName}.TYPES, tableOrView, ${f.index}, "${f.name}");
</#if></#foreach>
	public ${viewName}(TableView view) {
		super(${tableName}.TYPES, view);
	}

}
