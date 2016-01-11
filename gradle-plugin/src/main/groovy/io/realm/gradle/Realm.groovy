package io.realm.gradle

import com.neenbedankt.gradle.androidapt.AndroidAptPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class Realm implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.plugins.apply(AndroidAptPlugin)
        project.repositories.add(project.getRepositories().jcenter())
        project.repositories.add(project.repositories.maven { url "https://jitpack.io" })
        project.dependencies.add("compile", "io.realm:realm-android-library:${Version.VERSION}")
        project.dependencies.add("compile", "io.realm:realm-annotations:${Version.VERSION}")
        project.dependencies.add("apt", "io.realm:realm-annotations:${Version.VERSION}")
        project.dependencies.add("apt", "io.realm:realm-annotations-processor:${Version.VERSION}")
    }
}
