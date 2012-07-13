package com.tightdb.generator;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.lang.model.element.TypeElement;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;

public class TableSpecReader {

	private AnnotationProcessingLogger logger;

	private SpecMatcher specMatcher;

	public TableSpecReader(AnnotationProcessingLogger logger) {
		this.logger = logger;
		this.specMatcher = new SpecMatcher(logger);
	}

	public String getSpecFields(TypeElement model, File sourcePath) {
		String modelName = model.toString();

		File sourceFile = findSourceFile(sourcePath, modelName);
		if (sourceFile == null) {
			logger.error("Table spec retrieval failed!");
			return null;
		}

		logger.info("Searching table spec in file: " + sourceFile);
		String source;
		try {
			source = FileUtils.readFileToString(sourceFile);
		} catch (IOException e) {
			logger.error("Table spec retrieval failed, couldn't read file: " + sourceFile);
			return null;
		}

		String spec = specMatcher.matchSpec(model.getSimpleName().toString(), source);
		if (spec == null) {
			logger.error("Table spec retrieval failed, couldn't find table spec: " + modelName);
		}
		return spec;

	}

	private File findSourceFile(File sourcePath, String modelName) {
		String[] modelNameParts = modelName.split("\\.");

		for (String part : modelNameParts) {
			File path = new File(sourcePath.getAbsolutePath() + File.separator + part);
			if (path.isDirectory()) {
				sourcePath = path;
			} else {
				File sourceFile = new File(path.getAbsolutePath() + ".java");
				if (sourceFile.exists() && sourceFile.isFile()) {
					return sourceFile;
				} else {
					logger.warn("The file doesn't exist: " + sourceFile);
					return scanSourcePath(sourcePath, modelNameParts[modelNameParts.length - 1]);
				}
			}
		}

		// this should never execute
		return null;
	}

	private File scanSourcePath(File sourcePath, String modelName) {
		logger.debug("Scanning source path '" + sourcePath + "' for table spec '" + modelName + "'");
		IOFileFilter fileFilter = new AndFileFilter(new SuffixFileFilter(".java"), new SpecSourceFileFilter(specMatcher, modelName, logger));
		IOFileFilter dirFilter = FalseFileFilter.FALSE;
		Collection<File> files = FileUtils.listFiles(sourcePath, fileFilter, dirFilter);

		switch (files.size()) {
		case 0:
			return null;
		case 1:
			return files.iterator().next();
		default:
			logger.warn("More than one source files were found containing the table specs with the same name!");
			return files.iterator().next();
		}

	}

}
