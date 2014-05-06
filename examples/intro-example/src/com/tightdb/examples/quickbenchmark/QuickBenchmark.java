package com.realm.examples.quickbenchmark;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import com.realm.*;

public class QuickBenchmark {
    final static int ROW_COUNT = 500000;
    final static int ROUNDS    = 1;

    // Define a TightDB table. It will generate 3 classes: TestTable, TestQuery, TestTableView
    @DefineTable(table = "PersonTable")
    class person {
        String  name;
        Boolean hired;
        long    age;
        String  day;
    }

    // Define a class for Java arrays
    private static class JavaPerson
    {
        String  name;
        Boolean hired;
        int     age;
        String  day;

        public JavaPerson(String name, Boolean hired, int age, String day) {
            this.name = name;
            this.hired = hired;
            this.age = age;
            this.day = day;
        }
    }

    public static void main(String[] args) {
        System.out.println("\nThis is just a basic sanity check to see if TightDB was compiled correctly.");
        System.out.println("It is by no means a comprehensive or even realistic benchmark.");
        System.out.println("It just does some basic operations and compares with Javas ArrayList and HashMap.");

        System.out.println("\n\nPerformance tests with " + ROW_COUNT + " rows. Test is repeated "
                + ROUNDS + " times.");
        for (int _i=0; _i<ROUNDS; _i++) {
        
            // We need some random names that we know are present for lookups
            Random rand = new Random();
            String[] randomNames = new String[100];
            for (int i = 0; i < 100; i++) {
                randomNames[i] = "s" + rand.nextInt(ROW_COUNT+1);
            }
            // We also need to define a name and age to search for later
            final String LAST_NAME = "s" + (ROW_COUNT - 1);
            final int    LAST_AGE  = 60;

            Timer timer = new Timer();


            /****************************************************************
             * Test TightDB
             ****************************************************************/
            System.out.println("TightDB: ");

            // Create a simple table and fill it with somewhat random values
            rand.setSeed(0);
            PersonTable table = new PersonTable();
            for (int row = 0; row < ROW_COUNT; row++) {
                // we want name to be unique so we just make it from the number
                String  name  = "s" + row;
                boolean hired = (row % 2 == 0);
                int     age   = rand.nextInt(50);
                if (row == ROW_COUNT-1)
                    age = 60;
                String  day;
                if (row % 2 == 0)
                    day = "Monday";
                else
                    day = "Tuesday";

                table.add(name, hired, age, day);

                if (row == 100)
                    table.optimize();
            }

            // Start with a search for the last name in the last row
            // (has to do linear scan of all rows)
            long tightdbLastPos = 0;
            timer.Start();
            for (int n = 0; n < ROUNDS; ++n) {
                tightdbLastPos += table.age.findFirst(LAST_AGE).getPosition();
            }
            long tightdbFindTime = timer.GetTimeInMs();
            System.out.printf("  find (last integer):    %10d msec\n", tightdbFindTime);


            // Do some simple aggregates, we will start with a sum
            // (we add them up and print the sum so it does not just get optimized away)
            timer.Start();
            long tightdbSumAge = 0;
            for (int n = 0; n < ROUNDS; ++n) {
                tightdbSumAge += table.age.sum();
            }
            long tightdbSumTime = timer.GetTimeInMs();
            System.out.printf("  sum (all integers):     %10d msec\n", tightdbSumTime);


            // Then lets do a count
            timer.Start();
            long tightdbCountMondays = 0;
            for (int n = 0; n < ROUNDS; ++n) {
                tightdbCountMondays += table.day.count("Monday");
            }
            long tightdbCountTime = timer.GetTimeInMs();
            System.out.printf("  count (string):         %10d msec\n", tightdbCountTime);


            // Add an index and lets try some lookups
            table.name.setIndex();
            timer.Start();
            long tightdbLookups = 0;
            int randLength = randomNames.length;
            for (int n = 0; n < ROUNDS; ++n) {
                long rowIndex = table.name.findFirst( randomNames[ rand.nextInt(randLength) ] ).getPosition();
                tightdbLookups += table.get(rowIndex).getAge();
            }
            long tightdbLookupTime = timer.GetTimeInMs();
            System.out.printf("  find first (random string): %10d msec\n", tightdbLookupTime);


            /****************************************************************
             * Test Java data structures (ArrayList, HashMap)
             ****************************************************************/

            System.out.println("Java: ");

            // Create a simple table and fill it with somewhat random values
            // Create Map with same data
            HashMap<String, JavaPerson> javaMapTable = new HashMap<String, JavaPerson>();
            ArrayList<JavaPerson> javaTable = new ArrayList<JavaPerson>();
            rand.setSeed(0);
            for (int row = 0; row < ROW_COUNT; row++) {
                // we want name to be unique so we just make it from the number
                String  name  = "s" + row;
                boolean hired = (row % 2 == 0);
                int     age   = rand.nextInt(50);
                if (row == ROW_COUNT-1)
                    age = LAST_AGE;
                String  day;
                if (row % 2 == 0)
                    day = "Monday";
                else
                    day = "Tuesday";

                JavaPerson person = new JavaPerson(name, hired, age, day);
                javaTable.add(person);
                javaMapTable.put(name, person);
            }

            // Start with a search for the last name in the last row
            // (has to do linear scan of all rows)
            timer.Start();
            long javaLastPos = 0;
            for (int n = 0; n < ROUNDS; n++) {
                // Find position of LAST_NAME
                for (int index = 0; index < ROW_COUNT; index++) {
                    //if (javaTable.get(index).name.equals(LAST_NAME)) {
                    if (javaTable.get(index).age == LAST_AGE) {
                        javaLastPos += index;
                        break;
                    }
                }
            }
            long javaFindTime = timer.GetTimeInMs();
            System.out.printf("  find (last integer):    %10d msec\n", javaFindTime);


            // Do a sum with a basic loop
            timer.Start();
            long javaSumAge = 0;
            for (int n = 0; n < ROUNDS; n++) {
                // Find position of LAST_NAME
                for (int index = 0; index < ROW_COUNT; index++) {
                    javaSumAge += javaTable.get(index).age;
                }
            }
            long javaSumTime = timer.GetTimeInMs();
            System.out.printf("  sum (all integers):     %10d msec\n", javaSumTime);


            // Do a count
            timer.Start();
            long javaCountMondays = 0;
            for (int n = 0; n < ROUNDS; n++) {
                // Find position of LAST_NAME
                for (int index = 0; index < ROW_COUNT; index++) {
                    if (javaTable.get(index).day == "Monday")
                        javaCountMondays += 1;
                }
            }
            long javaCountTime = timer.GetTimeInMs();
            System.out.printf("  count (string):         %10d msec\n", javaCountTime);


            timer.Start();
            long javaLookups = 0;
            for (int n = 0; n < ROUNDS; n++) {
                javaLookups += javaMapTable.get( randomNames[ rand.nextInt(randLength) ] ).age;
            }
            long javaLookupTime = timer.GetTimeInMs();
            System.out.printf("  lookup (random string): %10d msec\n", javaLookupTime);



            /****************************************************************
             * Compare
             ****************************************************************/
            if (tightdbLastPos != javaLastPos ||
                tightdbSumAge != javaSumAge ||
                tightdbCountMondays != javaCountMondays ||
                tightdbLookups != javaLookups) {

                System.out.println("\nInvalid results!!!");
            }

            // Print comparable speeds
            if (tightdbFindTime > 0)
                System.out.printf("\nfind:   realm is %d times faster than ArrayList\n", javaFindTime / tightdbFindTime);
            if (tightdbSumTime > 0)
                System.out.printf("sum:    realm is %d times faster than ArrayList\n", javaSumTime / tightdbSumTime);
            if (tightdbCountTime > 0)
                System.out.printf("count:  realm is %d times faster than ArrayList\n", javaCountTime / tightdbCountTime);
            if (tightdbLookupTime > 0)
                System.out.printf("lookup: realm is %d times faster than HashMap\n", javaLookupTime / tightdbLookupTime);
            System.out.println("\nDONE.");

        }
    }
}


class Timer {
    static long startTime;

    public void Start() {
        startTime = System.nanoTime();
    }

    public long GetTimeInMs() {
        long stopTime = System.nanoTime();
        return (stopTime - startTime) / 1000000;
    }
}
