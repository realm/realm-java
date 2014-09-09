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
                String qualifiedPackageName = packageElement.getQualifiedName().toString();
        	
	            if (qualifiedPackageName != null) {
                	String qualifiedClassName = qualifiedPackageName + "." + classElement.getSimpleName() + "RealmProxy";
                	qualifiedClassName = qualifiedClassName.replace(".", "/");

                	JavaFileObject javaFileObject = null;
                	BufferedWriter bufferWriter = null;
                	
                	try {
                		javaFileObject = processingEnv.getFiler().createSourceFile(qualifiedClassName);
                		bufferWriter = new BufferedWriter(javaFileObject.openWriter());
                	} catch (IOException e) {
                		e.printStackTrace();
                		error("Unable to create file: " + e.getMessage());
                		return false;
                	}

                	if (!codeGenerator.setBufferedWriter(bufferWriter)) {
                        error(codeGenerator.getError());
                        return false;
                    }

                    if (!codeGenerator.setPackageName(qualifiedPackageName)) {
                        error(codeGenerator.getError());
                        return false;
                    }

                    if (!codeGenerator.setClassName(classElement.getSimpleName().toString())) {
                        error(codeGenerator.getError());
                        return false;
                    }
                
                    for (Element element : typeElement.getEnclosedElements()) {
		                if (element.getKind().equals(ElementKind.FIELD)) {
	                        String elementName = element.getSimpleName().toString();
	                        VariableElement varElem = (VariableElement)element;
			                
                            if (varElem.getAnnotation(Ignore.class) != null) {
			                    continue;
			                } 
                            
			                Set<Modifier> modifiers = varElem.getModifiers();
                            for (Modifier modifier : modifiers) {
	                            if (modifier == Modifier.PRIVATE) {
                                    if (!codeGenerator.setField(elementName, varElem)) {
                                        error(codeGenerator.getError());
                                        return false;
                                    }
	                            }
	                        }
			            }
		            }
                    try {
                        if (!codeGenerator.generate()) {
                            error(codeGenerator.getError());
                            return false;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        return false;
                    }
                }
            }
            return true;
        }

    private void error(String message) {
	    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message);
    }

}
