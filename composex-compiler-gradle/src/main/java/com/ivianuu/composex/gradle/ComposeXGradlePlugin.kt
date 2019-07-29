package com.ivianuu.composex.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

open class ComposeXGradlePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.create(
            "composeX",
            ComposeXGradlePlugin::class.java
        )
    }
}