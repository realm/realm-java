package com.tightdb;


public class TableQuery {
	public TableQuery(TableBase table){
		
	}
	
	public TableQuery equals(int columnIndex, long value){
		return null;
	}
	
	public TableQuery notEquals(int columnIndex, long value){
		return null;
	}
	
	public TableQuery greater(int columnIndex, long value){
		return null;
	}
	
	public TableQuery greaterEqual(int columnIndex, long value){
		return null;
	}
	
	public TableQuery lessThanEqualTo(int columnIndex, long value){
		return null;
	}
	
	public TableQuery lessThan(int columnIndex, long value){
		return null;
	}
	
	public TableQuery between(int columnIndex, long value1, long value2){
		return null;
	}
	
	public TableQuery equals(int columnIndex, boolean value){
		return null;
	}
	
	// strings
	public TableQuery equals(int columnIndex, String value, boolean caseSensitive){
		return null;
	}
	
	public TableQuery equals(int columnIndex, String value){
		return equals(columnIndex, value, true);
	}
	
	public TableQuery beginsWith(int columnIndex, String value, boolean caseSensitive){
		return null;
	}
	
	public TableQuery beginsWith(int columnIndex, String value){
		return beginsWith(columnIndex, value, true);
	}
	
	public TableQuery endsWith(int columnIndex, String value, boolean caseSensitive){
		return null;
	}
	
	public TableQuery endsWith(int columnIndex, String value){
		return beginsWith(columnIndex, value, true);
	}
	
	public TableQuery contains(int columnIndex, String value, boolean caseSensitive){
		return null;
	}
	
	public TableQuery contains(int columnIndex, String value){
		return beginsWith(columnIndex, value, true);
	}

	public TableQuery notEqual(int columnIndex, String value, boolean caseSensitive){
		return null;
	}
	
	public TableQuery notEqual(int columnIndex, String value){
		return beginsWith(columnIndex, value, true);
	}

	// TODO there are some methods which takes table as an input.
	public TableQuery group(){
		return null;
	}
	
	public TableQuery endGroup(){
		return null;
	}
	
	public TableQuery or(){
		return null;
	}
	
	public TableViewBase doQuery(){
		return null;
	}
}
