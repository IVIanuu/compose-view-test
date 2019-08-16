package com.ivianuu.compose.compiler

import com.google.auto.service.AutoService
import org.jetbrains.kotlin.codegen.StackValue
import org.jetbrains.kotlin.codegen.asmType
import org.jetbrains.kotlin.codegen.extensions.ExpressionCodegenExtension
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.resolve.descriptorUtil.builtIns

@AutoService(ComponentRegistrar::class)
class MyComponentRegistrar : ComponentRegistrar {
    override fun registerProjectComponents(
        project: MockProject,
        configuration: CompilerConfiguration
    ) {
        ExpressionCodegenExtension.registerExtension(project, ComposeExpressionCodegenExtension())
    }
}

class ComposeExpressionCodegenExtension : ExpressionCodegenExtension {

    override fun applyFunction(
        receiver: StackValue,
        resolvedCall: ResolvedCall<*>,
        c: ExpressionCodegenExtension.Context
    ): StackValue? {
        val resultingDescriptor = resolvedCall.resultingDescriptor
        return if (resultingDescriptor.name.asString() == "sourceLocation"
            && resultingDescriptor.returnType == resultingDescriptor.builtIns.anyType
        ) {
            StackValue.functionCall(
                resultingDescriptor.builtIns.intType.asmType(c.typeMapper),
                resultingDescriptor.builtIns.intType
            ) {
                with(it) {
                    val functionDescriptor = c.codegen.context.functionDescriptor
                    var key = functionDescriptor.containingDeclaration.name.hashCode()
                    key = key * 31 + functionDescriptor.name.hashCode()
                    functionDescriptor.typeParameters.forEach {
                        key = key * 31 + it.upperBounds.map { it.hashCode() }.hashCode()
                    }
                    functionDescriptor.valueParameters.forEach {
                        key = key * 31 + it.type.hashCode()
                    }
                    key = key * 31 + c.codegen.lastLineNumber

                    iconst(key)
                }
            }
        } else {
            super.applyFunction(receiver, resolvedCall, c)
        }
    }

}