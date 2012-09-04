    public ${cursorName} insert(long position, <#foreach f in columns><#if !f.isSubtable><#if (f_index > 0)>, </#if>${f.originalType} ${f.name}</#if></#foreach>) {
        try {
        <#foreach f in columns><#if f.isSubtable>	insert${f.type}(${f.index}, position);
        <#else>	insert${f.type}(${f.index}, position, ${f.name});
        </#if></#foreach>   insertDone();

            return cursor(position);
        } catch (Exception e) {
            throw new RuntimeException("Error occured while adding/inserting a new row!", e);
        }
    }