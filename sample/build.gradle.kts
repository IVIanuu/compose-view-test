plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
}

apply(from = "https://raw.githubusercontent.com/IVIanuu/gradle-scripts/master/android-build-app.gradle")
apply(from = "https://raw.githubusercontent.com/IVIanuu/gradle-scripts/master/java-8-android.gradle")
apply(from = "https://raw.githubusercontent.com/IVIanuu/gradle-scripts/master/kt-android-ext.gradle")
apply(from = "https://raw.githubusercontent.com/IVIanuu/gradle-scripts/master/kt-kapt.gradle")

dependencies {
    implementation(project(":compose"))
    kapt(project(":compose-compiler"))
}