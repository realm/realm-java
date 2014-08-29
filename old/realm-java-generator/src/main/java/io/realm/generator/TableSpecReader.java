package io.realm.generator;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.lang.model.element.TypeElement;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;

public class TableSpecReader {

    private AnnotationProcessingLogger logger;

    private SpecMatcher specMatcher;

    public TableSpecReader(AnnotationProcessingLogger logger,
            String[] sourceFolders) {
        this.logger = logger;
        this.specMatcher = new SpecMatcher(logger);
    }

    public String getSpecFields(TypeElement model, List<File> sourcesPath) {
        String modelName = model.toString();

        File sourceFile = findSourceFile(sourcesPath, modelName);
        if (sourceFile == null) {
            logger.warn("Table spec retrieval failed!");
            return null;
        }

        logger.info("Searching table spec in file: " + sourceFile);
        String source;
        try {
            source = FileUtils.readFileToString(sourceFile);
        } catch (IOException e) {
            logger.warn("Table spec retrieval failed, couldn't read file: "
                    + sourceFile);
            return null;
        }

        String spec = specMatcher.matchSpec(model.getSimpleName().toString(),
                source);
        if (spec == null) {
            logger.warn("Table spec retrieval failed, couldn't find table spec: "
                    + modelName);
        }
        return spec;

    }

    private File findSourceFile(List<File> sourceFolders, String modelName) {
        String[] modelNameParts = modelName.split("\\.");

        for (File sourceFolder : sourceFolders) {
            logger.debug("Searching sources in the folder: " + sourceFolder);
            File folder = sourceFolder;
            for (String part : modelNameParts) {
                File path = new File(folder, part);
                if (path.isDirectory()) {
                    folder = path;
                } else {
                    File sourceFile = new File(path.getAbsolutePath() + ".java");
                    if (sourceFile.exists() && sourceFile.isFile()) {
                        return sourceFile;
                    }
                }
            }

            // if the expected file wasn't found, search all of them
            File sf = scanSourcePath(folder,
                    modelNameParts[modelNameParts.length - 1]);
            if (sf != null) {
                return sf;
            }
        }

        // this should never execute
        return null;
    }

    private File scanSourcePath(File sourcePath, String modelName) {
        logger.debug("Scanning source path '" + sourcePath
                + "' for table spec '" + modelName + "'");
        IOFileFilter fileFilter = new AndFileFilter(new SuffixFileFilter(
                ".java"), new SpecSourceFileFilter(specMatcher, modelName,
                logger));
        IOFileFilter dirFilter = FalseFileFilter.FALSE;
        Collection<File> files = FileUtils.listFiles(sourcePath, fileFilter,
                dirFilter);

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
