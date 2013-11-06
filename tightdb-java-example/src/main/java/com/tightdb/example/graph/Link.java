package com.tightdb.example.graph;

import java.util.Date;

public class Link {
	int 		id1;
	int 		link_type;
	int 		id2;
	String 		data;
	int 		version;
	Date 		time;
	
	Link(int id1, int link_type, int id2, int visibility, String data, int version, Date time) {
		this.id1 		= id1;
		this.link_type 	= link_type;
		this.id2 		= id2;
		this.data 		= data;
		this.version 	= version;
		this.time 		= time;
	}
	
	public String toString() {
	    return String.format("id1: %d, type: %d, id2: %d, data: %s, version: %d, time: %s\n", 
	    		this.id1, this.link_type, this.id2, this.data, this.version, this.time );
	}

}
