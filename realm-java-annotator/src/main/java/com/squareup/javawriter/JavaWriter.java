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

package com.squareup.javawriter;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.lang.model.element.Modifier;

import static javax.lang.model.element.Modifier.ABSTRACT;

/** A utility class which aids in generating Java source files. */
public class JavaWriter implements Closeable {
  private static final Pattern TYPE_TRAILER = Pattern.compile("(.*?)(\\.\\.\\.|(?:\\[\\])+)$");
  private static final Pattern TYPE_PATTERN = Pattern.compile("(?:[\\w$]+\\.)*([\\w\\.*$]+)");
  private static final int MAX_SINGLE_LINE_ATTRIBUTES = 3;
  private static final String INDENT = "  ";

  /** Map fully qualified type names to their short names. */
  private final Map<String, String> importedTypes = new LinkedHashMap<String, String>();

  private String packagePrefix;
  private final Deque<Scope> scopes = new ArrayDeque<Scope>();
  private final Deque<String> types = new ArrayDeque<String>();
  private final Writer out;
  private boolean isCompressingTypes = true;
  private String indent = INDENT;

  /**
   * @param out the stream to which Java source will be written. This should be a buffered stream.
   */
  public JavaWriter(Writer out) {
    this.out = out;
  }

  public void setCompressingTypes(boolean isCompressingTypes) {
    this.isCompressingTypes = isCompressingTypes;
  }

  public boolean isCompressingTypes() {
    return isCompressingTypes;
  }

  public void setIndent(String indent) {
    this.indent = indent;
  }

  public String getIndent() {
    return indent;
  }

  /** Emit a package declaration and empty line. */
  public JavaWriter emitPackage(String packageName) throws IOException {
    if (this.packagePrefix != null) {
      throw new IllegalStateException();
    }
    if (packageName.isEmpty()) {
      this.packagePrefix = "";
    } else {
      out.write("package ");
      out.write(packageName);
      out.write(";\n\n");
      this.packagePrefix = packageName + ".";
    }
    return this;
  }

  /**
   * Emit an import for each {@code type} provided. For the duration of the file, all references to
   * these classes will be automatically shortened.
   */
  public JavaWriter emitImports(String... types) throws IOException {
    return emitImports(Arrays.asList(types));
  }

  /**
   * Emit an import for each {@code type} provided. For the duration of the file, all references to
   * these classes will be automatically shortened.
   */
  public JavaWriter emitImports(Class<?>... types) throws IOException {
    List<String> classNames = new ArrayList<String>(types.length);
    for (Class<?> classToImport : types) {
      classNames.add(classToImport.getCanonicalName());
    }
    return emitImports(classNames);
  }

  /**
   * Emit an import for each {@code type} in the provided {@code Collection}. For the duration of
   * the file, all references to these classes will be automatically shortened.
   */
  public JavaWriter emitImports(Collection<String> types) throws IOException {
    for (String type : new TreeSet<String>(types)) {
      Matcher matcher = TYPE_PATTERN.matcher(type);
      if (!matcher.matches()) {
        throw new IllegalArgumentException(type);
      }
      if (importedTypes.put(type, matcher.group(1)) != null) {
        throw new IllegalArgumentException(type);
      }
      out.write("import ");
      out.write(type);
      out.write(";\n");
    }
    return this;
  }

  /**
   * Emit a static import for each {@code type} provided. For the duration of the file,
   * all references to these classes will be automatically shortened.
   */
  public JavaWriter emitStaticImports(String... types) throws IOException {
    return emitStaticImports(Arrays.asList(types));
  }

  /**
   * Emit a static import for each {@code type} in the provided {@code Collection}. For the
   * duration of the file, all references to these classes will be automatically shortened.
   */
  public JavaWriter emitStaticImports(Collection<String> types) throws IOException {
    for (String type : new TreeSet<String>(types)) {
      Matcher matcher = TYPE_PATTERN.matcher(type);
      if (!matcher.matches()) {
        throw new IllegalArgumentException(type);
      }
      if (importedTypes.put(type, matcher.group(1)) != null) {
        throw new IllegalArgumentException(type);
      }
      out.write("import static ");
      out.write(type);
      out.write(";\n");
    }
    return this;
  }

