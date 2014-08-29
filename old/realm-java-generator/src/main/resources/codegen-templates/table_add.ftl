    public ${cursorName} add(<#foreach f in columns><#if (f_index > 0)>, </#if>${f.paramType} ${f.name}</#foreach>) {
        return insert(size()<#foreach f in columns>, ${f.name}</#foreach>);
    }