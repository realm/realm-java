    public ${entity} add(<#foreach f in columns><#if !f.isSubtable><#if (f_index > 0)>, </#if>${f.originalType} ${f.name}</#if></#foreach>) {
        try {
            long position = size();

		<#foreach f in columns><#if f.isSubtable>	insert${f.type}(${f.index}, position);
		<#else>	insert${f.type}(${f.index}, position, ${f.name});
		</#if></#foreach>   insertDone();

            return cursor(position);
        } catch (Exception e) {
            throw addRowException(e);
        }
    }