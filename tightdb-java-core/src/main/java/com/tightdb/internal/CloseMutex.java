package com.tightdb.internal;

// Singleton for handling synchronized close of tables and groups

public class CloseMutex {

	private static CloseMutex ourInstance = new CloseMutex();

	// Creation not possible externally
	private CloseMutex() {
		// System.out.println("CloseHandler created!!!");
	}

	public static CloseMutex getInstance() {
		return ourInstance;
	}

}
