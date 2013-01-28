    public ${cursorName} insert(long position<#foreach f in columns>, ${f.paramType} ${f.name}</#foreach>) {
        try {
        	doInsert(position<#foreach f in columns>, ${f.name}</#foreach>);

            return cursor(position);
        } catch (Exception e) {
            throw new RuntimeException("Error occured while adding/inserting a new row!", e);
        }
    }