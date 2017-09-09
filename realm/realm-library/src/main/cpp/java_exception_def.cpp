/*
 * Copyright 2017 Realm Inc.
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

#include "java_exception_def.hpp"

using namespace realm::_impl;

const char* JavaExceptionDef::IllegalState = "java/lang/IllegalStateException";
const char* JavaExceptionDef::IllegalArgument = "java/lang/IllegalArgumentException";
const char* JavaExceptionDef::OutOfMemory = "java/lang/OutOfMemoryError";
const char* JavaExceptionDef::RealmMigrationNeeded = "io/realm/exceptions/RealmMigrationNeededException";
