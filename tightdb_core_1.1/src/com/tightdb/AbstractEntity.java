package com.tightdb;

public class AbstractEntity {
	protected AbstractEntity(TableBase table, int rowIndex){
		this.table = table;
		this.tableView = null;
		this.rowIndex = rowIndex;
	}
	
	protected AbstractEntity(TableViewBase tableView, int rowIndex){
		this.table = null;
		this.tableView = tableView;
		this.rowIndex = rowIndex;
	}
	protected String getString(int columnIndex){
		if(tableView != null){
			return tableView.getString(columnIndex, rowIndex);
		}
		return table.getString(columnIndex, rowIndex);
	}
	
	protected long getLong(int columnIndex){
		if(tableView != null){
			return tableView.getLong(columnIndex, rowIndex);
		}
		return table.getLong(columnIndex, rowIndex);
	}
	
	protected boolean getBoolean(int columnIndex){
		if(tableView != null){
			return tableView.getBoolean(columnIndex, rowIndex);
		}
		return table.getBoolean(columnIndex, rowIndex);
	}
	
	protected byte[] getBinaryData(int columnIndex){
		if(tableView != null){
			return tableView.getBinaryData(columnIndex, rowIndex);
		}
		return table.getBinaryData(columnIndex, rowIndex);
	}
	
	protected TableBase table;
	protected int rowIndex;
	protected TableViewBase tableView;
}
