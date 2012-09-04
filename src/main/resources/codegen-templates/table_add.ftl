    public ${cursorName} add(<#foreach f in columns><#if !f.isSubtable><#if (f_index > 0)>, </#if>${f.originalType} ${f.name}</#if></#foreach>) {
        return insert(size()<#foreach f in columns><#if !f.isSubtable>, ${f.name}</#if></#foreach>);
    }