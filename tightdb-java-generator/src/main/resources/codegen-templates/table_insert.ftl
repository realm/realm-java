    public ${cursorName} insert(long position<#foreach f in columns>, ${f.paramType} ${f.name}</#foreach>) {
        try {
        <#foreach f in columns><#if f.isSubtable>    insert${f.type}(${f.index}, position);
        <#else>	insert${f.type}(${f.index}, position, ${f.name});
        </#if></#foreach>    insertDone();

            return cursor(position);
        } catch (Exception e) {
            throw new RuntimeException("Error occured while adding/inserting a new row!", e);
        }
    }