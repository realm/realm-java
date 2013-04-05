package com.tightdb.example.graph;

import java.util.Date;

public class Link {
	long id1;
	long link_type;
	long id2;
	byte[] data;
	long version;
	Date time;
	boolean deleted;
	
	Link(long id1, long link_type, long id2, byte[] data, long version, Date time) {
		this.id1 = id1;
		this.link_type = link_type;
		this.id2 = id2;
		this.data = data;
		this.version = version;
		this.time = time;
	}
}
