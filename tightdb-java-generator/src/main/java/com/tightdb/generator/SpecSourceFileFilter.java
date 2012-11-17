package com.tightdb.generator;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;

public class SpecSourceFileFilter implements IOFileFilter {

	private final SpecMatcher specMatcher;

	private final String modelName;

	private final AnnotationProcessingLogger logger;

	public SpecSourceFileFilter(SpecMatcher specMatcher, String modelName, AnnotationProcessingLogger logger) {
		this.specMatcher = specMatcher;
		this.modelName = modelName;
		this.logger = logger;
	}

	@Override
	public boolean accept(File file) {
		logger.debug("Checking file for potential match: " + file);
		try {
			String source;
			try {
				source = FileUtils.readFileToString(file);
			} catch (IOException e) {
				logger.warn("Couldn't read file: " + file);
				return false;
			}

			String spec = specMatcher.matchSpec(modelName, source);
			if (spec != null) {
				logger.debug("Found matching file: " + file);
			}
			return spec != null;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public boolean accept(File dir, String name) {
		return true;
	}

}
