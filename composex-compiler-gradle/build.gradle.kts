import org.anarres.gradle.plugin.jarjar.JarjarTask

plugins {
    id("java-gradle-plugin")
    id("kotlin")
    id("kotlin-kapt")
    id("org.anarres.jarjar")
    id("de.fuerstenau.buildconfig")
}

apply(from = "https://raw.githubusercontent.com/IVIanuu/gradle-scripts/master/java-8.gradle")
apply(from = "https://raw.githubusercontent.com/IVIanuu/gradle-scripts/master/kt-kapt.gradle")
apply(from = "https://raw.githubusercontent.com/IVIanuu/gradle-scripts/master/mvn-publish.gradle")

gradlePlugin {
    plugins {
        create("composexPlugin") {
            id = "com.ivianuu.composex"
            implementationClass = "com.ivianuu.composex.gradle.ComposeXGradlePlugin"
        }
    }
}

buildConfig {
    clsName = "BuildConfig"
    packageName = Publishing.groupId

    version = Publishing.version
    buildConfigField("String", "GROUP_ID", Publishing.groupId)
    buildConfigField("String", "ARTIFACT_ID", "composex-compiler")
}

configurations {
    create("jarFiles")
}

dependencies {
    implementation(Deps.autoService)
    kapt(Deps.autoService)

    add("jarFiles", project(":composex-compiler")) {
        isTransitive = false
    }

    implementation(Deps.kotlinGradlePluginApi)
}

val embeddedPlugin = tasks.create("repackage", JarjarTask::class) {
    destinationName = "composex-compiler.jar"
    from(configurations.getByName("jarFiles"))
    classRename("com.intellij.**", "org.jetbrains.kotlin.com.intellij.@1")
}

configurations {
    create("embeddablePlugin")
}

artifacts {
    add("embeddablePlugin", embeddedPlugin.destinationPath) {
        name = "composex-compiler-gradle"
        type = "jar"
        builtBy(embeddedPlugin)
    }
}