package com.tightdb.examples.performance;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.query.nativ.ONativeSynchQuery;
import com.orientechnologies.orient.core.query.nativ.OQueryContextNativeSchema;
import com.orientechnologies.orient.core.query.nativ.OQueryContextNative;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

import java.util.List;
import java.util.Properties;

public class OrientDBTest extends PerformanceBase implements IPerformance {

    private int Rows = 0;
    private ODatabaseDocumentTx db;
    
    public OrientDBTest() {
        this.db = new ODatabaseDocumentTx("local:/tmp/test").open("admin", "admin");
        if (this.db.getMetadata().getSchema().existsClass("Table")) {
            this.db.getMetadata().getSchema().dropClass("Table");
        }
        OClass table = db.getMetadata().getSchema().createClass("Table");
        table.createProperty("indexInt", OType.INTEGER);
        table.createProperty("second",   OType.STRING);
        table.createProperty("smallInt", OType.SHORT);
        table.createProperty("byteInt",  OType.BYTE);
        table.createProperty("longInt",  OType.LONG);
    }
    
    public long usedNativeMemory() {
    	return 0;
    }
    
    public void buildTable(int rows) {
		for (int i = 0; i < rows; ++i) {
		    int n = Util.getRandNumber();		    
            ODocument table = new ODocument("Table");
            table.field("indexInt", n);
            table.field("second", Util.getNumberString(n));
            table.field("byteInt", Performance.BYTE_TEST_VAL);
            table.field("smallInt", Performance.SMALL_TEST_VAL);
            table.field("longInt", Performance.LONG_TEST_VAL);
            table.save();
		}
        this.db.commit();
		this.Rows = rows;
    }
    
  //--------------- small Int
    
    public boolean findSmallInt(long value) {
        List<ODocument> result = this.db.query(new OSQLSynchQuery<ODocument>("select count(*) from Table where smallInt = "+value));

        ODocument d = result.get(0);
        long r = d.field("count");
    	return (r != Rows);	
    }
    
    //--------------- byte Int
 
    public boolean findByteInt(long value) {
        List<ODocument> result = this.db.query(new OSQLSynchQuery<ODocument>("select count(*) from Table where byteInt = "+value));

        ODocument d = result.get(0);
        long r = d.field("count");
    	return (r != Rows);	
    }
    
  //--------------- long Int
    
    public boolean findLongInt(long value) {
        List<ODocument> result = this.db.query(new OSQLSynchQuery<ODocument>("select count(*) from Table where longInt = "+value));

        ODocument d = result.get(0);
        long r = d.field("count");
    	return (r != Rows);	
    }
    
    //---------------- string
    
    public boolean findString(String value) {
        List<ODocument> result = this.db.query(new OSQLSynchQuery<ODocument>("select count(*) from Table where second = "+value));

        ODocument d = result.get(0);
        long r = d.field("count");
    	return (r != Rows);	
    }
    
    //---------------- int with index
    
    public boolean addIndex() {
    	return false;
    }

	public long findIntWithIndex(long value) 
	{
		return -1;
	}
}
