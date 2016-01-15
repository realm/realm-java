package io.realm.gradle

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.neenbedankt.gradle.androidapt.AndroidAptPlugin
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.KotlinAndroidPlugin

class Realm implements Plugin<Project> {

    @Override
    void apply(Project project) {
        // Make sure the project is either an Android application or library
        def isAndroidApp = project.plugins.withType(AppPlugin)
        def isAndroidLib = project.plugins.withType(LibraryPlugin)
        if (!isAndroidApp && !isAndroidLib) {
            throw new GradleException("'android' or 'android-library' plugin required.")
        }

        def isKotlinProject = project.plugins.withType(KotlinAndroidPlugin)

        project.plugins.apply(AndroidAptPlugin)

        project.android.registerTransform(new RealmTransformer())
        project.repositories.add(project.getRepositories().jcenter())
        project.repositories.add(project.repositories.maven { url "https://jitpack.io" })
        project.dependencies.add("compile", "io.realm:realm-android-library:${Version.VERSION}")
        project.dependencies.add("compile", "io.realm:realm-annotations:${Version.VERSION}")
        if (isKotlinProject) {
            project.dependencies.add("kapt", "io.realm:realm-annotations-processor:${Version.VERSION}")
            project.dependencies.add("kapt", "io.realm:realm-annotations:${Version.VERSION}")
        }
    }
}
