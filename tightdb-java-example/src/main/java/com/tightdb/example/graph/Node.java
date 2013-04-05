package com.tightdb.example.graph;

import java.nio.ByteBuffer;
import java.util.Date;

public class Node {
	long 		id;
	long 		node_type;
	long 		version;
	Date 		time;
	ByteBuffer 	data;
	
	Node(long id, long node_type, long version, Date time, ByteBuffer data) {
		this.id = id;
		this.node_type = node_type;
		this.version = version;
		this.time = time;
		this.data = data;
	}
}
