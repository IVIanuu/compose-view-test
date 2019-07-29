package com.ivianuu.compose.ide

import org.jetbrains.kotlin.cli.common.arguments.CommonCompilerArguments
import org.jetbrains.kotlin.idea.facet.KotlinFacet
import org.jetbrains.kotlin.utils.PathUtil
import java.io.File


internal object ComposeImportHandler {
    val PLUGIN_JPS_JAR: String
        get() = File(PathUtil.kotlinPathsForIdeaPlugin.libPath, "compose-compiler.jar").absolutePath

    fun modifyCompilerArguments(facet: KotlinFacet, buildSystemPluginJar: String) {
        val facetSettings = facet.configuration.settings
        val commonArguments = facetSettings.compilerArguments ?: CommonCompilerArguments.DummyImpl()

        var pluginWasEnabled = false
        val oldPluginClasspaths =
            (commonArguments.pluginClasspaths ?: emptyArray()).filterTo(mutableListOf()) {
                val lastIndexOfFile = it.lastIndexOfAny(charArrayOf('/', File.separatorChar))
                if (lastIndexOfFile < 0) {
                    return@filterTo true
                }
                val match =
                    it.drop(lastIndexOfFile + 1).matches("$buildSystemPluginJar-.*\\.jar".toRegex())
                if (match) pluginWasEnabled = true
                !match
            }

        val newPluginClasspaths =
            if (pluginWasEnabled) oldPluginClasspaths + PLUGIN_JPS_JAR else oldPluginClasspaths
        commonArguments.pluginClasspaths = newPluginClasspaths.toTypedArray()
        facetSettings.compilerArguments = commonArguments
    }
}