package com.tightdb.performance;

import com.almworks.sqlite4java.*;

public class SQLite implements PerformanceTest {

	private SQLiteConnection db = null;
	private SQLiteStatement stmt = null;
    
    private void error(SQLiteException e) {
    	System.out.println("SQL error");
    	db.dispose();
		e.printStackTrace();
    }
    
    public SQLite() {
    	db = new SQLiteConnection();
    	try {
    		db.open(true);
    		// Create table
    		db.exec("create table t1 (indexInt INTEGER, string VARCHAR(100), byteInt INTEGER, smallInt INTEGER);");
    	} catch (SQLiteException e) {
    		error(e);
    	}
    }
    
    public void buildTable(int rows) {
    	SQLiteStatement stmt;
		try {
			stmt = db.prepare("INSERT INTO t1 VALUES(?1, ?2, ?3, ?4);", true);
			for (int i = 0; i < rows; ++i) {
			    // create random string
			    int n = Util.getRandNumber();
			    String s = Util.getNumberString(n);
			    
			    stmt.reset();
		        stmt.bind(1, n);
		        stmt.bind(2, s);
		        stmt.bind(3, 1);
		        stmt.bind(4, 2);
		        stmt.step();
			}
	    	stmt.dispose();
		} catch (SQLiteException e) {
			error(e);
		}
    }
    
    //--------------- small Int

    public void begin_findSmallInt(int value) {
    	try {
			stmt = db.prepare("SELECT * FROM t1 WHERE smallInt=?1;", true);
			stmt.bind(1, value);
    	} catch (SQLiteException e) {
    		error(e);
		}
    }
    
    public boolean findSmallInt(int value) {
    	try {
			stmt.reset();
			return stmt.step();
    	} catch (SQLiteException e) {
			error(e);
		}
		return false;	
    }
    
    public void end_findSmallInt() {
    	stmt.dispose();
    }
 
    //--------------- byte Int
    
 	public void begin_findByteInt(int value) {
 		try {
 			stmt = db.prepare("SELECT * FROM t1 WHERE byteInt=?1;", true);
 			stmt.bind(1, value);
 		} catch (SQLiteException e) {
 			error(e);
 		}
 	}

     public boolean findByteInt(int value) {
    	 try {
    		 stmt.reset();
    		 return stmt.step();
    	 } catch (SQLiteException e) {
  			error(e);
  		 }
         return false;
     }
     
     public void end_findByteInt() {
    	 stmt.dispose();
     }
     
     //---------------- string
     
     public void begin_findString(String value) {
    	 try {
	    	 stmt = db.prepare("SELECT * FROM t1 WHERE string=?1;", true);
	     	 stmt.bind(1, value);
    	 } catch (SQLiteException e) {
    		 error(e);
    	 }
     }
     
     public boolean findString(String value) {
    	 try {
    		 stmt.reset();
    		 return stmt.step();
    	 } catch (SQLiteException e) {
    		 error(e);
    	 }
         return false;	
     }
     
     public void end_findString() {
    	 stmt.dispose();
     }
     
     //---------------- int with index
     
     public boolean addIndex() {
    	 try {
	    	 stmt = db.prepare("CREATE INDEX i1a ON t1(indexInt);", true);
	    	 stmt.reset();
		     stmt.step();
		     stmt.dispose();
	    	 return true;
    	 } catch (SQLiteException e) {
    		 error(e);
	  	}
    	return false;
    }

 	public void begin_findIntWithIndex() {
 		try {
 			stmt = db.prepare("SELECT * FROM t1 WHERE indexInt=?1;", true);
 		} catch (SQLiteException e) {
 			error(e);
 		}
 	}

 	public int findIntWithIndex(int value) 
 	{
 		try {
 			stmt.reset();
 			stmt.bind(1, value);
 			if (stmt.step())
 				return (value);
 			else
 				return -1;
 		} catch (SQLiteException e) {
 			error(e);
 		}
 		return -1;
 	}
 	
 	public void end_findIntWithIndex() {
 		stmt.dispose();
 	}

 	
 	public void closeTable() {
 		db.dispose();
 	}
 	
}
