    public ${cursorName} insert(long _rowIndex<#foreach f in columns>, ${f.paramType} ${f.name}</#foreach>) {
        try {
        <#foreach f in columns><#if f.isSubtable>    insert${f.type}(${f.index}, _rowIndex, ${f.name});
        <#else>	insert${f.type}(${f.index}, _rowIndex, ${f.name});
        </#if></#foreach>    insertDone();

            return cursor(_rowIndex);
        } catch (Exception e) {
            throw new RuntimeException("Error occured while adding/inserting a new row!", e);
        }
    }