package io.tightdb.example;

import io.realm.ColumnType;
import io.realm.Mixed;
import io.realm.Table;
import io.realm.TableSpec;

public class MixedTest {

    public static void main(String[] args) {
        Table table = new Table();

        TableSpec tableSpec = new TableSpec();
        tableSpec.addColumn(ColumnType.INTEGER, "num");
        tableSpec.addColumn(ColumnType.MIXED, "mix");
        TableSpec subspec = tableSpec.addSubtableColumn("subtable");
        subspec.addColumn(ColumnType.INTEGER, "num");
        table.updateFromSpec(tableSpec);

        try {
            // Shouldn't work: no Mixed stored yet
            @SuppressWarnings("unused")
            Mixed m = table.getMixed(1, 0);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println( "Got exception - as expected!");
        }

        table.addEmptyRow();
        table.setMixed(1, 0, new Mixed(ColumnType.TABLE));
        Mixed m = table.getMixed(1, 0);

        ColumnType mt = table.getMixedType(1,0);
        System.out.println("m = " + m + " type: " + mt);

    }

}
