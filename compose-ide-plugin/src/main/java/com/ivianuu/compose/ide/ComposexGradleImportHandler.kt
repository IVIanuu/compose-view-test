package com.ivianuu.compose.ide

import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.project.ModuleData
import org.jetbrains.kotlin.idea.configuration.GradleProjectImportHandler
import org.jetbrains.kotlin.idea.facet.KotlinFacet
import org.jetbrains.plugins.gradle.model.data.GradleSourceSetData

class ComposexGradleImportHandler : GradleProjectImportHandler {
    override fun importBySourceSet(
        facet: KotlinFacet,
        sourceSetNode: DataNode<GradleSourceSetData>
    ) {
        ComposexImportHandler.modifyCompilerArguments(facet, PLUGIN_GRADLE_JAR)
    }

    override fun importByModule(facet: KotlinFacet, moduleNode: DataNode<ModuleData>) {
        ComposexImportHandler.modifyCompilerArguments(facet, PLUGIN_GRADLE_JAR)
    }

    private val PLUGIN_GRADLE_JAR = "com.ivianuu.compose" // todo
}