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

package io.realm.gradle

import org.gradle.api.Project

class RealmPluginExtension {
    private Project project
    def boolean syncEnabled
    def boolean kotlinExtensionsEnabled
    private String dependencyConfigurationName

    RealmPluginExtension(Project project, boolean syncEnabledDefault, boolean useKotlinExtensionsDefault, String dependencyConfigurationName) {
        this.project = project
        this.dependencyConfigurationName = dependencyConfigurationName
        setSyncEnabled(syncEnabledDefault)
        setKotlinExtensionsEnabled(useKotlinExtensionsDefault)
    }

    void setSyncEnabled(value) {
        this.syncEnabled = value;
        setDependencies(syncEnabled, kotlinExtensionsEnabled)
    }

    void setKotlinExtensionsEnabled(value) {
        this.kotlinExtensionsEnabled = value
        setDependencies(syncEnabled, kotlinExtensionsEnabled)
    }

    void setDependencies(boolean syncEnabled, boolean kotlinExtensionsEnabled) {
        // remove libraries first
        def iterator = project.getConfigurations().getByName(dependencyConfigurationName).getDependencies().iterator();
        while (iterator.hasNext()) {
            def item = iterator.next()
            if (item.group == 'io.realm') {
                if (item.name.startsWith('realm-android-library')) {
                    iterator.remove()
                }
                if (item.name.startsWith('realm-android-kotlin-extensions')) {
                    iterator.remove()
                }
            }
        }

        // then add again
        def syncArtifactName = "realm-android-library${syncEnabled ? '-object-server' : ''}"
        project.dependencies.add(dependencyConfigurationName, "io.realm:${syncArtifactName}:${Version.VERSION}")

        if (kotlinExtensionsEnabled) {
            def kotlinExtArtifactName = "realm-android-kotlin-extensions${syncEnabled ? '-object-server' : ''}"
            project.dependencies.add(dependencyConfigurationName, "io.realm:${kotlinExtArtifactName}:${Version.VERSION}")
        }
    }
}
