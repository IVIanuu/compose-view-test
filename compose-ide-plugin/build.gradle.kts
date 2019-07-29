import org.anarres.gradle.plugin.jarjar.JarjarTask

plugins {
    id("java-gradle-plugin")
    id("kotlin")
    id("kotlin-kapt")
    id("org.jetbrains.intellij") version "0.4.9"
    //id("org.anarres.jarjar")
}

apply(from = "https://raw.githubusercontent.com/IVIanuu/gradle-scripts/master/java-8.gradle")
apply(from = "https://raw.githubusercontent.com/IVIanuu/gradle-scripts/master/kt-kapt.gradle")
//apply(from = "https://raw.githubusercontent.com/IVIanuu/gradle-scripts/master/mvn-publish.gradle")

intellij {
    pluginName = "compose"
    version = "2019.1"
    setPlugins("gradle", "org.jetbrains.kotlin:1.3.41-release-IJ2019.1-1")
}

dependencies {
    api(project(":compose-compiler"))
}

/*

configurations {
    create("jarFiles")
}

dependencies {
    add("jarFiles", project(":compose-compiler")) {
        isTransitive = false
    }

    implementation(Deps.kotlinGradlePluginApi)
}

val embeddedPlugin = tasks.create("repackage", JarjarTask::class) {
    destinationName = "compose-compiler.jar"
    from(configurations.getByName("jarFiles"))
    classRename("com.intellij.**", "org.jetbrains.kotlin.com.intellij.@1")
}

configurations {
    create("embeddablePlugin")
}

artifacts {
    add("embeddablePlugin", embeddedPlugin.destinationPath) {
        name = "compose-compiler-embeddable"
        type = "jar"
        builtBy(embeddedPlugin)
    }
}*/