package com.ivianuu.compose.compiler

import com.google.auto.service.AutoService
import com.ivianuu.processingx.steps.StepProcessor
import javax.annotation.processing.Processor

@AutoService(Processor::class)
class ComposeCompiler : StepProcessor() {
    override fun initSteps() = setOf(ViewAttributeStep())
}