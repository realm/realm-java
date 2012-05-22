package com.tightdb;

public class LoadLibrary {
	
	public static void Tightdb() {
		try{
			System.loadLibrary("tightdb_jni");
		}catch(UnsatisfiedLinkError err){
			try {
				System.loadLibrary("tightdb_jni32");
			} catch(UnsatisfiedLinkError err2){
				try {
					System.loadLibrary("tightdb_jni64");
				} catch(UnsatisfiedLinkError err3){
					System.out.println("Can't load find tightdb library.");
					System.out.println(err2);
				}
			}
		}
	}
}