  /**
   * Emits a name like {@code java.lang.String} or {@code java.util.List<java.lang.String>},
   * compressing it with imports if possible. Type compression will only be enabled if
   * {@link #isCompressingTypes} is true.
   */
  private JavaWriter emitCompressedType(String type) throws IOException {
    if (isCompressingTypes) {
      out.write(compressType(type));
    } else {
      out.write(type);
    }
    return this;
  }

  /** Try to compress a fully-qualified class name to only the class name. */
  public String compressType(String type) {
    Matcher trailer = TYPE_TRAILER.matcher(type);
    if (trailer.matches()) {
      type = trailer.group(1);
    }

    StringBuilder sb = new StringBuilder();
    if (this.packagePrefix == null) {
      throw new IllegalStateException();
    }

    Matcher m = TYPE_PATTERN.matcher(type);
    int pos = 0;
    while (true) {
      boolean found = m.find(pos);

      // Copy non-matching characters like "<".
      int typeStart = found ? m.start() : type.length();
      sb.append(type, pos, typeStart);

      if (!found) {
        break;
      }

      // Copy a single class name, shortening it if possible.
      String name = m.group(0);
      String imported = importedTypes.get(name);
      if (imported != null) {
        sb.append(imported);
      } else if (isClassInPackage(name, packagePrefix)) {
        String compressed = name.substring(packagePrefix.length());
        if (isAmbiguous(compressed)) {
          sb.append(name);
        } else {
          sb.append(compressed);
        }
      } else if (isClassInPackage(name, "java.lang.")) {
        sb.append(name.substring("java.lang.".length()));
      } else {
        sb.append(name);
      }
      pos = m.end();
    }

    if (trailer.matches()) {
      sb.append(trailer.group(2));
    }
    return sb.toString();
  }

