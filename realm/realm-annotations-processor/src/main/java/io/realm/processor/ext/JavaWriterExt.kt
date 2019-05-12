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

