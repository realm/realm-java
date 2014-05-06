package com.realm.generator;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

public class FieldSorter {

    private AnnotationProcessingLogger logger;
    private TableSpecReader specReader;

    public FieldSorter(AnnotationProcessingLogger logger, String[] sourceFolders) {
        this.logger = logger;
        this.specReader = new TableSpecReader(logger, sourceFolders);
    }

    public void sortFields(List<VariableElement> fields, TypeElement model, List<File> sourcesPath) {
        String specSource = specReader.getSpecFields(model, sourcesPath);
        if (specSource == null) {
            logger.warn("Field sorting failed, couldn't find table spec: " + model.getSimpleName());
            return;
        }

        Set<String> fieldNames = new HashSet<String>();
        for (VariableElement field : fields) {
            fieldNames.add(field.getSimpleName().toString());
        }

        SpecParser parser = new SpecParser(logger);
        List<String> words = new ArrayList<String>();
        words.addAll(Arrays.asList(parser.parseWords(specSource)));
        words.retainAll(fieldNames);

        final Map<String, Integer> indexes = new HashMap<String, Integer>();
        for (int i = 0; i < words.size(); i++) {
            indexes.put(words.get(i), i);
        }

        if (indexes.size() == fields.size()) {
            Collections.sort(fields, new Comparator<VariableElement>() {
                @Override
                public int compare(VariableElement el1, VariableElement el2) {
                    return position(el1.getSimpleName().toString()) - position(el2.getSimpleName().toString());
                }

                private int position(String name) {
                    Integer pos = indexes.get(name);
                    return pos != null ? pos : 0;
                }
            });
            logger.info("Successfully sorted fields: " + fields);
        } else {
            logger.warn("Field sorting failed!");
        }
    }

}
