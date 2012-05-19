package com.tightdb;

import java.io.File;
import java.io.IOException;

/**
 * This class is used to serialize tables to either disk or memory. It consists
 * a collection of tables. 
 * 
 * We can see this class as a database in RDBMS sense. We keep a collection of 
 * table by their names as key.
 * 
 * @author acer
 *
 */
public class Group {
	
	public Group(){
		this(createNative());
	}
	
	protected Group(long nativePtr){
		this.nativePtr = nativePtr;
	}
	
	public boolean isValid(){
		return nativeIsValid(nativePtr);
	}
	
	//TODO to be implemented.
	protected native boolean nativeIsValid(long nativeGroupPtr);
	
	public int getTableCount(){
		return nativeGetTableCount(nativePtr);
	}

	protected native int nativeGetTableCount(long nativeGroupPtr);

	/**
	 * Checks whether table exists in the Group.
	 * 
	 * @param name The name of the table.
	 * @return true if the table exists, otherwise false.
	 */
	public boolean hasTable(String name){
		if(name == null)
			return false;
		return nativeHasTable(nativePtr, name);
	}
	
	protected native boolean nativeHasTable(long nativeGroupPtr, String name);

	public String getTableName(int index){
		if(index < 0 || index >= getTableCount()){
			throw new IndexOutOfBoundsException("Table index argument is out of range. possible range is [0, tableCount - 1]");
		}
		return nativeGetTableName(nativePtr, index);
	}
	
	protected native String nativeGetTableName(long nativeGroupPtr, int index);

	/**
	 * Returns a table with the specified name.
	 * 
	 * @param name The name of the table.
	 * @return The table if it exists, otherwise null.
	 */
	public TableBase getTable(String name){
		if(hasTable(name)){
			return new TableBase(nativeGetTableNativePtr(nativePtr, name));
		}
		return null;
	}
	
	protected native long nativeGetTableNativePtr(long nativeGroupPtr, String name);
	/**
	 * Loads the group (or a group of tables) from the input data. The data can 
	 * not be an arbitrary string. It must be created from the string created from 
	 * the {@code Group} writeToMemory method.
	 * 
	 * @param data The serialized table.
	 * @return The group (of tables).
	 */
	public static Group loadData(byte[] data){
		if(data == null || data.length == 0){
			throw new IllegalArgumentException("input data is not in correct format.");
		}
		return new Group(nativeLoadData(data));
	}
	
	/**
	 * Loads a file and create a group loaded from the file data. The returned group
	 * contains all the tables that are already stored into the group before writting 
	 * the file with the writeToFile method.
	 * 
	 * @param file A File object representing the file.
	 * @return The group (of tables).
	 */
	public static Group load(File file){
		return new Group(nativeLoadFile(file.getAbsolutePath()));
	}
	
	protected native static long nativeLoadFile(String fileName);
	
	public static Group load(String fileName){
		if(fileName == null){
			throw new NullPointerException("Null filename");
		}
		File file = new File(fileName);
		if(!file.exists()){
			throw new IllegalArgumentException("file does not exists");
		}
		if(file.isDirectory()){
			throw new IllegalArgumentException("Invalid filename its a directory only");
		}
		return load(file);
	}
	
	/**
	 * Writes the table to the specific file in the disk.
	 * 
	 * @param fileName The file of the file.
	 * @throws IOException
	 */
	public void writeToFile(String fileName) throws IOException{
		if(fileName == null)
			throw new NullPointerException("file name is null");
		File file = new File(fileName);
		writeToFile(file);
	}
	
	protected native void nativeWriteToFile(long nativeGroupPtr, String fileName) throws Exception;
	
	/**
	 * Writes the table to the specific file in the disk.
	 *
	 * @param file A File object representing the file.
	 * @throws IOException
	 */
	public void writeToFile(File file) throws IOException{
		if(!file.exists()){
			file.createNewFile();
		}
		try{
			nativeWriteToFile(nativePtr, file.getAbsolutePath());
		}catch(Exception ex){
			throw new IOException(ex.getMessage());
		}		
	}
	
	protected static native long nativeLoadData(byte[] buffer);
	/**
	 * Writes the table as a string which can be used for other purpose.
	 * 
	 * @return Binary array of the serialized group.
	 */
	public byte[] writeToMem(){
		return nativeWriteToMem(nativePtr);
	}
	
	protected native byte[] nativeWriteToMem(long nativeGroupPtr);
	
	protected static native long createNative();
	
	protected long nativePtr;
}
