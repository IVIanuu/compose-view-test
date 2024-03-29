/*
 * Copyright 2019 Manuel Wrage
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

buildscript {
    repositories {
        mavenLocal()
        maven("https://dl.bintray.com/ivianuu/maven/")
        google()
        jcenter()
        mavenCentral()
        maven("https://plugins.gradle.org/m2")
    }

    dependencies {
        classpath(Deps.androidGradlePlugin)
        classpath(Deps.bintrayGradlePlugin)
        classpath(Deps.buildConfigPlugin)
        classpath(Deps.composeGradlePlugin)
        classpath(Deps.jarJarGradlePlugin)
        classpath(Deps.kotlinGradlePlugin)
        classpath(Deps.mavenGradlePlugin)
    }
}

allprojects {
    repositories {
        mavenLocal()
        maven("https://dl.bintray.com/ivianuu/maven/")
        google()
        jcenter()
        mavenCentral()
    }

    configurations.all {
        resolutionStrategy {
            eachDependency {
                if (requested.group == "org.jetbrains.kotlin") {
                    useVersion(Versions.kotlin)
                }
            }
        }
    }
}