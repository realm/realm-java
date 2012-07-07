try {
<#foreach f in columns><#if f.isSubtable>	insert${f.type}(${f.index}, position);
<#else>	insert${f.type}(${f.index}, position, ${f.name});
</#if></#foreach>	insertDone();

	return cursor(position);
} catch (Exception e) {
	throw insertRowException(e);
}
