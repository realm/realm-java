package com.tightdb;

public class SubTableBase extends TableBase {
	public SubTableBase(long nativePtr){
		super(nativePtr);
	}

	public void close(){
		nativeClose();
	}

	protected native void nativeClose();

	// NOT used as of now. For future reference.
	protected TableBase parentTable;
	protected int columnIndex;
	protected int rowIndex;
}
