
package io.realm.internal;

import junit.framework.TestCase;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import junit.framework.Test;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import io.realm.internal.test.ExpectedValue;
import io.realm.internal.test.MixedData;

public class TestJNIMixedType extends TestCase {


    ExpectedValue expectedValue = new ExpectedValue(null);
    static byte[] b = new byte[] { 1, 2, 3, 4, 5 };
    static List<MixedData> mixedDataList = new ArrayList<MixedData>();
    static Date date = new Date(645342);

    public static Collection<Object[]> parameters() {
        //Adding MixedData to the list
        mixedDataList.add(0,new MixedData(ColumnType.INTEGER, 123L));
        mixedDataList.add(1,new MixedData(ColumnType.FLOAT, 987.123f));
        mixedDataList.add(2,new MixedData(ColumnType.DOUBLE, 1234567.898d));
        mixedDataList.add(3,new MixedData(ColumnType.BOOLEAN, true));
        mixedDataList.add(4,new MixedData(ColumnType.STRING, "abc"));
        mixedDataList.add(5,new MixedData(ColumnType.BINARY, b));
        mixedDataList.add(6,new MixedData(ColumnType.DATE, date));

        return Arrays.asList(
                new Object[] {new ExpectedValue(123L),mixedDataList},
                new Object[] {new ExpectedValue(987.123f),mixedDataList},
                new Object[] {new ExpectedValue(1234567.898d),mixedDataList},
                new Object[] {new ExpectedValue(true),mixedDataList},
                new Object[] {new ExpectedValue("abc"),mixedDataList},
                new Object[] {new ExpectedValue (b),mixedDataList},
                new Object[] {new ExpectedValue(date),mixedDataList}
        );
    }

    public TestJNIMixedType(ExpectedValue expectedValue, ArrayList mixedDataList) {
        this.expectedValue = expectedValue;
        this.mixedDataList = mixedDataList;



    }

    public void testShouldMatchMixedValues() {
        for(int i=0; i < mixedDataList.size();i++){
            for(int j=0; j < mixedDataList.size();j++){
                if(mixedDataList.get(i).value==mixedDataList.get(j).value){
                    assertEquals(mixedDataList.get(i).value, mixedDataList.get(j).value);
                   // System.out.println(mixedDataList.get(i).value + "+" + mixedDataList.get(j).value);
                   // System.out.println(mixedDataList.get(i).type);
                   // System.out.println(mixedDataList.size() + "+" + j + "+" + i);
                }else{
                    assertNotSame(mixedDataList.get(i).value, mixedDataList.get(j).value);
                    
                }
            }
        }
    }

