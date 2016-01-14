package io.realm.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class RealmRobolectric implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.task('downloadRealmRobolectric') {
            def targetZip = "${project.projectDir}/realm-robolectric-darwin-${Version.VERSION}.zip"
            def targetLib = "${project.projectDir}/src/test/libs/librealm-jni-darwin.dylib"

            inputs.file project.file(targetZip)
            outputs.file project.file(targetLib)

            doLast {
                project.download {
                    src "http://static.realm.io/downloads/java/realm-robolectric-darwin-${Version.VERSION}.zip"
                    dest "${project.projectDir}"
                }

                project.copy {
                    from project.zipTree(project.file(targetZip))
                    into "${project.projectDir}/src/test/libs"
                }
            }
        }
        project.afterEvaluate {
            project.test.dependsOn project.tasks.downloadRealmRobolectric
        }
    }
}