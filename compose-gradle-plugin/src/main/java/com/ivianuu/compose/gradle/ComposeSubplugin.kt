package com.ivianuu.compose.gradle

import com.google.auto.service.AutoService
import org.gradle.api.Project
import org.gradle.api.tasks.compile.AbstractCompile
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinGradleSubplugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

@AutoService(KotlinGradleSubplugin::class)
open class ComposeSubplugin : KotlinGradleSubplugin<AbstractCompile> {

    override fun isApplicable(project: Project, task: AbstractCompile) =
        project.plugins.hasPlugin(ComposeGradlePlugin::class.java)

    override fun apply(
        project: Project,
        kotlinCompile: AbstractCompile,
        javaCompile: AbstractCompile?,
        variantData: Any?,
        androidProjectHandler: Any?,
        kotlinCompilation: KotlinCompilation<*>?
    ): List<SubpluginOption> = listOf()

    override fun getCompilerPluginId(): String = "com.ivianuu.compose"

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = com.ivianuu.compose.BuildConfig.GROUP_ID,
        artifactId = com.ivianuu.compose.BuildConfig.ARTIFACT_ID,
        version = com.ivianuu.compose.BuildConfig.VERSION
    )
}