  private static boolean isClassInPackage(String name, String packagePrefix) {
    if (name.startsWith(packagePrefix)) {
      if (name.indexOf('.', packagePrefix.length()) == -1) {
        return true;
      }
      // check to see if the part after the package looks like a class
      if (Character.isUpperCase(name.charAt(packagePrefix.length()))) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns true if the imports contain a class with same simple name as {@code compressed}.
   *
   * @param compressed simple name of the type
   */
  private boolean isAmbiguous(String compressed) {
    return importedTypes.values().contains(compressed);
  }

  /**
   * Emits an initializer declaration.
   *
   * @param isStatic true if it should be an static initializer, false for an instance initializer.
   */
  public JavaWriter beginInitializer(boolean isStatic) throws IOException {
    indent();
    if (isStatic) {
      out.write("static");
      out.write(" {\n");
    } else {
      out.write("{\n");
    }
    scopes.push(Scope.INITIALIZER);
    return this;
  }

  /** Ends the current initializer declaration. */
  public JavaWriter endInitializer() throws IOException {
    popScope(Scope.INITIALIZER);
    indent();
    out.write("}\n");
    return this;
  }

 /**
  * Emits a type declaration.
  *
  * @param kind such as "class", "interface" or "enum".
  */
  public JavaWriter beginType(String type, String kind) throws IOException {
    return beginType(type, kind, EnumSet.noneOf(Modifier.class), null);
  }

  /**
   * Emits a type declaration.
   *
   * @param kind such as "class", "interface" or "enum".
   */
  public JavaWriter beginType(String type, String kind, Set<Modifier> modifiers)
      throws IOException {
    return beginType(type, kind, modifiers, null);
  }

  /**
   * Emits a type declaration.
   *
   * @param kind such as "class", "interface" or "enum".
   * @param extendsType the class to extend, or null for no extends clause.
   */
  public JavaWriter beginType(String type, String kind, Set<Modifier> modifiers, String extendsType,
      String... implementsTypes) throws IOException {
    indent();
    emitModifiers(modifiers);
    out.write(kind);
    out.write(" ");
    emitCompressedType(type);
    if (extendsType != null) {
      out.write(" extends ");
      emitCompressedType(extendsType);
    }
    if (implementsTypes.length > 0) {
      out.write("\n");
      indent();
      out.write("    implements ");
      for (int i = 0; i < implementsTypes.length; i++) {
        if (i != 0) {
          out.write(", ");
        }
        emitCompressedType(implementsTypes[i]);
      }
    }
    out.write(" {\n");
    scopes.push("interface".equals(kind) ? Scope.INTERFACE_DECLARATION : Scope.TYPE_DECLARATION);
    types.push(type);
    return this;
  }

  /** Completes the current type declaration. */
  public JavaWriter endType() throws IOException {
    popScope(Scope.TYPE_DECLARATION, Scope.INTERFACE_DECLARATION);
    types.pop();
    indent();
    out.write("}\n");
    return this;
  }

  /** Emits a field declaration. */
  public JavaWriter emitField(String type, String name) throws IOException {
    return emitField(type, name, EnumSet.noneOf(Modifier.class), null);
  }

  /** Emits a field declaration. */
  public JavaWriter emitField(String type, String name, Set<Modifier> modifiers)
      throws IOException {
    return emitField(type, name, modifiers, null);
  }

  /** Emits a field declaration. */
  public JavaWriter emitField(String type, String name, Set<Modifier> modifiers,
      String initialValue) throws IOException {
    indent();
    emitModifiers(modifiers);
    emitCompressedType(type);
    out.write(" ");
    out.write(name);

    if (initialValue != null) {
      out.write(" =");
      if (!initialValue.startsWith("\n")) {
        out.write(" ");
      }

      String[] lines = initialValue.split("\n", -1);
      out.write(lines[0]);
      for (int i = 1; i < lines.length; i++) {
        out.write("\n");
        hangingIndent();
        out.write(lines[i]);
      }
    }
    out.write(";\n");
    return this;
  }

  /**
   * Emit a method declaration.
   *
   * <p>A {@code null} return type may be used to indicate a constructor, but
   * {@link #beginConstructor(Set, String...)} should be preferred. This behavior may be removed in
   * a future release.
   *
   * @param returnType the method's return type, or null for constructors
   * @param name the method name, or the fully qualified class name for constructors.
   * @param modifiers the set of modifiers to be applied to the method
   * @param parameters alternating parameter types and names.
   */
  public JavaWriter beginMethod(String returnType, String name, Set<Modifier> modifiers,
      String... parameters) throws IOException {
    return beginMethod(returnType, name, modifiers, Arrays.asList(parameters), null);
  }

  /**
   * Emit a method declaration.
   *
   * <p>A {@code null} return type may be used to indicate a constructor, but
   * {@link #beginConstructor(Set, List, List)} should be preferred. This behavior may be removed in
   * a future release.
   *
   * @param returnType the method's return type, or null for constructors.
   * @param name the method name, or the fully qualified class name for constructors.
   * @param modifiers the set of modifiers to be applied to the method
   * @param parameters alternating parameter types and names.
   * @param throwsTypes the classes to throw, or null for no throws clause.
   */
  public JavaWriter beginMethod(String returnType, String name, Set<Modifier> modifiers,
      List<String> parameters, List<String> throwsTypes) throws IOException {
    indent();
    emitModifiers(modifiers);
    if (returnType != null) {
      emitCompressedType(returnType);
      out.write(" ");
      out.write(name);
    } else {
      emitCompressedType(name);
    }
    out.write("(");
    if (parameters != null) {
      for (int p = 0; p < parameters.size();) {
        if (p != 0) {
          out.write(", ");
        }
        emitCompressedType(parameters.get(p++));
        out.write(" ");
        emitCompressedType(parameters.get(p++));
      }
    }
    out.write(")");
    if (throwsTypes != null && throwsTypes.size() > 0) {
      out.write("\n");
      indent();
      out.write("    throws ");
      for (int i = 0; i < throwsTypes.size(); i++) {
        if (i != 0) {
          out.write(", ");
        }
        emitCompressedType(throwsTypes.get(i));
      }
    }
    if (modifiers.contains(ABSTRACT) || Scope.INTERFACE_DECLARATION.equals(scopes.peek())) {
      out.write(";\n");
      scopes.push(Scope.ABSTRACT_METHOD);
    } else {
      out.write(" {\n");
      scopes.push(returnType == null ? Scope.CONSTRUCTOR : Scope.NON_ABSTRACT_METHOD);
    }
    return this;
  }

  public JavaWriter beginConstructor(Set<Modifier> modifiers, String... parameters)
      throws IOException {
    beginMethod(null, rawType(types.peekFirst()), modifiers, parameters);
    return this;
  }

  public JavaWriter beginConstructor(Set<Modifier> modifiers,
      List<String> parameters, List<String> throwsTypes)
      throws IOException {
    beginMethod(null, rawType(types.peekFirst()), modifiers, parameters, throwsTypes);
    return this;
  }

  /** Emits some Javadoc comments with line separated by {@code \n}. */
  public JavaWriter emitJavadoc(String javadoc, Object... params) throws IOException {
    String formatted = String.format(javadoc, params);

    indent();
    out.write("/**\n");
    for (String line : formatted.split("\n")) {
      indent();
      out.write(" *");
      if (!line.isEmpty()) {
        out.write(" ");
        out.write(line);
      }
      out.write("\n");
    }
    indent();
    out.write(" */\n");
    return this;
  }

  /** Emits a single line comment. */
  public JavaWriter emitSingleLineComment(String comment, Object... args) throws IOException {
    indent();
    out.write("// ");
    out.write(String.format(comment, args));
    out.write("\n");
    return this;
  }

  public JavaWriter emitEmptyLine() throws IOException {
    out.write("\n");
    return this;
  }

  public JavaWriter emitEnumValue(String name) throws IOException {
    indent();
    out.write(name);
    out.write(",\n");
    return this;
  }

  /**
   * A simple switch to emit the proper enum depending if its last causing it to be terminated
   * by a semi-colon ({@code ;}).
   */
  public JavaWriter emitEnumValue(String name, boolean isLast) throws IOException {
    return isLast ? emitLastEnumValue(name) : emitEnumValue(name);
  }

  private JavaWriter emitLastEnumValue(String name) throws IOException {
    indent();
    out.write(name);
    out.write(";\n");
    return this;
  }

  /** Emit a list of enum values followed by a semi-colon ({@code ;}). */
  public JavaWriter emitEnumValues(Iterable<String> names) throws IOException {
    Iterator<String> iterator = names.iterator();

    while (iterator.hasNext()) {
      String name = iterator.next();
      if (iterator.hasNext()) {
        emitEnumValue(name);
      } else {
        emitLastEnumValue(name);
      }
    }

    return this;
  }

  /** Equivalent to {@code annotation(annotation, emptyMap())}. */
  public JavaWriter emitAnnotation(String annotation) throws IOException {
    return emitAnnotation(annotation, Collections.<String, Object>emptyMap());
  }

  /** Equivalent to {@code annotation(annotationType.getName(), emptyMap())}. */
  public JavaWriter emitAnnotation(Class<? extends Annotation> annotationType) throws IOException {
    return emitAnnotation(type(annotationType), Collections.<String, Object>emptyMap());
  }

  /**
   * Annotates the next element with {@code annotationType} and a {@code value}.
   *
   * @param value an object used as the default (value) parameter of the annotation. The value will
   *     be encoded using Object.toString(); use {@link #stringLiteral} for String values. Object
   *     arrays are written one element per line.
   */
  public JavaWriter emitAnnotation(Class<? extends Annotation> annotationType, Object value)
      throws IOException {
    return emitAnnotation(type(annotationType), value);
  }

  /**
   * Annotates the next element with {@code annotation} and a {@code value}.
   *
   * @param value an object used as the default (value) parameter of the annotation. The value will
   *     be encoded using Object.toString(); use {@link #stringLiteral} for String values. Object
   *     arrays are written one element per line.
   */
  public JavaWriter emitAnnotation(String annotation, Object value) throws IOException {
    indent();
    out.write("@");
    emitCompressedType(annotation);
    out.write("(");
    emitAnnotationValue(value);
    out.write(")");
    out.write("\n");
    return this;
  }

  /** Equivalent to {@code annotation(annotationType.getName(), attributes)}. */
  public JavaWriter emitAnnotation(Class<? extends Annotation> annotationType,
      Map<String, ?> attributes) throws IOException {
    return emitAnnotation(type(annotationType), attributes);
  }

  /**
   * Annotates the next element with {@code annotation} and {@code attributes}.
   *
   * @param attributes a map from annotation attribute names to their values. Values are encoded
   *     using Object.toString(); use {@link #stringLiteral} for String values. Object arrays are
   *     written one element per line.
   */
  public JavaWriter emitAnnotation(String annotation, Map<String, ?> attributes)
      throws IOException {
    indent();
    out.write("@");
    emitCompressedType(annotation);
    switch (attributes.size()) {
      case 0:
        break;
      case 1:
        Entry<String, ?> onlyEntry = attributes.entrySet().iterator().next();
        out.write("(");
        if (!"value".equals(onlyEntry.getKey())) {
          out.write(onlyEntry.getKey());
          out.write(" = ");
        }
        emitAnnotationValue(onlyEntry.getValue());
        out.write(")");
        break;
      default:
        boolean split = attributes.size() > MAX_SINGLE_LINE_ATTRIBUTES
            || containsArray(attributes.values());
        out.write("(");
        scopes.push(Scope.ANNOTATION_ATTRIBUTE);
        String separator = split ? "\n" : "";
        for (Map.Entry<String, ?> entry : attributes.entrySet()) {
          out.write(separator);
          separator = split ? ",\n" : ", ";
          if (split) {
            indent();
          }
          out.write(entry.getKey());
          out.write(" = ");
          Object value = entry.getValue();
          emitAnnotationValue(value);
        }
        popScope(Scope.ANNOTATION_ATTRIBUTE);
        if (split) {
          out.write("\n");
          indent();
        }
        out.write(")");
        break;
    }
    out.write("\n");
    return this;
  }

  private boolean containsArray(Collection<?> values) {
    for (Object value : values) {
      if (value instanceof Object[]) {
        return true;
      }
    }
    return false;
  }

  /**
   * Writes a single annotation value. If the value is an array, each element in the array will be
   * written to its own line.
   */
  private JavaWriter emitAnnotationValue(Object value) throws IOException {
    if (value instanceof Object[]) {
      out.write("{");
      boolean firstValue = true;
      scopes.push(Scope.ANNOTATION_ARRAY_VALUE);
      for (Object o : ((Object[]) value)) {
        if (firstValue) {
          firstValue = false;
          out.write("\n");
        } else {
          out.write(",\n");
        }
        indent();
        out.write(o.toString());
      }
      popScope(Scope.ANNOTATION_ARRAY_VALUE);
      out.write("\n");
      indent();
      out.write("}");
    } else {
      out.write(value.toString());
    }
    return this;
  }

  /**
   * @param pattern a code pattern like "int i = %s". Newlines will be further indented. Should not
   *     contain trailing semicolon.
   */
  public JavaWriter emitStatement(String pattern, Object... args) throws IOException {
    checkInMethod();
    String[] lines = String.format(pattern, args).split("\n", -1);
    indent();
    out.write(lines[0]);
    for (int i = 1; i < lines.length; i++) {
      out.write("\n");
      hangingIndent();
      out.write(lines[i]);
    }
    out.write(";\n");
    return this;
  }

  /**
   * @param controlFlow the control flow construct and its code, such as "if (foo == 5)". Shouldn't
   *     contain braces or newline characters.
   */
  public JavaWriter beginControlFlow(String controlFlow, Object... args) throws IOException {
    checkInMethod();
    indent();
    out.write(String.format(controlFlow, args));
    out.write(" {\n");
    scopes.push(Scope.CONTROL_FLOW);
    return this;
  }

  /**
   * @param controlFlow the control flow construct and its code, such as "else if (foo == 10)".
   *     Shouldn't contain braces or newline characters.
   */
  public JavaWriter nextControlFlow(String controlFlow, Object... args) throws IOException {
    popScope(Scope.CONTROL_FLOW);
    indent();
    scopes.push(Scope.CONTROL_FLOW);
    out.write("} ");
    out.write(String.format(controlFlow, args));
    out.write(" {\n");
    return this;
  }

  public JavaWriter endControlFlow() throws IOException {
    return endControlFlow(null);
  }

  /**
   * @param controlFlow the optional control flow construct and its code, such as
   *     "while(foo == 20)". Only used for "do/while" control flows.
   */
  public JavaWriter endControlFlow(String controlFlow, Object... args) throws IOException {
    popScope(Scope.CONTROL_FLOW);
    indent();
    if (controlFlow != null) {
      out.write("} ");
      out.write(String.format(controlFlow, args));
      out.write(";\n");
    } else {
      out.write("}\n");
    }
    return this;
  }

  /** Completes the current method declaration. */
  public JavaWriter endMethod() throws IOException {
    Scope popped = scopes.pop();
    // support calling a constructor a "method" to support the legacy code
    if (popped == Scope.NON_ABSTRACT_METHOD || popped == Scope.CONSTRUCTOR) {
      indent();
      out.write("}\n");
    } else if (popped != Scope.ABSTRACT_METHOD) {
      throw new IllegalStateException();
    }
    return this;
  }

  /** Completes the current constructor declaration. */
  public JavaWriter endConstructor() throws IOException {
    popScope(Scope.CONSTRUCTOR);
    indent();
    out.write("}\n");
    return this;
  }

  /**
   * Returns the string literal representing {@code data}, including wrapping quotes.
   *
   * @deprecated use {@link StringLiteral} and its {@link StringLiteral#literal()} method instead.
   */
  @Deprecated
  public static String stringLiteral(String data) {
    return StringLiteral.forValue(data).literal();
  }

  /** Build a string representation of a type and optionally its generic type arguments. */
  public static String type(Class<?> raw, String... parameters) {
    if (parameters.length == 0) {
      return raw.getCanonicalName();
    }
    if (raw.getTypeParameters().length != parameters.length) {
      throw new IllegalArgumentException();
    }
    StringBuilder result = new StringBuilder();
    result.append(raw.getCanonicalName());
    result.append("<");
    result.append(parameters[0]);
    for (int i = 1; i < parameters.length; i++) {
      result.append(", ");
      result.append(parameters[i]);
    }
    result.append(">");
    return result.toString();
  }

  /** Build a string representation of the raw type for a (optionally generic) type. */
  public static String rawType(String type) {
    int lessThanIndex = type.indexOf('<');
    if (lessThanIndex != -1) {
      return type.substring(0, lessThanIndex);
    }
    return type;
  }

  @Override public void close() throws IOException {
    out.close();
  }

  /** Emits the modifiers to the writer. */
  private void emitModifiers(Set<Modifier> modifiers) throws IOException {
    if (modifiers.isEmpty()) {
      return;
    }
    // Use an EnumSet to ensure the proper ordering
    if (!(modifiers instanceof EnumSet)) {
      modifiers = EnumSet.copyOf(modifiers);
    }
    for (Modifier modifier : modifiers) {
      out.append(modifier.toString()).append(' ');
    }
  }

  private void indent() throws IOException {
    for (int i = 0, count = scopes.size(); i < count; i++) {
      out.write(indent);
    }
  }

  private void hangingIndent() throws IOException {
    for (int i = 0, count = scopes.size() + 2; i < count; i++) {
      out.write(indent);
    }
  }

  private static final EnumSet<Scope> METHOD_SCOPES = EnumSet.of(
      Scope.NON_ABSTRACT_METHOD, Scope.CONSTRUCTOR, Scope.CONTROL_FLOW, Scope.INITIALIZER);

  private void checkInMethod() {
    if (!METHOD_SCOPES.contains(scopes.peekFirst())) {
      throw new IllegalArgumentException();
    }
  }

  private void popScope(Scope... expected) {
    if (!EnumSet.copyOf(Arrays.asList(expected)).contains(scopes.pop())) {
      throw new IllegalStateException();
    }
  }

  private enum Scope {
    TYPE_DECLARATION,
    INTERFACE_DECLARATION,
    ABSTRACT_METHOD,
    NON_ABSTRACT_METHOD,
    CONSTRUCTOR,
    CONTROL_FLOW,
    ANNOTATION_ATTRIBUTE,
    ANNOTATION_ARRAY_VALUE,
    INITIALIZER
  }
}
