package io.realm.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class RealmRobolectric implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.task('downloadRealmRobolectric') {
            doLast {
                def targetZip = "${project.projectDir}/realm-robolectric-darwin-${Version.VERSION}.zip"
                def targetLib = "${project.projectDir}/robolectricLibs/librealm-jni-darwin.dylib"

                if (project.file(targetZip).exists() && project.file(targetLib).exists()) {
                    println "librealm-jni-darwin.dylib is already exsit. Skip download the Realm Robolectric."
                    return
                }

                project.download {
                    src "http://static.realm.io/downloads/java/realm-robolectric-darwin-${Version.VERSION}.zip"
                    dest "${project.projectDir}"
                }

                project.copy {
                    from project.zipTree(project.file(targetZip))
                    into "${project.projectDir}/robolectricLibs"
                }
            }
        }
        project.afterEvaluate {
            project.test.dependsOn project.tasks.downloadRealmRobolectric
        }
    }
}