package io.realm.processor;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import io.realm.annotations.Ignore;
import io.realm.annotations.RealmClass;



@SupportedAnnotationTypes({"io.realm.annotations.RealmClass", "io.realm.annotations.Ignore"})
@SupportedSourceVersion(javax.lang.model.SourceVersion.RELEASE_6)
public class RealmProcessor extends AbstractProcessor {
	RealmSourceCodeGenerator codeGenerator = new RealmSourceCodeGenerator();
	   @Override
	    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

	        for (Element classElement : roundEnv.getElementsAnnotatedWith(Ignore.class)) {
	            // Check the annotation was applied to a FIELD
	            if (!classElement.getKind().equals(ElementKind.FIELD)) {
	                error("The Ignore annotation can only be applied to Fields");
	                return false;
	            }
	            if (!codeGenerator.addIgnore(classElement.getSimpleName().toString()))
	            {
	            	error(codeGenerator.getError());
	            	return false;
	            }
	        }

		   for (Element classElement : roundEnv.getElementsAnnotatedWith(RealmClass.class)) {
	            // Check the annotation was applied to a Class
	            if (!classElement.getKind().equals(ElementKind.CLASS)) {
	                error("The RealmClass annotation can only be applied to classes");
	                return false;
	            }
	            TypeElement typeElement = (TypeElement) classElement;
	            
	            // Get the package of the class
	            Element enclosingElement = typeElement.getEnclosingElement();
	            if (!enclosingElement.getKind().equals(ElementKind.PACKAGE)) {
	                error("The RealmClass annotation does not support nested classes");
	                return false;
	            }

	            	PackageElement packageElement = (PackageElement) enclosingElement;
	            	String qName = packageElement.getQualifiedName().toString();
	            	
	            	if (qName != null)
	            	{
	            		String qualifiedClassName = qName + "."+classElement.getSimpleName()+"RealmProxy";
	            		//qualifiedClassName = qualifiedClassName.replace(".", "/");

                        JavaFileObject jfo = null;
                        try {
                            jfo = processingEnv.getFiler().createSourceFile(qualifiedClassName);
                        } catch (IOException e) {
                            e.printStackTrace();
                            error("Unable to create file: " + e.getMessage());
                            return false;
                        }

                        try {
                            codeGenerator.setBufferedWriter(new BufferedWriter(jfo.openWriter()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        try {
                            if (!codeGenerator.setPackageName(qName))
                            {
                                error(codeGenerator.getError());
                                return false;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        try {
                            if (!codeGenerator.setClassName(classElement.getSimpleName().toString()))
                            {
                                error(codeGenerator.getError());
                                return false;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        for (Element element : typeElement.getEnclosedElements()) {
			                if (element.getKind().equals(ElementKind.FIELD)) 
			                {
			                	String elementName = element.getSimpleName().toString();
			                	VariableElement varElem = (VariableElement)element;
			                	
			                	Set<Modifier> modifiers = varElem.getModifiers();

                                for (Modifier modifier : modifiers) {
                                    if (modifier == Modifier.PRIVATE) {
                                        try {
                                            if (!codeGenerator.setField(elementName, varElem)) {
                                                error(codeGenerator.getError());
                                                return false;
                                            }
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
			                }
			            }
                        try {
                            if (!codeGenerator.generate())
                            {
                                error(codeGenerator.getError());
                                return false;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
	            }
	        
	        return true;
	    }

	    private void error(String message) {
	        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message);
	    }
	    
	    
}
