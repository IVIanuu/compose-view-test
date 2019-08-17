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

package com.ivianuu.compose.compiler

import org.jetbrains.kotlin.codegen.StackValue
import org.jetbrains.kotlin.codegen.asmType
import org.jetbrains.kotlin.codegen.extensions.ExpressionCodegenExtension
import org.jetbrains.kotlin.codegen.topLevelClassAsmType
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter

class ComposeExpressionCodegenExtension : ExpressionCodegenExtension {

    init {
        println("hellu")
    }

    override fun applyFunction(
        receiver: StackValue,
        resolvedCall: ResolvedCall<*>,
        c: ExpressionCodegenExtension.Context
    ): StackValue? {
        println("apply function resulting ${resolvedCall.resultingDescriptor.javaClass} candi ${resolvedCall.candidateDescriptor.javaClass}")
        return if (resolvedCall.candidateDescriptor is ComposeViewConstructorFunctionDescriptor) {
            val clazz =
                (resolvedCall.candidateDescriptor as ComposeViewConstructorFunctionDescriptor).clazz
            object : StackValue(Type.VOID_TYPE) {
                override fun putSelector(
                    type: Type,
                    kotlinType: KotlinType?,
                    v: InstructionAdapter
                ) {
                    val asmType = clazz.defaultType.asmType(c.typeMapper)
                    with(v) {
                        anew(asmType)
                        dup()
                        invokevirtual(
                            "com/ivianuu/myapplication/ViewComposition",
                            "getContext",
                            "()Landroid/content/Context;",
                            false
                        )
                        invokespecial(
                            clazz.defaultType.toString(),
                            "<init>",
                            "(Landroid/content/Context;)V",
                            false
                        )
                        astore(asmType)
                        aload(asmType)
                        aload(VIEW_COMPOSITION.topLevelClassAsmType())
                        checkcast(VIEW.topLevelClassAsmType())
                        invokevirtual(
                            "com/ivianuu/myapplication/ViewComposition",
                            "emit",
                            "(Landroid/view/View;)V",
                            false
                        )
                    }
                }
            }
        } else {
            super.applyFunction(receiver, resolvedCall, c)
        }
    }

}