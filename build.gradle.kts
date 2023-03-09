plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.13.1"
}

group = "com.hathoute"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2022.3.2")

    plugins.set(listOf("com.intellij.java"))
}

java {
    sourceCompatibility = org.gradle.api.JavaVersion.VERSION_17
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    buildSearchableOptions {
        enabled = false
    }

    patchPluginXml {
        version.set("${project.version}")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
