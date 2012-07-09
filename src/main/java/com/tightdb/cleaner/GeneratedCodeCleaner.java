package com.tightdb.cleaner;

import java.io.File;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

public class GeneratedCodeCleaner {

	public int removeObsoleteGeneratedCode(String path, long beforeTimestamp) {
		File directory = new File(path);
		if (directory.exists()) {
//			context.getLogger().info("Scanning source files directory: {}", directory);

			IOFileFilter fileFilter = new AndFileFilter(new SuffixFileFilter(".java"), new ObsoleteGeneratedCodeFilter());
			IOFileFilter dirFilter = TrueFileFilter.TRUE;
			Collection<File> files = FileUtils.listFiles(directory, fileFilter, dirFilter);

			int removedFilesCount = 0;
			for (File file : files) {
				if (file.lastModified() < beforeTimestamp) {
//					context.getLogger().info("Removing obsolete generated file: {}", file);
					removedFilesCount++;
					try {
						if (!file.delete()) {
//							context.getLogger().warn("Couldn't immediately delete file: {}, scheduled delete on exit!", file);
							file.deleteOnExit();
						}
					} catch (Exception e) {
//						context.getLogger().warn("Couldn't delete file due to security constraints: {}, scheduled delete on exit!", file);
						file.deleteOnExit();
					}
				}
			}

			return removedFilesCount;
		} else {
			throw new RuntimeException("Cannot find folder: " + path);
		}
	}
}
