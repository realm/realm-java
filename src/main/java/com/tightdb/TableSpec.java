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
	
	public TableSpec addSubtableColumn(String name){
		ColumnInfo columnInfo = new ColumnInfo(ColumnType.ColumnTypeTable, name);
		columnInfos.add(columnInfo);
		return columnInfo.tableSpec;
	}
	
	public TableSpec getSubtableSpec(long columnIndex){
		return columnInfos.get((int)columnIndex).tableSpec;
	}

	public long getColumnCount(){
		return columnInfos.size();
	}
	
	public ColumnType getColumnType(long columnIndex){
		return columnInfos.get((int)columnIndex).type;
	}
	
	public String getColumnName(long columnIndex){
		return columnInfos.get((int)columnIndex).name;
	}
	
	public long getColumnIndex(String name){
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
