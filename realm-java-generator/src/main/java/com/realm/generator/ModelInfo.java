package com.realm.generator;

public class ModelInfo {

    private final String tableName;

    private final String cursorName;

    private final String viewName;

    private final String queryName;

    public ModelInfo(String tableName, String cursorName, String viewName,
            String queryName) {
        this.tableName = tableName;
        this.cursorName = cursorName;
        this.viewName = viewName;
        this.queryName = queryName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getCursorName() {
        return cursorName;
    }

    public String getViewName() {
        return viewName;
    }

    public String getQueryName() {
        return queryName;
    }

}
