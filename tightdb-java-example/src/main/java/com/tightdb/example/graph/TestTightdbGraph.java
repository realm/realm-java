package com.tightdb.example.graph;

import java.io.File;
import com.tightdb.*;

public class TestTightdbGraph {
	static String filename = "graph.tightdb";
	
	public static void main(String[] args) {
		System.out.println("starting...");
		
		deleteFile(filename);
		
		TightdbGraphStore db = new TightdbGraphStore(filename);
		
		
		System.out.println("Done.");
		
	}
	
	 static void deleteFile(String filename) {
	   	File f = new File(filename);
	   	if (f.exists())
	   		f.delete();
	 }
}
