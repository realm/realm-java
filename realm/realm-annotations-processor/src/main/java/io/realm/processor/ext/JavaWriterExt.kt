/*
 * Copyright 2019 Realm Inc.
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
 
package io.realm.processor.ext

import com.squareup.javawriter.JavaWriter
import io.realm.processor.QualifiedClassName
import io.realm.processor.SimpleClassName
import javax.lang.model.element.Modifier

fun JavaWriter.beginType(type: QualifiedClassName,
                         kind: String,
                         modifiers: Set<Modifier>,
                         extendsType: QualifiedClassName,
                         implementsType: Array<String>): JavaWriter {
    return this.beginType(type.toString(), kind, modifiers, extendsType.toString(), *implementsType)
}

fun JavaWriter.beginType(type: QualifiedClassName,
                         kind: String,
                         modifiers: Set<Modifier>,
                         extendsType: QualifiedClassName,
                         implementsType: Array<SimpleClassName>): JavaWriter {
    val types: Array<String> = implementsType.map { it.toString() }.toTypedArray()
    return this.beginType(type.toString(), kind, modifiers, extendsType.toString(), *types)
}

fun JavaWriter.beginMethod(returnType: QualifiedClassName,
                           name: String,
                           modifiers: Set<Modifier>,
                           vararg parameters: String): JavaWriter {
    return this.beginMethod(returnType.toString(), name, modifiers, *parameters)
}

fun JavaWriter.beginMethod(returnType: QualifiedClassName,
                           name: String,
                           modifiers: Set<Modifier>,
                           parameters: List<String>,
                           throwsTypes: List<String>): JavaWriter {
    return this.beginMethod(returnType.toString(), name, modifiers, parameters, throwsTypes)
}

