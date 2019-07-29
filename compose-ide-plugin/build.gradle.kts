plugins {
    id("java-gradle-plugin")
    id("kotlin")
    id("kotlin-kapt")
    id("org.jetbrains.intellij") version "0.4.9"
}

apply(from = "https://raw.githubusercontent.com/IVIanuu/gradle-scripts/master/java-8.gradle")
apply(from = "https://raw.githubusercontent.com/IVIanuu/gradle-scripts/master/kt-kapt.gradle")
apply(from = "https://raw.githubusercontent.com/IVIanuu/gradle-scripts/master/mvn-publish.gradle")

intellij {
    pluginName = "compose"
    version = "2019.1"
    setPlugins("gradle", "org.jetbrains.kotlin:1.3.31-release-IJ2019.1-1")
}

dependencies {
    implementation(project(":compose-compiler"))
}