package com.tightdb.example.graph;

import java.util.Date;
import java.nio.ByteBuffer;
import com.tightdb.*;

public class TightdbGraphStore {

	SharedGroup db;
	
	@DefineTable(table = "NodeTable")
	class node {
		Long 	 	node_type;
		long 	 	version;
		Date 	 	time;
		ByteBuffer 	data;
		boolean	 	deleted;
		linktype 	link_in;
		linktype 	link_out;
	}
	@DefineTable(table = "LinktypeTable")
	class linktype {
		long type;
		long link;
	}
	
	@DefineTable(table = "LinkTable")
	class link {
		long 		id1;
		long 		link_type;
		long 		id2;
		ByteBuffer 	data;
		long 		version;
		Date 		time;
		boolean 	deleted;
	}

	@DefineTable()
	class nodeDeleted {
		long id;
	}
	
	@DefineTable()
	class linkDeleted {
		long id;
	}
	
	TightdbGraphStore(String filename) {
		db = new SharedGroup(filename);

		// Create initial tables
		WriteTransaction tr = db.beginWrite();
		try {
			if (tr.size() == 0) {
				new NodeTable(tr);
				new LinkTable(tr);
				new NodeDeletedTable(tr);
				new LinkDeletedTable(tr);
				for (int i=0; i<tr.size(); i++)
					System.out.println(" Table: '" + tr.getTableName(i) + "' created.");
				tr.commit();		
			}
		} catch (Exception e) {
			tr.rollback();
			System.err.println("Exception");
		}
	}
	
	long addNode(Node node) {
		long free_row_ndx = 0;
		WriteTransaction tr = db.beginWrite();
		try {						
			NodeTable nodes = new NodeTable(tr);
			NodeDeletedTable deleted_nodes = new NodeDeletedTable(tr);
			if (deleted_nodes.isEmpty()) {
				nodes.add(node.node_type, node.version, node.time, node.data, false, null, null);
				free_row_ndx = nodes.size() - 1; // inserted at end
			} else {
				 free_row_ndx = deleted_nodes.at(0).getId();
				 // (set() method is not yet implemented in typed interface
				 nodes.at(free_row_ndx).setNode_type(node.node_type);
				 nodes.at(free_row_ndx).setVersion(node.version);
				 nodes.at(free_row_ndx).setTime(node.time);
				 nodes.at(free_row_ndx).setData(node.data);
				 nodes.at(free_row_ndx).setDeleted(false);
				 nodes.at(free_row_ndx).setLink_in(null);
				 nodes.at(free_row_ndx).setLink_out(null);
				 
				 deleted_nodes.remove(0);
			}
			tr.commit();
		} catch (Exception e) {
			System.err.println("Exception");
			tr.rollback();
		}
		return free_row_ndx;
	}
	
	Node getNode(long node_type, long node_id) {
		ReadTransaction tr = db.beginRead();
		NodeTable nodes = new NodeTable(tr);
		NodeRow row = nodes.at(node_id);
		assert(row.getNode_type() == node_type);
		tr.endRead();
		return new Node(node_id, node_type, row.getVersion(), row.getTime(), row.getData());
	}
	
	boolean updateNode(Node node) {
		WriteTransaction tr = db.beginWrite();
		try {						
			// TODO
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
			// TODO
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
