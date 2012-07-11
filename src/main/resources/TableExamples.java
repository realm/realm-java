package com.tightdb.doc;

import com.tightdb.generated.Employee;

public class TableExamples {

	public static void main(String[] args) {

		/* EXAMPLE: add */

		people.add("Mary", 21, false);
		people.add("Lars", 24, true);
		
		/* EXAMPLE: at */

		Person p = people.at(42);
		
		/* EXAMPLE: clear */

		people.clear();
		
		/* EXAMPLE: first */

		Person firstPerson = people.first();
		
		/* EXAMPLE: getName */

		String tableName = people.getName();
		
		/* EXAMPLE: insert */

		people.insert(0, "Mary", 21, false);
		people.insert(0, "Lars", 21, true);
		
		/* EXAMPLE: isEmpty */

		boolean empty = people.isEmpty();
		
		/* EXAMPLE: iterator */

		for (Person p : people) System.out.println(p);
		
		/* EXAMPLE: last */

		Person lastPerson = people.last();

		/* EXAMPLE: optimize */
		
		people.optimize();
		
		/* EXAMPLE: remove */

		people.remove(0);
		
		/* EXAMPLE: size */

		long size = people.size();
		
		/* EXAMPLE: where */

		people.where().age.is(22).findAll();
		
		/* EXAMPLE: END! */
		
	}
	
}
