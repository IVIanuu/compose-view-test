plugins {
    id("com.android.application")
    kotlin("android")
}

apply(from = "https://raw.githubusercontent.com/IVIanuu/gradle-scripts/master/android-build-app.gradle")
apply(from = "https://raw.githubusercontent.com/IVIanuu/gradle-scripts/master/java-8-android.gradle")
apply(from = "https://raw.githubusercontent.com/IVIanuu/gradle-scripts/master/kt-android-ext.gradle")
apply(from = "https://raw.githubusercontent.com/IVIanuu/gradle-scripts/master/kt-compiler-args.gradle")

dependencies {
    implementation(Deps.androidxAppCompat)
    implementation(project(":compose-runtime"))
    implementation(Deps.androidxRecyclerView)
}