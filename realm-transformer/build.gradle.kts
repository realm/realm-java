// Local fork of io.realm:realm-transformer:10.19.0 with AGP 9 compatibility patches.
// Published to the composite build under the same coordinates so dependency substitution
// kicks in automatically for any consumer that pulls `io.realm:realm-gradle-plugin:10.19.0`.

plugins {
    kotlin("jvm") version "2.2.21"
    `java-library`
    `maven-publish`
}

group = "io.realm"
version = "10.19.0"

repositories {
    google()
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(gradleApi())
    implementation("io.realm:realm-annotations:10.19.0")
    compileOnly("com.android.tools.build:gradle:9.0.0")
    implementation("org.javassist:javassist:3.25.0-GA")
    implementation("com.google.guava:guava:31.0.1-jre")
    implementation("javax.xml.bind:jaxb-api:2.3.1")
}

publishing {
    publications {
        create<MavenPublication>("realmPublication") {
            groupId = "io.realm"
            artifactId = "realm-transformer"
            version = "10.19.0"
            from(components["java"])
        }
    }
}
