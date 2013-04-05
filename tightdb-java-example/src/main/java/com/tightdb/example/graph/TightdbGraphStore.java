package com.tightdb.example.graph;

import java.util.Date;

import com.tightdb.*;

public class TightdbGraphStore {

	SharedGroup db;
	static String NODES = "nodes";
	static String DELETED_NODES = "nodes_d";
	static String LINKS = "links";
	static String DELETED_LINKS = "links_d";
	
	@DefineTable(table = "NodeTable")
	class node {
		Long 	node_type;
		long 	version;
		Date 	time;
		byte[]	data;
		linktype link_in;
		linktype link_out;
	}
	@DefineTable
	class linktype {
		long type;
		long link;
	}
	
	@DefineTable
	class link {
		long 	id1;
		long 	link_type;
		long 	id2;
		byte[] 	data;
		long 	version;
		Date 	time;
		boolean deleted;
	}

	@DefineTable
	class deleted {
		long id;
	}
	
	
	TightdbGraphStore(String filename) {
		db = new SharedGroup(filename);

		// Create initial tables
		WriteTransaction tr = db.beginWrite();
		try {
			if (tr.size() == 0) {
				NodeTable nodes = new NodeTable(tr);
				LinkTable links = new LinkTable(tr);
				DeletedTable deletedNodes = new DeletedTable(tr);
				DeletedTable deletedLinks = new DeletedTable(tr);
				tr.commit();
			}
		} catch (Exception e) {
			System.err.println("Exception");
			tr.rollback();
		}	
	}
	
	long addNode(Node node) {
		WriteTransaction tr = db.beginWrite();
		try {						
			tr.getTable(NODES);
			tr.commit();
		} catch (Exception e) {
			System.err.println("Exception");
			tr.rollback();
		}
		
		return 0;
	}
	
	boolean updateNode(Node node) {
		WriteTransaction tr = db.beginWrite();
		try {						
			
			tr.commit();
		} catch (Exception e) {
			System.err.println("Exception");
			tr.rollback();
		}
		
		return true;
	}
	
	boolean deleteNode(long node_type, long id) {
		WriteTransaction tr = db.beginWrite();
		try {						
			// ??
			tr.commit();
		} catch (Exception e) {
			System.err.println("Exception");
			tr.rollback();
			return false;
		}
		return true;
	}

	boolean addLink(Link link) {
		return true;
	}

	/*
	 		WriteTransaction tr = db.beginWrite();
		try {						
			tr.commit();
		} catch (Exception e) {
			System.err.println("Exception");
			tr.rollback();
		}

	 */
	
}
