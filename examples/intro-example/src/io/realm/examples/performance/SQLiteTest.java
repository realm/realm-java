package io.realm.examples.performance;

import com.almworks.sqlite4java.SQLite;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;

public class SQLiteTest extends PerformanceBase implements IPerformance {

    private SQLiteConnection db = null;
    private SQLiteStatement stmt = null;

    private void error(SQLiteException e) {
        System.out.println("SQL error");
        db.dispose();
        e.printStackTrace();
    }

    public SQLiteTest() {
        db = new SQLiteConnection();
        try {
            db.open(true);
            // Create table
            db.exec("create table t1 (indexInt INTEGER, string VARCHAR(100), byteInt INTEGER, smallInt INTEGER, longInt INTEGER);");
        } catch (SQLiteException e) {
            error(e);
        }
    }

    public long usedNativeMemory() {
        try {
            return SQLite.getMemoryUsed();
        } catch (SQLiteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return 0;
    }

    public void buildTable(int rows) {
        SQLiteStatement stmt;
        try {
            stmt = db.prepare("INSERT INTO t1 VALUES(?1, ?2, ?3, ?4, ?5);", true);
            for (int i = 0; i < rows; ++i) {
                int n = ExampleHelper.getRandNumber();
                stmt.reset();
                stmt.bind(1, n);
                stmt.bind(2, ExampleHelper.getNumberString(n));
                stmt.bind(3, Performance.BYTE_TEST_VAL);
                stmt.bind(4, Performance.SMALL_TEST_VAL);
                stmt.bind(5, Performance.LONG_TEST_VAL);
                stmt.step();
            }
            stmt.dispose();
        } catch (SQLiteException e) {
            error(e);
        }
    }

    //--------------- small Int

    public void begin_findSmallInt(long value) {
        try {
            stmt = db.prepare("SELECT * FROM t1 WHERE smallInt=?1;", true);
            stmt.bind(1, value);
        } catch (SQLiteException e) {
            error(e);
        }
    }

    public boolean findSmallInt(long value) {
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

    public void begin_findByteInt(long value) {
        try {
            stmt = db.prepare("SELECT * FROM t1 WHERE byteInt=?1;", true);
            stmt.bind(1, value);
        } catch (SQLiteException e) {
            error(e);
        }
    }

     public boolean findByteInt(long value) {
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

     //--------------- long Int

    public void begin_findLongInt(long value) {
        try {
            stmt = db.prepare("SELECT * FROM t1 WHERE longInt=?1;", true);
            stmt.bind(1, value);
        } catch (SQLiteException e) {
            error(e);
        }
    }

      public boolean findLongInt(long value) {
         try {
             stmt.reset();
             return stmt.step();
         } catch (SQLiteException e) {
            error(e);
         }
          return false;
      }

      public void end_findLongInt() {
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

    public long findIntWithIndex(long value)
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
