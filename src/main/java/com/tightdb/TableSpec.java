package com.tightdb;

import java.util.ArrayList;
import java.util.List;

public class TableSpec {
	public class ColumnInfo {
		public ColumnInfo(ColumnType type, String name){
			this.name = name;
			this.type = type;
			this.tableSpec = null;
			if(this.type == ColumnType.ColumnTypeTable){
				this.tableSpec = new TableSpec();
			}
		}
		protected ColumnType type;
		protected String name;
		protected TableSpec tableSpec;
	}
	
	public TableSpec(){
		columnInfos = new ArrayList<ColumnInfo>();
	}

	public void addColumn(ColumnType type, String name){
		columnInfos.add(new ColumnInfo(type, name));
	}
	
	public TableSpec addColumnTable(String name){
		ColumnInfo columnInfo = new ColumnInfo(ColumnType.ColumnTypeTable, name);
		columnInfos.add(columnInfo);
		return columnInfo.tableSpec;
	}
	
	public TableSpec getTableSpec(int columnIndex){
		return columnInfos.get(columnIndex).tableSpec;
	}

	public int getColumnCount(){
		return columnInfos.size();
	}
	
	public ColumnType getColumnType(int columnIndex){
		return columnInfos.get(columnIndex).type;
	}
	
	public String getColumnName(int columnIndex){
		return columnInfos.get(columnIndex).name;
	}
	
	public int getColumnIndex(String name){
		for(int i=0; i< columnInfos.size(); i++){
			ColumnInfo columnInfo = columnInfos.get(i);
			if(columnInfo.name.equals(name)){
				return i;
			}
		}
		return -1;
	}
	
	private List<ColumnInfo> columnInfos;
}
