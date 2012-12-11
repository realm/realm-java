package com.tightdb.experiemnt;

public class Experiment {
	public static void main(String[] args) {
		insert(new Object[] {1, "txt"});
		insert("hmm", 2, "hej");
		
		Object[] sub2 = new Object[] {2, "str2", 22};
		Object[] subtable = new Object[] {1, "str1", sub2, 11};
		insert("hmm", subtable, 1);
		
	}
	
	public static void insert(Object... objects) {
		if (objects == null) return;
		System.out.print("\ninsert: ");
		for (Object obj : objects) {
			System.out.print(obj + ", ");
			if (obj instanceof Object[]) {
				System.out.print("...");
				insert(obj);
			}
		}
	}
}
