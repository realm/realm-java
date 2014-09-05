/*
 * Copyright 2014 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.realm.generator;

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
