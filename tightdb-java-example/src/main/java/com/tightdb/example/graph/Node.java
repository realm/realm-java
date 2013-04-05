package com.tightdb.example.graph;

import java.util.Date;

public class Node {
	long id;
	long node_type;
	long version;
	Date time;
	byte[] data;
	
	Node(long id, long node_type, long version, Date time, byte[] data) {
		this.id = id;
		this.node_type = node_type;
		this.version = version;
		this.time = time;
		this.data = data;
	}
}
