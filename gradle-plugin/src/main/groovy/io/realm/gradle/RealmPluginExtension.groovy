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
    private String dependencyConfigurationName

    RealmPluginExtension(Project project, boolean syncEnabledDefault, String dependencyConfigurationName) {
        this.project = project
        this.dependencyConfigurationName = dependencyConfigurationName
        setSyncEnabled(syncEnabledDefault)
    }

    void setSyncEnabled(value) {
        this.syncEnabled = value;

        // remove realm android library first
        def iterator = project.getConfigurations().getByName(dependencyConfigurationName).getDependencies().iterator();
        while (iterator.hasNext()) {
            def item = iterator.next()
            if (item.group == 'io.realm' && item.name.startsWith('realm-android-library')) {
                iterator.remove()
            }
        }

        // then add again
        def artifactName = "realm-android-library${syncEnabled ? '-object-server' : ''}"
        project.dependencies.add(dependencyConfigurationName, "io.realm:${artifactName}:${Version.VERSION}")
    }
}
