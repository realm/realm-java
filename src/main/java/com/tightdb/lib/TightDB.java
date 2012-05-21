package com.tightdb.lib;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class TightDB {

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

	public static void print(String caption, AbstractRowset<? extends AbstractCursor<?>, ?, ?> rowset) {
		String format = "%-15s| ";
		System.out.println(String.format("================== %s ====================", caption));
		if (!rowset.isEmpty()) {
			for (AbstractColumn<?, ?, ?> column : rowset.at(0).columns()) {
				System.out.print(String.format(format, column.getName()));
			}
			System.out.println();

			for (int i = 0; i < rowset.size(); i++) {
				AbstractCursor<?> p = rowset.at(i);
				for (AbstractColumn<?, ?, ?> column : p.columns()) {
					System.out.print(String.format(format, column.getReadableValue()));
				}
				System.out.println();
			}
			System.out.println();
		} else {
			System.out.println(" - No records to show!");
		}
	}

	public static void print(AbstractRowset<? extends AbstractCursor<?>, ?, ?> rowset) {
		print(rowset.getName(), rowset);
	}

	public static void print(String caption, AbstractCursor<?> cursor) {
		System.out.println(caption + ": " + cursor);
	}

	public static void loadLibrary() {
		if (!loadedLibrary) {
			loadedLibrary = loadCorrectLibrary("tightdbd", "tightdb32", "tightdb64");
			if (!loadedLibrary) {
				throw new RuntimeException("Couldn't load the TightDB library!");
			}
		}
	}

	private static boolean loadCorrectLibrary(String... libraryCandidateNames) {
		for (String libraryCandidateName : libraryCandidateNames) {
			try {
				System.loadLibrary(libraryCandidateName);
				return true;
			} catch (Throwable e) {
			}
		}

		return false;
	}

}
