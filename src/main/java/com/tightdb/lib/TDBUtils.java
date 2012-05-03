package com.tightdb.lib;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class TDBUtils {

	private static boolean loadedLibrary;

	public static byte[] serialize(Serializable value) {
		try {
			ByteArrayOutputStream mem = new ByteArrayOutputStream();
			ObjectOutputStream output = new ObjectOutputStream(mem);
			output.writeObject(value);
			output.close();
			return mem.toByteArray();
		} catch (Exception e) {
			throw new RuntimeException("Cannot serialize the object!", e);
		}
	}

	public static Serializable deserialize(byte[] value) {
		try {
			ByteArrayInputStream mem = new ByteArrayInputStream(value);
			ObjectInputStream output = new ObjectInputStream(mem);
			Object obj = output.readObject();
			output.close();
			return (Serializable) obj;
		} catch (Exception e) {
			throw new RuntimeException("Cannot deserialize the object!", e);
		}
	}

	public static void printTable(AbstractTable<? extends AbstractCursor<?>, ? extends AbstractView<?, ?>, ?> table) {
		String format = "%-15s| ";
		System.out.println(String.format("================== Table %s ====================", table.getName()));
		if (!table.isEmpty()) {
			for (AbstractColumn<?, ?, ?> column : table.at(0).columns()) {
				System.out.print(String.format(format, column.getName()));
			}
			System.out.println();

			for (int i = 0; i < table.size(); i++) {
				AbstractCursor<?> p = table.at(i);
				for (AbstractColumn<?, ?, ?> column : p.columns()) {
					System.out.print(String.format(format, column.getReadable()));
				}
				System.out.println();
			}
		} else {
			System.out.println(" - the table is empty");
		}
	}

	public static void loadLibrary() {
		if (!loadedLibrary) {
			System.loadLibrary("tightdb");
			loadedLibrary = true;
		}
	}

}
