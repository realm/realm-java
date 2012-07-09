package com.tightdb.generator;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.lang.model.element.TypeElement;

import org.apache.commons.io.FileUtils;

public class TableSpecReader {

	private AnnotationProcessingLogger logger;

	public TableSpecReader(AnnotationProcessingLogger logger) {
		this.logger = logger;
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

		Matcher m = Pattern.compile("(?sm)class\\s+" + model.getSimpleName() + "\\s*\\{(.+?)\\}").matcher(source);
		if (m.find()) {
			String specSource = m.group(1).trim();
			return specSource;
		} else {
			logger.error("Table spec retrieval failed, couldn't find table spec: " + model.getSimpleName());
			return null;
		}

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
					logger.error("The file doesn't exist: " + sourceFile);
					return null;
				}
			}
		}

		// this should never execute
		return null;
	}

}
