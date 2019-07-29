package com.ivianuu.composex.gradle

import com.google.auto.service.AutoService
import org.gradle.api.Project
import org.gradle.api.tasks.compile.AbstractCompile
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinGradleSubplugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

@AutoService(KotlinGradleSubplugin::class)
open class ComposeXSubplugin : KotlinGradleSubplugin<AbstractCompile> {

    override fun isApplicable(project: Project, task: AbstractCompile) =
        project.plugins.hasPlugin(ComposeXGradlePlugin::class.java)

    init {
    }

    override fun apply(
        project: Project,
        kotlinCompile: AbstractCompile,
        javaCompile: AbstractCompile?,
        variantData: Any?,
        androidProjectHandler: Any?,
        kotlinCompilation: KotlinCompilation<*>?
    ): List<SubpluginOption> {
        val extension = project.extensions.findByType(ComposeXExtension::class.java)
            ?: ComposeXExtension()
        return listOf(SubpluginOption("enabled", extension.enabled.toString()))
    }

    override fun getCompilerPluginId(): String = "com.ivianuu.composex"

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = com.ivianuu.composex.BuildConfig.GROUP_ID,
        artifactId = com.ivianuu.composex.BuildConfig.ARTIFACT_ID,
        version = com.ivianuu.composex.BuildConfig.VERSION
    )
}