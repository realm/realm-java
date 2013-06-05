package com.tightdb.doc;

import com.tightdb.generated.Employee;

public class ViewExamples {

    public static void main(String[] args) {

        /* EXAMPLE: at */

        Person p = people.age.equals(19).findAll().at(1);
        
        /* EXAMPLE: clear */

        people.age.equals(19).findAll().clear();
        
        /* EXAMPLE: first */

        Person firstPerson = people.age.equals(19).findAll().first();
        
        /* EXAMPLE: isEmpty */

        boolean empty = people.age.equals(19).findAll().isEmpty();
        
        /* EXAMPLE: iterator */

        for (Person p : people.age.equals(19).findAll()) System.out.println(p);
        
        /* EXAMPLE: last */

        Person lastPerson = people.age.equals(19).findAll().last();
        
        /* EXAMPLE: size */

        long size = people.age.equals(19).findAll().size();
        
        /* EXAMPLE: END! */
        
    }
    
}
