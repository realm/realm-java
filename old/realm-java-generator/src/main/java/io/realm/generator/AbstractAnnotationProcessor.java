package io.realm.generator;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.FileObject;

import io.realm.DefineTable;

public abstract class AbstractAnnotationProcessor extends AbstractProcessor {

    private static final String[] SUPPORTED_ANNOTATIONS = { DefineTable.class.getCanonicalName() };

    protected Elements elementUtils;
    protected Types typeUtils;
    protected Filer filer;
    protected Map<String, String> options;
    protected AnnotationProcessingLogger logger;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        logger.info("Entering annotation processor...");

        // this is a hack that detects if the annotation processing runs inside Eclipse APT
        boolean insideEclipse = env.getClass().getCanonicalName()
                .startsWith("org.eclipse.jdt.");
        if (insideEclipse) {
            logger.debug("Detected Eclipse, the appropriate work-arounds will be activated...");
        }

        if (!env.processingOver()) {
            logger.info("Processing annotations...");
            try {
                processAnnotations(annotations, env, insideEclipse);
                logger.info("Successfully finished processing.");
            } catch (Exception e) {
                String info = e.getMessage() != null ? "(" + e.getMessage() + ")" : "";
                String msg = e.getClass().getCanonicalName() + " " + info + "\n\n" + StringUtils.join(e.getStackTrace(), "\n");

                Throwable cause = e.getCause();
                while (cause != null) {
                    info = cause.getMessage() != null ? cause.getMessage() : "";
                    msg += "\n\nCause: " + info + "\n" + StringUtils.join(cause.getStackTrace(), "\n");
                    cause = cause.getCause();
                }

                logger.error(msg);
            }
        } else {
            logger.info("Last round, processing is done.");
        }
        return true;
    }

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);

        Messager messager = env.getMessager(); // required for logging
        logger = new AnnotationProcessingLogger(messager);
        logger.info("Initializing annotation processor...");

        elementUtils = env.getElementUtils();
        typeUtils = env.getTypeUtils();
        filer = env.getFiler();
        options = env.getOptions();

        logger.info("Initialization finished.");
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        logger.info("Specifying supported annotations...");
        return new HashSet<String>(Arrays.asList(SUPPORTED_ANNOTATIONS));
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    protected abstract void processAnnotations(Set<? extends TypeElement> annotations, RoundEnvironment env, boolean insideEclipse) throws Exception;

    protected void writeToSourceFile(String pkg, String filename, String content, Element... originatingElements) {
        Writer writer = null;
        try {
            String name = !pkg.isEmpty() ? pkg + "." + filename : filename;
            logger.info("Writing source file: " + name);
            FileObject fileRes = filer.createSourceFile(name, originatingElements);
            writer = fileRes.openWriter();
            writer.write(content);
        } catch (IOException e) {
            logger.warn("Couldn't write to file: " + filename);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    logger.warn("Couldn't write to file: " + filename);
                }
            }
        }
    }

}
