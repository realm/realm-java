package com.tightdb;

import static org.testng.AssertJUnit.*;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.tightdb.lib.TightDB;
import com.tightdb.test.DataProviderUtil;
import com.tightdb.test.MixedData;

public class JNIMixedTypeTest {

	@Test(dataProvider = "mixedValuesProvider")
	public void shouldStoreValuesOfMixedType(MixedData value1, MixedData value2, MixedData value3) throws Exception {
		TableBase table = new TableBase();

		TableSpec tableSpec = new TableSpec();
		tableSpec.addColumn(ColumnType.ColumnTypeMixed, "mix");
		table.updateFromSpec(tableSpec);

		table.insertMixed(0, 0, Mixed.mixedValue(value1.value));
		table.insertDone();

		checkMixedCell(table, 0, 0, value1.type, value1.value);

		table.setMixed(0, 0, Mixed.mixedValue(value2.value));

		checkMixedCell(table, 0, 0, value2.type, value2.value);

		table.setMixed(0, 0, Mixed.mixedValue(value3.value));

		checkMixedCell(table, 0, 0, value3.type, value3.value);
	}

	private void checkMixedCell(TableBase table, long col, long row, ColumnType columnType, Object value) throws IllegalAccessException {
		ColumnType mixedType = table.getMixedType(col, row);
		assertEquals(columnType, mixedType);
	
		Mixed mixed = table.getMixed(col, row);
		if (columnType == ColumnType.ColumnTypeBinary) {
			if (mixed.getBinaryType() == Mixed.BINARY_TYPE_BYTE_ARRAY) {
				byte[] bin = mixed.getBinaryByteArray();
				assertEquals(Mixed.mixedValue(value), bin);
			} else {
				// TODO: This is not fully working...
				ByteBuffer binBuf = mixed.getBinaryValue();
				assertEquals(Mixed.mixedValue(value), binBuf);
			}
		} else {		
			assertEquals(value, mixed.getValue());
		}	
	}

	@DataProvider(name = "mixedValuesProvider")
	public Iterator<Object[]> mixedValuesProvider() {
		Object[] values = { 
				new MixedData(ColumnType.ColumnTypeBool, true), 
				new MixedData(ColumnType.ColumnTypeString, "abc"),
				new MixedData(ColumnType.ColumnTypeInt, 123L), 
				new MixedData(ColumnType.ColumnTypeDate, new Date(645342)), 
				new MixedData(ColumnType.ColumnTypeBinary, new byte[] { 1, 2, 3, 4, 5 }) 
		};
		
		List<?> mixedValues = Arrays.asList(values);
		return DataProviderUtil.allCombinations(mixedValues, mixedValues, mixedValues);
	}
}
