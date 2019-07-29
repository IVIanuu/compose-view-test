plugins {
    id("java-gradle-plugin")
    id("kotlin")
    id("kotlin-kapt")
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

dependencies {
    implementation(Deps.autoService)
    kapt(Deps.autoService)
    implementation(project(":composex-compiler"))
    implementation(Deps.kotlinGradlePluginApi)
}