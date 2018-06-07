/*
 * Copyright 2016 Realm Inc.
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

package io.realm.transformer

import kotlin.collections.EmptyList
import org.gradle.api.Project

import javax.annotation.Nonnull
import javax.annotation.Nullable;

/**
 * Helper methods for functionality that is really hard to port to Java/Kotlin
 */
class GroovyUtil {

    @Nullable
    static String getTargetSdk(@Nonnull Project project) {
        return project?.android?.defaultConfig?.targetSdkVersion?.mApiLevel as String
    }

    @Nullable
    static String getMinSdk(@Nonnull Project project) {
        return project?.android?.defaultConfig?.minSdkVersion?.mApiLevel as String
    }

    @Nonnull
    static boolean isSyncEnabled(@Nonnull Project project) {
        return project.realm?.syncEnabled != null && project.realm.syncEnabled
    }

    @Nonnull
    static Collection<File> getBootClasspath(@Nonnull Project project) {
        def classpath = project.android.bootClasspath as Collection<File>
        return (classpath != null) ? classpath : Collections.emptyList()
    }
}
