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

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic.Kind;

public class AnnotationProcessingLogger {

    protected Messager messager;

    public AnnotationProcessingLogger(Messager messager) {
        this.messager = messager;
    }

    public void debug(String msg) {
        messager.printMessage(Kind.NOTE, msg);
    }

    protected void info(String msg) {
        messager.printMessage(Kind.NOTE, msg);
    }

    protected void info(String msg, Element element) {
        messager.printMessage(Kind.NOTE, msg, element);
    }

    protected void warn(String msg) {
        messager.printMessage(Kind.WARNING, msg);
    }

    protected void warn(String msg, Element element) {
        messager.printMessage(Kind.WARNING, msg, element);
    }

    protected void error(String msg) {
        messager.printMessage(Kind.ERROR, msg);
    }

    protected void error(String msg, Element element) {
        messager.printMessage(Kind.ERROR, msg, element);
    }

}
