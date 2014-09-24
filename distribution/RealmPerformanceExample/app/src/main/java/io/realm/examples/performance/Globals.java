package io.realm.examples.performance;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Xion on 9/23/14.
 */
public class Globals {

    public static final int NUM_INSERTS = 100000;
    public static final int MAX_AGE = 50;
    public static final int MIN_AGE = 20;
    public static final int NUM_TEST_NAMES = 1000;

    public static List<String> NAMES = null;

    public static void initNames() {
        NAMES = new ArrayList<String>();
        for(int i=0;i<Globals.NUM_TEST_NAMES;i++) {
            Globals.NAMES.add("Foo"+i);
        }
    }

    public static String getName(int row) {
        return NAMES.get(row % NUM_TEST_NAMES);
    }

    public static int getAge(int row) {
        return row % MAX_AGE + MIN_AGE;
    }

    public static int getHired(int row) {
        return row % 2;
    }

    public static boolean getHiredBool(int row) {
        if(row % 2 == 0) return false;
        return true;
    }

//    - (int)ageValue:(NSUInteger)row {
//        return row % (int)_ageRange.length + (int)_ageRange.location;
//    }
//
//    - (BOOL)hiredValue:(NSUInteger)row {
//        return row % 2;
//    }
//
//    - (NSString *)nameValue:(NSUInteger)row {
//        return s_names[row % kNumTestNames];
//    }
}
