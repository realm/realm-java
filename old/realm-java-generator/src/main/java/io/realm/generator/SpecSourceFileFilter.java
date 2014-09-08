/*
 * Copyright 2014 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.realm.generator;

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
