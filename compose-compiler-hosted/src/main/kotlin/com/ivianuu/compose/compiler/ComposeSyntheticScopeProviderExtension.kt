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

import org.jetbrains.kotlin.builtins.createFunctionType
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.findClassAcrossModuleDependencies
import org.jetbrains.kotlin.descriptors.impl.SimpleFunctionDescriptorImpl
import org.jetbrains.kotlin.descriptors.impl.ValueParameterDescriptorImpl
import org.jetbrains.kotlin.incremental.components.LookupLocation
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.descriptorUtil.builtIns
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameUnsafe
import org.jetbrains.kotlin.resolve.descriptorUtil.getAllSuperclassesWithoutAny
import org.jetbrains.kotlin.resolve.scopes.ResolutionScope
import org.jetbrains.kotlin.resolve.scopes.SyntheticScope
import org.jetbrains.kotlin.synthetic.JavaSyntheticPropertiesScope
import org.jetbrains.kotlin.synthetic.SyntheticScopeProviderExtension

class ComposeSyntheticScopeProviderExtension : SyntheticScopeProviderExtension {
    override fun getScopes(
        moduleDescriptor: ModuleDescriptor,
        javaSyntheticPropertiesScope: JavaSyntheticPropertiesScope
    ): List<SyntheticScope> {
        println("get scopes $moduleDescriptor $javaSyntheticPropertiesScope")
        return listOf(ComposeSyntheticScope(moduleDescriptor))
    }
}

val VIEW = FqName("android.view.View")
val VIEW_COMPOSITION = FqName("com.ivianuu.myapplication.ViewComposition")

class ComposeSyntheticScope(val module: ModuleDescriptor) : SyntheticScope.Default() {

    override fun getSyntheticStaticFunctions(
        scope: ResolutionScope,
        name: Name,
        location: LookupLocation
    ): Collection<FunctionDescriptor> {
        return getSynthFunc(scope, name, location)
    }

    private fun getSynthFunc(
        scope: ResolutionScope,
        name: Name,
        location: LookupLocation
    ): List<FunctionDescriptor> {
        val classifier =
            scope.getContributedClassifier(name, location) as? ClassDescriptor ?: return emptyList()
        // todo better check + support View
        if (classifier.getAllSuperclassesWithoutAny().none { it.fqNameUnsafe.toString() == VIEW.toString() }) return emptyList()

        val function = ComposeViewConstructorFunctionDescriptor(
            classifier,
            module, // todo
            null,
            Annotations.EMPTY,
            classifier.fqNameSafe.shortName(),
            CallableMemberDescriptor.Kind.SYNTHESIZED,
            SourceElement.NO_SOURCE
        )

        val params = listOf(
            ValueParameterDescriptorImpl(
                function,
                null,
                0,
                Annotations.EMPTY,
                Name.identifier("block"),
                createFunctionType(
                    classifier.builtIns,
                    Annotations.EMPTY,
                    classifier.defaultType,
                    emptyList(),
                    emptyList(),
                    classifier.builtIns.unitType,
                    false
                ),
                false,
                false,
                false,
                varargElementType = null,
                source = SourceElement.NO_SOURCE
            )
        )
        function.initialize(
            module.findClassAcrossModuleDependencies(ClassId.topLevel(VIEW_COMPOSITION))!!.thisAsReceiverParameter,
            null,
            emptyList(),
            params,
            classifier.builtIns.unitType,
            Modality.FINAL,
            classifier.visibility,
            null
        )

        return listOf(function)
    }
}

class ComposeViewConstructorFunctionDescriptor(
    val clazz: ClassDescriptor,
    containingDeclaration: DeclarationDescriptor,
    original: SimpleFunctionDescriptor?,
    annotations: Annotations,
    name: Name,
    kind: CallableMemberDescriptor.Kind,
    source: SourceElement
) : SimpleFunctionDescriptorImpl(containingDeclaration, original, annotations, name, kind, source)