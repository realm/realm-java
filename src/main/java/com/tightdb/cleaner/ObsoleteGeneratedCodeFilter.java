package com.tightdb.cleaner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.apache.commons.io.filefilter.IOFileFilter;

import com.tightdb.generator.CodeGenerator;

public class ObsoleteGeneratedCodeFilter implements IOFileFilter {

	@Override
	public boolean accept(File file) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String firstLine = reader.readLine();
			return CodeGenerator.INFO_GENERATED.equals(firstLine);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean accept(File dir, String name) {
		return true;
	}

}
