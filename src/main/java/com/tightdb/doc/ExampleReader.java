package com.tightdb.doc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

public class ExampleReader {

	private final String content;

	public ExampleReader(String filename) throws IOException {
		InputStream input = getClass().getClassLoader().getResourceAsStream(filename);
		content = IOUtils.toString(input);
	}

	public String getExample(String name) throws IOException {
		String start = Pattern.quote(String.format("/* EXAMPLE: %s */", name));
		String end = Pattern.quote("/* EXAMPLE: ");

		String regex = "(?smi)" + start + "(.+?)" + end;
		Matcher m = Pattern.compile(regex).matcher(content);

		if (m.find()) {
			return removeIndentation(m.group(1).trim());
		} else {
			return "";
		}
	}

	private String removeIndentation(String s) throws IOException {
		List<String> lines = IOUtils.readLines(new ByteArrayInputStream(s.getBytes()));
		List<String> lines2 = new LinkedList<String>();
		for (String line : lines) {
			lines2.add(line.trim());
		}
		return StringUtils.join(lines2, "\n");
	}
}
