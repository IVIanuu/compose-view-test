package com.ivianuu.compose.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

open class ComposeGradlePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.create(
            "compose",
            ComposeGradlePlugin::class.java
        )
    }
}