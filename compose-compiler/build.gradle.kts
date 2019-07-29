plugins {
    id("java-library")
    id("kotlin")
    id("kotlin-kapt")
}

apply(from = "https://raw.githubusercontent.com/IVIanuu/gradle-scripts/master/java-8.gradle")
apply(from = "https://raw.githubusercontent.com/IVIanuu/gradle-scripts/master/kt-kapt.gradle")
apply(from = "https://raw.githubusercontent.com/IVIanuu/gradle-scripts/master/mvn-publish.gradle")

dependencies {
    implementation(Deps.processingX)
    kapt(Deps.processingX)
    implementation(Deps.kotlinStdLib)
    implementation(project(":compose-annotations"))
}
