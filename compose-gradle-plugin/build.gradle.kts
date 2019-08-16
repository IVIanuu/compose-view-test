plugins {
    id("java-gradle-plugin")
    kotlin("jvm")
    kotlin("kapt")
    id("de.fuerstenau.buildconfig")
}

apply(from = "https://raw.githubusercontent.com/IVIanuu/gradle-scripts/master/java-8.gradle")
apply(from = "https://raw.githubusercontent.com/IVIanuu/gradle-scripts/master/kt-compiler-args.gradle")
apply(from = "https://raw.githubusercontent.com/IVIanuu/gradle-scripts/master/mvn-publish.gradle")

gradlePlugin {
    plugins {
        create("composePlugin") {
            id = "com.ivianuu.compose"
            implementationClass = "com.ivianuu.compose.gradle.ComposeGradlePlugin"
        }
    }
}

buildConfig {
    clsName = "BuildConfig"
    packageName = Publishing.groupId

    version = Publishing.version
    buildConfigField("String", "GROUP_ID", Publishing.groupId)
    buildConfigField("String", "ARTIFACT_ID", "compose-compiler")
}

dependencies {
    api(Deps.autoService)
    kapt(Deps.autoService)
    api(project(":compose-compiler"))
    api(Deps.kotlinGradlePluginApi)
}