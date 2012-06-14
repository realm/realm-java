package com.tightdb;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.tightdb.ColumnType;
import com.tightdb.TableBase;
import com.tightdb.TableSpec;
import com.tightdb.test.DataProviderUtil;

public class JNITableSpecTest {

	@Test(dataProvider = "oneColumn")
	public void shouldDefineOneColumnTable(ColumnType columnType) {
		TableSpec spec = new TableSpec();
		spec.addColumn(columnType, "foo");

		TableBase table = new TableBase();
		table.updateFromSpec(spec);
	}

	@Test(dataProvider = "twoColumns")
	public void shouldDefineTwoColumnsTable(ColumnType columnType, ColumnType columnType2) {
		TableSpec spec = new TableSpec();
		spec.addColumn(columnType, "foo");

		TableSpec subspec = spec.addSubtableColumn("bar");
		subspec.addColumn(columnType2, "subbar");

		TableBase table = new TableBase();
		table.updateFromSpec(spec);
	}

	@DataProvider(name = "oneColumn")
	public Iterator<Object[]> oneColumn() {
		return DataProviderUtil.allCombinations(columnTypes());
	}

	@DataProvider(name = "twoColumns")
	public Iterator<Object[]> twoColumns() {
		return DataProviderUtil.allCombinations(columnTypes(), columnTypes());
	}

	private List<?> columnTypes() {
		return Arrays.asList(ColumnType.values());
	}

}
