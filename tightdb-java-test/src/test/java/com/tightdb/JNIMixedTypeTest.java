package com.tightdb;

import static org.testng.AssertJUnit.*;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.tightdb.test.DataProviderUtil;
import com.tightdb.test.MixedData;

public class JNIMixedTypeTest {

    @Test(dataProvider = "mixedValuesProvider")
    public void shouldMatchMixedValues(MixedData value1, MixedData value2,
            MixedData value3) throws Exception {
        Mixed mixed1 = Mixed.mixedValue(value1.value);
        Mixed mixed2 = Mixed.mixedValue(value2.value);

        if (value1.value.equals(value2.value)) {
            assertEquals(mixed1, mixed2);
        } else {
            assertNotSame(mixed1, mixed2);
        }
    }

    @Test(expectedExceptions = IllegalAccessException.class, dataProvider = "columnTypesProvider")
    public void shouldFailOnWrongTypeRetrieval(ColumnType columnType)
            throws Exception {
        Object value = columnType != ColumnType.STRING ? "abc" : 123;
        Mixed mixed = Mixed.mixedValue(value);

        switch (columnType) {
        case BINARY:
            mixed.getBinaryByteArray();
            break;
        case DATE:
            mixed.getDateValue();
            break;
        case BOOLEAN:
            mixed.getBooleanValue();
            break;
        case INTEGER:
            mixed.getLongValue();
            break;
        case FLOAT:
            mixed.getFloatValue();
            break;
        case DOUBLE:
            mixed.getDoubleValue();
            break;
        case STRING:
            mixed.getStringValue();
            break;

        default:
            assertTrue(false);
            break;
        }
    }

    @Test(dataProvider = "mixedValuesProvider")
    public void shouldStoreValuesOfMixedType(MixedData value1,
            MixedData value2, MixedData value3) throws Exception {
        Table table = new Table();

        TableSpec tableSpec = new TableSpec();
        tableSpec.addColumn(ColumnType.MIXED, "mix");
        table.updateFromSpec(tableSpec);

        table.add(value1.value);

        checkMixedCell(table, 0, 0, value1.type, value1.value);

        table.setMixed(0, 0, Mixed.mixedValue(value2.value));

        checkMixedCell(table, 0, 0, value2.type, value2.value);

        table.setMixed(0, 0, Mixed.mixedValue(value3.value));

        checkMixedCell(table, 0, 0, value3.type, value3.value);
        table.private_debug_close();
    }

    private void checkMixedCell(Table table, long col, long row,
            ColumnType columnType, Object value) throws IllegalAccessException {
        ColumnType mixedType = table.getMixedType(col, row);
        assertEquals(columnType, mixedType);

        Mixed mixed = table.getMixed(col, row);
        if (columnType == ColumnType.BINARY) {
            if (mixed.getBinaryType() == Mixed.BINARY_TYPE_BYTE_ARRAY) {
                // NOTE: We never get here because we always "get" a ByteBuffer.
                byte[] bin = mixed.getBinaryByteArray();
                assertEquals(Mixed.mixedValue(value), bin);
            } else {
                ByteBuffer binBuf = mixed.getBinaryValue();
                // TODO: Below is sort of hack to compare the content of the
                // buffers, since you always will get a ByteBuffer from a Mixed.
                ByteBuffer valueBuf = ByteBuffer.wrap((byte[]) value);
                if (!binBuf.equals(valueBuf))
                    System.out.println("***failed");
                assertEquals(Mixed.mixedValue(valueBuf), Mixed.mixedValue(binBuf));
            }
        } else {
            assertEquals(value, mixed.getValue());
        }
    }

    @DataProvider(name = "mixedValuesProvider")
    public Iterator<Object[]> mixedValuesProvider() {
        Object[] values = {
                new MixedData(ColumnType.BOOLEAN, true),
                new MixedData(ColumnType.STRING, "abc"),
                new MixedData(ColumnType.INTEGER, 123L),
                new MixedData(ColumnType.FLOAT, 987.123f),
                new MixedData(ColumnType.DOUBLE, 1234567.898d),
                new MixedData(ColumnType.DATE, new Date(645342)),
                new MixedData(ColumnType.BINARY, new byte[] { 1, 2,
                        3, 4, 5 }) };

        List<?> mixedValues = Arrays.asList(values);
        return DataProviderUtil.allCombinations(mixedValues, mixedValues,
                mixedValues);
    }

    @DataProvider(name = "columnTypesProvider")
    public Object[][] columnTypesProvider() {
        Object[][] values = { {ColumnType.BOOLEAN},
                {ColumnType.STRING}, {ColumnType.INTEGER},
                {ColumnType.FLOAT}, {ColumnType.DOUBLE},
                {ColumnType.DATE}, {ColumnType.BINARY} };

        return values;
    }

}
