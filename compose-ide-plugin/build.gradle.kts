plugins {
    kotlin("jvm")
    id("org.jetbrains.intellij") version "0.4.9"
}

apply(from = "https://raw.githubusercontent.com/IVIanuu/gradle-scripts/master/java-8.gradle")
apply(from = "https://raw.githubusercontent.com/IVIanuu/gradle-scripts/master/kt-compiler-args.gradle")
apply(from = "https://raw.githubusercontent.com/IVIanuu/gradle-scripts/master/mvn-publish.gradle")

intellij {
    pluginName = "compose"
    //version = "2019.2"
    setPlugins("org.jetbrains.kotlin:1.3.41-release-IJ2019.2-1")
}

configurations {
    register("include")
}

dependencies {
    "include"(project(":compose-compiler"))
}

tasks.withType<Jar> {
    from(configurations.getByName("include").map {
        if (it.isDirectory) it else zipTree(it)
    })
}