   /* public void testShouldFailOnWrongTypeRetrieval() {
        switch (mixedData.type) {
            case BINARY:
                try {
                    //mixed.getBinaryByteArray();
                    System.out.println(mixedData.type + ": in BINARY");

                    //fail("Wrong mixed type");
                } catch (IllegalMixedTypeException e) { }
                break;
            case DATE:
                try {
                    //mixed.getDateValue();
                    System.out.println(mixedData.type + ": in DATE");
                    //fail("Wrong mixed type");
                } catch (IllegalMixedTypeException e) { }
                break;
            case BOOLEAN:
                try {
                    //mixed.getBooleanValue();
                    System.out.println(mixedData.type + ": in BOOLEAN");
                    //fail("Wrong mixed type");
                } catch (IllegalMixedTypeException e) { }
                break;
            case INTEGER:
                try {
                    //mixed.getLongValue();
                    System.out.println(mixedData.type + ": in INTEGER");
                    //fail("Wrong mixed type");
                } catch (IllegalMixedTypeException e) { }
                break;
            case FLOAT:
                try {
                    //mixed.getFloatValue();
                    System.out.println(mixedData.type + ": in FLOAT");
                    //fail("Wrong mixed type");
                } catch (IllegalMixedTypeException e) { }
                break;
            case DOUBLE:
                try {
                    //mixed.getDoubleValue();
                    System.out.println(mixedData.type + ": in DOUBLE");
                    //fail("Wrong mixed type");
                } catch (IllegalMixedTypeException e) { }
                break;
            case STRING:
                try {
                    //mixed.getStringValue();
                    System.out.println(mixedData.type + ": in STRING");
                    //fail("Wrong mixed type");
                } catch (IllegalMixedTypeException e) { }
                break;
            default:
                System.out.println(mixedData.type + ": in DEFAULT");
                fail("wrong type");
                break;
        }
    }


    public void testShouldStoreValuesOfMixedType() throws Throwable {
        if(mixedData.type == ColumnType.MIXED){
        Table table = new Table();
        table.addColumn(mixedData.type, "mix");

        table.add("test");

        checkMixedCell(table, 0, 0, ColumnType.STRING, "test");

        table.setMixed(0, 0, Mixed.mixedValue(5));

        checkMixedCell(table, 0, 0, ColumnType.INTEGER, 20);

        table.setMixed(0, 0, Mixed.mixedValue(2.4));

        checkMixedCell(table, 0, 0, ColumnType.DOUBLE, 2.4);
        table.close();
    }}
/*
    @Test(dataProvider = "columnTypesProvider")
    public void shouldFailOnWrongTypeRetrieval(ColumnType columnType) {
        Object value = columnType != ColumnType.STRING ? "abc" : 123;
        Mixed mixed = Mixed.mixedValue(value);

        switch (columnType) {
        case BINARY:
            try { 
                mixed.getBinaryByteArray();
                fail("Wrong mixed type"); 
            } catch (IllegalMixedTypeException e) { }
            break;
        case DATE:
            try { 
                mixed.getDateValue();         
                fail("Wrong mixed type"); 
            } catch (IllegalMixedTypeException e) { }
            break;
        case BOOLEAN:
            try { 
                mixed.getBooleanValue();      
                fail("Wrong mixed type"); 
            } catch (IllegalMixedTypeException e) { }
            break;
        case INTEGER:
            try { 
                mixed.getLongValue();         
                fail("Wrong mixed type"); 
            } catch (IllegalMixedTypeException e) { }
            break;
        case FLOAT:
            try { 
                mixed.getFloatValue();        
                fail("Wrong mixed type"); 
            } catch (IllegalMixedTypeException e) { }
            break;
        case DOUBLE:
            try { 
                mixed.getDoubleValue();       
                fail("Wrong mixed type"); 
            } catch (IllegalMixedTypeException e) { }
            break;
        case STRING:
            try { 
                mixed.getStringValue();       
                fail("Wrong mixed type"); 
            } catch (IllegalMixedTypeException e) { }
            break;
        default:
            fail("wrong type");
            break;
        }
    }

    @Test(dataProvider = "mixedValuesProvider")
    public void shouldStoreValuesOfMixedType(MixedData value1,
    										 MixedData value2, MixedData value3) throws Throwable {
        Table table = new Table();
        table.addColumn(ColumnType.MIXED, "mix");

        table.add(value1.value);

        checkMixedCell(table, 0, 0, value1.type, value1.value);

        table.setMixed(0, 0, Mixed.mixedValue(value2.value));

        checkMixedCell(table, 0, 0, value2.type, value2.value);

        table.setMixed(0, 0, Mixed.mixedValue(value3.value));

        checkMixedCell(table, 0, 0, value3.type, value3.value);
        table.close();
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

    @DataProvider(name = "mixedValuesProvider")
    public Iterator<Object[]> mixedValuesProvider() {
        Object[] values = {
                new MixedData(ColumnType.BOOLEAN, true),
                new MixedData(ColumnType.STRING, "abc"),
                new MixedData(ColumnType.INTEGER, 123L),
                new MixedData(ColumnType.FLOAT, 987.123f),
                new MixedData(ColumnType.DOUBLE, 1234567.898d),
                new MixedData(ColumnType.DATE, new Date(645342)),
                new MixedData(ColumnType.BINARY, new byte[] { 1, 2, 3, 4, 5 }) };

        List<?> mixedValues = Arrays.asList(values);
        return DataProviderUtil.allCombinations(mixedValues, mixedValues,
                mixedValues);
    }

    @DataProvider(name = "columnTypesProvider")
    public Object[][] columnTypesProvider() {
        Object[][] values = { 
                {ColumnType.BOOLEAN},
                {ColumnType.STRING}, 
                {ColumnType.INTEGER},
                {ColumnType.FLOAT}, 
                {ColumnType.DOUBLE},
                {ColumnType.DATE}, 
                {ColumnType.BINARY} 
        };

        return values;
    }*/

    public static Test suite() {
        return new RealmTestSuite(TestJNIMixedType.class, parameters());

    }
}

