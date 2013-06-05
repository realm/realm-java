package com.tightdb.doc;

import com.tightdb.generated.Employee;
import com.tightdb.lib.AbstractColumn;

public class RowExamples {

    public static void main(String[] args) {

        /* EXAMPLE: after */

        Person third = people.at(0).after(2);

        /* EXAMPLE: before */

        Person second = people.at(4).before(3);

        /* EXAMPLE: columns */

        for (AbstractColumn<?, ?, ?, ?> column : john.columns()) {
            System.out.println(column.getName() + "=" + column.getReadableValue());
        }

        /* EXAMPLE: next */

        Person fifth = people.at(3).next();

        /* EXAMPLE: previous */

        Person secondLast = people.last().previous();

        /* EXAMPLE: getFoo */

        String name = people.first().getName();
        int age = people.last().getAge();

        /* EXAMPLE: setFoo */

        people.last().setName("John");
        people.first().setAge(30);

        /* EXAMPLE: END! */

    }
}
