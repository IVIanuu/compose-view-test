plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
}

apply(from = "https://raw.githubusercontent.com/IVIanuu/gradle-scripts/master/android-build-lib.gradle")
apply(from = "https://raw.githubusercontent.com/IVIanuu/gradle-scripts/master/java-8-android.gradle")
apply(from = "https://raw.githubusercontent.com/IVIanuu/gradle-scripts/master/kt-android-ext.gradle")
apply(from = "https://raw.githubusercontent.com/IVIanuu/gradle-scripts/master/kt-kapt.gradle")
apply(from = "https://raw.githubusercontent.com/IVIanuu/gradle-scripts/master/mvn-publish.gradle")

dependencies {
    api(Deps.androidxAppCompat)
    api(Deps.androidxUiMaterial)
    api(Deps.kotlinStdLib)
    api(project(":compose-annotations"))
    kapt(project(":compose-compiler"))
}
