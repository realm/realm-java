package io.realm.gradle

import com.neenbedankt.gradle.androidapt.AndroidAptPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class Realm implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.plugins.apply(AndroidAptPlugin)
        project.repositories.add(project.getRepositories().jcenter());
        project.dependencies.add("compile", "io.realm:realm:0.83.0-SNAPSHOT"); // TODO: make version dynamic
        project.dependencies.add("apt", "io.realm:realm-annotations-processor:0.83.0-SNAPSHOT");
    }
}
