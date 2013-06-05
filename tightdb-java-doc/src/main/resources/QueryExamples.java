package com.tightdb.doc;

import com.tightdb.generated.Employee;
import com.tightdb.generated.EmployeeQuery;
import com.tightdb.lib.TightDB;

public class QueryExamples {

    public static void main(String[] args) {

        /* EXAMPLE: clear */

        people.age.lessThan(14).clear();
        
        /* EXAMPLE: count */

        long count = people.name.equal("John").count();
        
        /* EXAMPLE: endGroup */

        people.name.eq("John").group()
                                      .age.eq(10)
                                      .or()
                                      .age.eq(20)
                              .endGroup()
                              .findAll());
        
        /* EXAMPLE: findAll */

        PersonView johns = people.name.equal("John").findAll();
        
        /* EXAMPLE: findFirst */

        Person firstJohn = people.name.equal("John").findFirst();
        
        /* EXAMPLE: findLast */

        Person lastJohn = people.name.equal("John").findLast();
        
        /* EXAMPLE: findNext */

        PersonQuery johns = people.name.equal("John");
        Person p;
        while ((p = johns.findNext()) != null) System.out.println(p);
        
        /* EXAMPLE: group */

        PersonView view = people.name.eq("John").group()
                                                        .age.eq(10)
                                                        .or()
                                                        .age.eq(20)
                                                .endGroup()
                                                .findAll();
        
        /* EXAMPLE: or */

        PersonView view = people.age.eq(10).or().age.eq(20).findAll();
        
        /* EXAMPLE: END! */
        
    }
}
