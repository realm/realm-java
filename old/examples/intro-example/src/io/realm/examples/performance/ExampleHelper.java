/*
 * Copyright 2014 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.realm.examples.performance;

import java.util.Scanner;

import io.realm.typed.TightDB;

public class ExampleHelper {

    public static int getRandNumber() {
        return (int)(Math.random() * 1000);
    }

    public static String getNumberString(long nlong) {
        String ones[] = {"zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine",
                                     "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen",
                                     "eighteen", "nineteen"};
        String tens[] = {"", "ten", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety"};

        int n = (int)nlong;

        String txt = null;
        if (n >= 1000) {
            txt = getNumberString(n/1000) + " thousand ";
            n %= 1000;
        }
        if (n >= 100) {
            txt += ones[n/100];
            txt += " hundred ";
            n %= 100;
        }
        if (n >= 20) {
            txt += tens[n/10];
            n %= 10;
        }
        else {
            txt += " ";
            txt += ones[n];
        }

        return txt;
    }

    public static void waitForEnter() {
        System.out.println("Press Enter to continue...");
        Scanner sc = new Scanner(System.in);
           while(!sc.nextLine().equals(""));
        sc.close();
    }

    // Measuring memory usage in Java is highly unreliable...

    static final Runtime run = Runtime.getRuntime();

    private static long memUsed() {
        return run.totalMemory() - run.freeMemory();
    }

    public static long getUsedMemory() {
        long memAfterGC  = memUsed();
        long memBeforeGC = memAfterGC+1;
        while (memAfterGC < memBeforeGC) {
            memBeforeGC = memAfterGC;
            for (int i = 0; i < 5; ++i) {
                TightDB.gcGuaranteed();
                System.runFinalization();
                Thread.yield();
            }
            memAfterGC = memUsed();
        }
        return memBeforeGC;
    }

    public static void test_getMemUsed() {
        long mem[] = new long[5];
        mem[0] = ExampleHelper.getUsedMemory();
        mem[1] = ExampleHelper.getUsedMemory();
        mem[2] = ExampleHelper.getUsedMemory();
        mem[3] = ExampleHelper.getUsedMemory();
        mem[4] = ExampleHelper.getUsedMemory();
        for (int i=0; i<5; ++i)
            System.out.println("Memuse " + mem[i]);
    }
}
