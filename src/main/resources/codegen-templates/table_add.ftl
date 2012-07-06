try {
	long position = size();

<#foreach f in columns><#if f.code.attributes.isSubtable>	insert${f.code.attributes.columnType}(${f.code.attributes.index}, position);
<#else>	insert${f.code.attributes.columnType}(${f.code.attributes.index}, position, ${f.name});
</#if></#foreach>	insertDone();

	return cursor(position);
} catch (Exception e) {
	throw addRowException(e);
}
