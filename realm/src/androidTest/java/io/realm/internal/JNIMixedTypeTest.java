package io.realm.internal;

import junit.framework.Test;
import junit.framework.TestCase;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import io.realm.internal.test.MixedData;

public class JNIMixedTypeTest extends TestCase {

    static List<MixedData> mixedDataList = new ArrayList<MixedData>();

    public static Collection<Object[]> parameters() {
        //Adding MixedData to the list
        mixedDataList.add(0, new MixedData(ColumnType.INTEGER, 123L));
        mixedDataList.add(1, new MixedData(ColumnType.FLOAT, 987.123f));
        mixedDataList.add(2, new MixedData(ColumnType.DOUBLE, 1234567.898d));
        mixedDataList.add(3, new MixedData(ColumnType.BOOLEAN, true));
        mixedDataList.add(4, new MixedData(ColumnType.STRING, "abc"));
        mixedDataList.add(5, new MixedData(ColumnType.BINARY, new byte[]{1, 2, 3, 4, 5}));
        mixedDataList.add(6, new MixedData(ColumnType.DATE, new Date(645342)));

        return Arrays.asList(
                new Object[]{mixedDataList},
                new Object[]{mixedDataList},
                new Object[]{mixedDataList},
                new Object[]{mixedDataList}
        );
    }

    public JNIMixedTypeTest(ArrayList mixedDataList) {
        this.mixedDataList = mixedDataList;

    }

    public void testShouldMatchMixedValues() {
        for (int i = 0; i < mixedDataList.size(); i++) {
            for (int j = 0; j < mixedDataList.size(); j++) {
                if (mixedDataList.get(i).value == mixedDataList.get(j).value) {
                    assertEquals(mixedDataList.get(i).value, mixedDataList.get(j).value);

                } else {
                    assertNotSame(mixedDataList.get(i).value, mixedDataList.get(j).value);

                }
            }
        }
    }

    public void testShouldFailOnWrongTypeRetrieval() {
        for (int i = 0; i < mixedDataList.size(); i++) {
            Object value = mixedDataList.get(i).type != ColumnType.STRING ? "abc" : 123;
            Mixed mixed = Mixed.mixedValue(value);

            switch (mixedDataList.get(i).type) {
                case BINARY:
                    try {
                        mixed.getBinaryByteArray();
                        fail("Wrong mixed type");
                    } catch (IllegalMixedTypeException e) {
                    }
                    break;
                case DATE:
                    try {
                        mixed.getDateValue();
                        fail("Wrong mixed type");
                    } catch (IllegalMixedTypeException e) {
                    }
                    break;
                case BOOLEAN:
                    try {
                        mixed.getBooleanValue();
                        fail("Wrong mixed type");
                    } catch (IllegalMixedTypeException e) {
                    }
                    break;
                case INTEGER:
                    try {
                        mixed.getLongValue();
                        fail("Wrong mixed type");
                    } catch (IllegalMixedTypeException e) {
                    }
                    break;
                case FLOAT:
                    try {
                        mixed.getFloatValue();
                        fail("Wrong mixed type");
                    } catch (IllegalMixedTypeException e) {
                    }
                    break;
                case DOUBLE:
                    try {
                        mixed.getDoubleValue();
                        fail("Wrong mixed type");
                    } catch (IllegalMixedTypeException e) {
                    }
                    break;
                case STRING:
                    try {
                        mixed.getStringValue();
                        fail("Wrong mixed type");
                    } catch (IllegalMixedTypeException e) {
                    }
                    break;
                default:
                    fail("wrong type");
                    break;
            }
        }
    }

    public void testShouldStoreValuesOfMixedType() throws Throwable {
        for (int i = 0; i < mixedDataList.size(); i++) {
            for (int j = 0; j < mixedDataList.size(); j++) {
                for (int k = 0; k < mixedDataList.size(); k++) {

                    Table table = new Table();
                    table.addColumn(ColumnType.MIXED, "mix");

                    table.add(mixedDataList.get(i).value);

                    checkMixedCell(table, 0, 0, mixedDataList.get(i).type, mixedDataList.get(i).value);

                    table.setMixed(0, 0, Mixed.mixedValue(mixedDataList.get(j).value));

                    checkMixedCell(table, 0, 0, mixedDataList.get(j).type, mixedDataList.get(j).value);

                    table.setMixed(0, 0, Mixed.mixedValue(mixedDataList.get(k).value));

                    checkMixedCell(table, 0, 0, mixedDataList.get(k).type, mixedDataList.get(k).value);
                    table.close();
                }
            }
        }
    }

    private void checkMixedCell(Table table, long col, long row, ColumnType columnType, Object value) throws IllegalMixedTypeException {
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

    public static Test suite() {
        return new JNITestSuite(JNIMixedTypeTest.class, parameters());

    }
}

