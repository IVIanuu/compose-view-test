plugins {
    id("java-library")
    id("kotlin")
    id("kotlin-kapt")
}

apply(from = "https://raw.githubusercontent.com/IVIanuu/gradle-scripts/master/java-8.gradle")
apply(from = "https://raw.githubusercontent.com/IVIanuu/gradle-scripts/master/kt-kapt.gradle")
apply(from = "https://raw.githubusercontent.com/IVIanuu/gradle-scripts/master/mvn-publish.gradle")

dependencies {
    api(Deps.autoService)
    kapt(Deps.autoService)
    api(Deps.kotlinCompilerEmbeddable)
    api(Deps.kotlinStdLib)
}
