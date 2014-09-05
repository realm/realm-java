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

package io.realm;

import java.util.ArrayList;
import java.util.List;

public class TableSpec {

    public static class ColumnInfo {

        protected final ColumnType type;
        protected final String name;
        protected final TableSpec tableSpec;

        public ColumnInfo(ColumnType type, String name) {
            this.name = name;
            this.type = type;
            this.tableSpec = (type == ColumnType.TABLE) ? new TableSpec() : null;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            result = prime * result + ((tableSpec == null) ? 0 : tableSpec.hashCode());
            result = prime * result + ((type == null) ? 0 : type.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ColumnInfo other = (ColumnInfo) obj;
            if (name == null) {
                if (other.name != null)
                    return false;
            } else if (!name.equals(other.name))
                return false;
            if (tableSpec == null) {
                if (other.tableSpec != null)
                    return false;
            } else if (!tableSpec.equals(other.tableSpec))
                return false;
            if (type != other.type)
                return false;
            return true;
        }
    }

    private List<ColumnInfo> columnInfos;

    public TableSpec() {
        columnInfos = new ArrayList<ColumnInfo>();
    }

    public void addColumn(ColumnType type, String name) {
        if (name.length() > 63) {
            throw new IllegalArgumentException("Column names are currently limited to max 63 characters.");
        }
        columnInfos.add(new ColumnInfo(type, name));
    }

    protected void addColumn(int colTypeIndex, String name) {
        addColumn(ColumnType.fromNativeValue(colTypeIndex), name);
    }

    public TableSpec addSubtableColumn(String name) {
        if (name.length() > 63) {
            throw new IllegalArgumentException("Column names are currently limited to max 63 characters.");
        }
        ColumnInfo columnInfo = new ColumnInfo(ColumnType.TABLE, name);
        columnInfos.add(columnInfo);
        return columnInfo.tableSpec;
    }

    public TableSpec getSubtableSpec(long columnIndex) {
        return columnInfos.get((int) columnIndex).tableSpec;
    }

    public long getColumnCount() {
        return columnInfos.size();
    }

    public ColumnType getColumnType(long columnIndex) {
        return columnInfos.get((int) columnIndex).type;
    }

    public String getColumnName(long columnIndex) {
        return columnInfos.get((int) columnIndex).name;
    }

    public long getColumnIndex(String name) {
        for (int i = 0; i < columnInfos.size(); i++) {
            ColumnInfo columnInfo = columnInfos.get(i);
            if (columnInfo.name.equals(name)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((columnInfos == null) ? 0 : columnInfos.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TableSpec other = (TableSpec) obj;
        if (columnInfos == null) {
            if (other.columnInfos != null)
                return false;
        } else if (!columnInfos.equals(other.columnInfos))
            return false;
        return true;
    }

}
