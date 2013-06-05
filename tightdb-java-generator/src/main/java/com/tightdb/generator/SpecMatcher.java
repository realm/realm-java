package com.tightdb.generator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpecMatcher {

    public AnnotationProcessingLogger logger;

    public SpecMatcher(AnnotationProcessingLogger logger) {
        this.logger = logger;
    }

    public String matchSpec(String modelName, String source) {
        Matcher m = Pattern.compile("(?sm)class\\s+" + modelName + "\\s*\\{(.+?)\\}").matcher(source);
        if (m.find()) {
            String specSource = m.group(1).trim();
            return specSource;
        } else {
            return null;
        }
    }

}