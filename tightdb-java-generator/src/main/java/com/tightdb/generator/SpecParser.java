package com.tightdb.generator;

import java.util.regex.Pattern;

public class SpecParser {

    @SuppressWarnings("unused")
    private AnnotationProcessingLogger logger;

    public SpecParser(AnnotationProcessingLogger logger) {
        this.logger = logger;
    }

    public String removeComments(String source) {
        source = source.replaceAll("(?sm)" + Pattern.quote("/*") + "" + Pattern.quote("*/"), "");
        source = source.replaceAll("(?sm)//.*?\n", "\n");
        return source;
    }

    public String[] parseWords(String source) {
        return removeComments(source).split("[^\\w_$]+");
    }
}
