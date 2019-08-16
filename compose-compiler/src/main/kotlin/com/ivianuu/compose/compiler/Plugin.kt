package com.ivianuu.compose.compiler

import com.google.auto.service.AutoService
import org.jetbrains.kotlin.codegen.ClassBuilder
import org.jetbrains.kotlin.codegen.ClassBuilderFactory
import org.jetbrains.kotlin.codegen.DelegatingClassBuilder
import org.jetbrains.kotlin.codegen.extensions.ClassBuilderInterceptorExtension
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.diagnostics.DiagnosticSink
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOrigin
import org.jetbrains.org.objectweb.asm.Label
import org.jetbrains.org.objectweb.asm.MethodVisitor
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter

@AutoService(ComponentRegistrar::class)
class ComposeComponentRegistrar : ComponentRegistrar {
    override fun registerProjectComponents(
        project: MockProject,
        configuration: CompilerConfiguration
    ) {
        ClassBuilderInterceptorExtension.registerExtension(
            project,
            ComposeClassBuilderInterceptorExtension()
        )
    }
}

class ComposeClassBuilderInterceptorExtension : ClassBuilderInterceptorExtension {
    override fun interceptClassBuilderFactory(
        interceptedFactory: ClassBuilderFactory,
        bindingContext: BindingContext,
        diagnostics: DiagnosticSink
    ): ClassBuilderFactory = ComposeClassBuilderFactory(interceptedFactory)

    private inner class ComposeClassBuilderFactory(
        private val delegateFactory: ClassBuilderFactory
    ) : ClassBuilderFactory {

        override fun newClassBuilder(origin: JvmDeclarationOrigin): ClassBuilder {
            return ComposeClassBuilder(delegateFactory.newClassBuilder(origin))
        }

        override fun getClassBuilderMode() = delegateFactory.classBuilderMode

        override fun asText(builder: ClassBuilder?): String? {
            return delegateFactory.asText((builder as ComposeClassBuilder).delegateClassBuilder)
        }

        override fun asBytes(builder: ClassBuilder?): ByteArray? {
            return delegateFactory.asBytes((builder as ComposeClassBuilder).delegateClassBuilder)
        }

        override fun close() {
            delegateFactory.close()
        }
    }


}

class ComposeClassBuilder(val delegateClassBuilder: ClassBuilder) : DelegatingClassBuilder() {

    override fun getDelegate(): ClassBuilder = delegateClassBuilder

    override fun newMethod(
        origin: JvmDeclarationOrigin,
        access: Int,
        name: String,
        desc: String,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        val original = super.newMethod(origin, access, name, desc, signature, exceptions)

        if (origin.descriptor == null) return original

        var lineNumber = 0

        return object : MethodVisitor(Opcodes.ASM5, original) {
            override fun visitLineNumber(line: Int, start: Label?) {
                super.visitLineNumber(line, start)
                lineNumber = line
            }

            override fun visitMethodInsn(
                opcode: Int,
                owner: String?,
                name: String?,
                descriptor: String?,
                isInterface: Boolean
            ) {
                if (opcode == 184 && name == "sourceLocation") {
                    InstructionAdapter(this).apply {
                        this.aconst("${origin.descriptor!!.fqNameSafe}:$lineNumber")
                    }
                } else {
                    super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
                }
            }
        }
    }
}