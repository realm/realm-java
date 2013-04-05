package com.tightdb.example.graph;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import com.tightdb.*;

public class TestTightdbGraph {
	static String filename = "graph.tightdb";
	
	public static void main(String[] args) {
		deleteFile(filename);
		
		TightdbGraphStore graph = new TightdbGraphStore(filename);
		
	    // add some node
	    int alice      = (int) graph.addNode(new Node(0, 0, 0, new Date(), "Alice"));
	    int bob        = (int) graph.addNode(new Node(0, 0, 0, new Date(), "Bob"));
	    int chess_club = (int) graph.addNode(new Node(0, 0, 0, new Date(), "Chess Club"));
	    	    
	    // link types
	    int knows     = 0;
	    int is_member = 1;

	    graph.addLink(new Link(alice, knows, bob, 0, "since=2001/10/03", 0, new Date()));
	    graph.addLink(new Link(bob, knows, alice, 0, "since=2001/10/04", 0, new Date()));
	    
	    graph.addLink(new Link(alice, is_member, chess_club, 0, "since=2005/07/01", 0, new Date()));
	    graph.addLink(new Link(bob, is_member, chess_club, 0, "since=2005/07/01", 0, new Date()));
	    
	    ArrayList<Link> alice_knows = graph.getLinkList(alice, knows);
	    System.out.println("\nAlice knows: \n" + alice_knows);

	    ArrayList<Link> bob_knows = graph.getLinkList(bob, knows);
	    System.out.println("\nBob knows: \n" + bob_knows);
	    
	    ArrayList<Link> members = graph.getBacklinkList(chess_club, is_member);
	    System.out.println("\nMembers: \n" + members);

	    System.out.println("\nDeleting node 'bob':");
	    boolean found = graph.deleteNode(bob, 0);
	    System.out.println("\nDeleting node 'bob': found: " + found);
	    
	    ArrayList<Link> members2 = graph.getBacklinkList(chess_club, is_member);
	    for (Link member : members2)
	        System.out.println("  MemberId: " + member.id1);


		System.out.println("\nDone.");		
	}
	
	 static void deleteFile(String filename) {
	   	File f = new File(filename);
	   	if (f.exists())
	   		f.delete();
	 }
}
