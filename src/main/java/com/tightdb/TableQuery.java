package com.tightdb;


public class TableQuery {
	public TableQuery(TableBase table){
		this.nativePtr = createNativePtr();
	}
	
	public TableQuery equals(int columnIndex, long value){
		nativeEquals(columnIndex, value);
		return this;
	}
	
	protected native void nativeEquals(int columnIndex, long value);
	
	public TableQuery notEquals(int columnIndex, long value){
		nativeNotEquals(columnIndex, value);
		return this;
	}
	
	protected native void nativeNotEquals(int columnIndex, long value);
		
	public TableQuery greater(int columnIndex, long value){
		nativeGreater(columnIndex, value);
		return this;
	}
	protected native void nativeGreater(int columnIndex, long value);
	
	public TableQuery greaterEqual(int columnIndex, long value){
		nativeGreaterEqual(columnIndex, value);
		return this;
	}
	protected native void nativeGreaterEqual(int columnIndex, long value);
	
	public TableQuery lessThanEqualTo(int columnIndex, long value){
		nativeLessThanEqualTo(columnIndex, value);
		return this;
	}
	protected native void nativeLessThanEqualTo(int columnIndex, long value);
	
	public TableQuery lessThan(int columnIndex, long value){
		nativeLessThan(columnIndex, value);
		return this;
	}
	protected native void nativeLessThan(int columnIndex, long value);
	
	public TableQuery between(int columnIndex, long value1, long value2){
		nativeBetween(columnIndex, value1, value2);
		return this;
	}
	protected native void nativeBetween(int columnIndex, long value1, long value2);
	
	public TableQuery equals(int columnIndex, boolean value){
		nativeEquals(columnIndex, value);
		return this;
	}
	protected native void nativeEquals(int columnIndex, boolean value);
	
	// strings
	public TableQuery equals(int columnIndex, String value, boolean caseSensitive){
		nativeEquals(columnIndex, value, caseSensitive);
		return this;
	}
	protected native void nativeEquals(int columnIndex, String value, boolean caseSensitive);
	
	public TableQuery equals(int columnIndex, String value){
		return equals(columnIndex, value, true);
	}
	
	public TableQuery beginsWith(int columnIndex, String value, boolean caseSensitive){
		nativeBeginsWith(columnIndex, value, caseSensitive);
		return this;
	}
	protected native void nativeBeginsWith(int columnIndex, String value, boolean caseSensitive);
	
	
	public TableQuery beginsWith(int columnIndex, String value){
		return beginsWith(columnIndex, value, true);
	}
	
	public TableQuery endsWith(int columnIndex, String value, boolean caseSensitive){
		nativeEndsWith(columnIndex, value, caseSensitive);
		return this;
	}
	protected native void nativeEndsWith(int columnIndex, String value, boolean caseSensitive);
	
	public TableQuery endsWith(int columnIndex, String value){
		return endsWith(columnIndex, value, true);
	}
	
	public TableQuery contains(int columnIndex, String value, boolean caseSensitive){
		nativeContains(columnIndex, value, caseSensitive);
		return this;
	}
	
	protected native void nativeContains(int columnIndex, String value, boolean caseSensitive);
	
	public TableQuery contains(int columnIndex, String value){
		return contains(columnIndex, value, true);
	}

	public TableQuery notEqual(int columnIndex, String value, boolean caseSensitive){
		nativeNotEqual(columnIndex, value, caseSensitive);
		return this;
	}
	protected native void nativeNotEqual(int columnIndex, String value, boolean caseSensitive);
	
	public TableQuery notEqual(int columnIndex, String value){
		return notEqual(columnIndex, value, true);
	}

	// TODO there are some methods which takes table as an input.
	public TableQuery startGroup(){
		nativeGroup();
		return this;
	}
	protected native void nativeGroup();
	
	public TableQuery endGroup(){
		nativeEndGroup();
		return this;
	}
	protected native void nativeEndGroup();
	
	public TableQuery or(){
		nativeOr();
		return this;
	}
	
	TableViewBase findAll(TableBase tableBase, int start, int end, int limit){
		return new TableViewBase(tableBase, nativeFindAll(tableBase, start, end, limit));
	}
	
	protected native long nativeFindAll(TableBase tableBase, int start, int end, int limit);
	
	protected native void nativeOr();
	
	protected native long createNativePtr();
	
	protected long nativePtr;
}
