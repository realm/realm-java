package io.realm.internal.test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public final class DataProviderUtil {

    // Enforce non-instantiability
    private DataProviderUtil() {}

    public static Iterator<Object[]> allCombinations(List<?>... lists) {
        Iterator<Object[]> iterator = new VariationsIterator<Object>(Arrays.asList(lists));
        return iterator;
    }

}
