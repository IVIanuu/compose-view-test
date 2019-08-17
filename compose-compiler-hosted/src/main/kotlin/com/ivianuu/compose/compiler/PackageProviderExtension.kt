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

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.analyzer.ModuleInfo
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentProvider
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.ModuleDescriptorImpl
import org.jetbrains.kotlin.descriptors.impl.PackageFragmentDescriptorImpl
import org.jetbrains.kotlin.descriptors.impl.SimpleFunctionDescriptorImpl
import org.jetbrains.kotlin.descriptors.resolveClassByFqName
import org.jetbrains.kotlin.incremental.components.LookupLocation
import org.jetbrains.kotlin.incremental.components.LookupTracker
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.BindingTrace
import org.jetbrains.kotlin.resolve.descriptorUtil.builtIns
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.descriptorUtil.getAllSuperClassifiers
import org.jetbrains.kotlin.resolve.jvm.extensions.PackageFragmentProviderExtension
import org.jetbrains.kotlin.resolve.scopes.MemberScope
import org.jetbrains.kotlin.resolve.scopes.MemberScopeImpl
import org.jetbrains.kotlin.storage.StorageManager
import org.jetbrains.kotlin.storage.getValue
import org.jetbrains.kotlin.utils.Printer

// todo way to slow and heavy :/

class ComposePackageFragmentProviderExtension : PackageFragmentProviderExtension {
    override fun getPackageFragmentProvider(
        project: Project,
        module: ModuleDescriptor,
        storageManager: StorageManager,
        trace: BindingTrace,
        moduleInfo: ModuleInfo?,
        lookupTracker: LookupTracker
    ): PackageFragmentProvider? {
        return ComposePackageFragmentProvider(module, lookupTracker, storageManager)
    }
}

private class ComposePackageFragmentProvider(
    private val module: ModuleDescriptor,
    private val lookupTracker: LookupTracker,
    private val storageManager: StorageManager
) : PackageFragmentProvider {

    private val siblings by storageManager.createLazyValue {
        val compositeProvider = (module as ModuleDescriptorImpl).packageFragmentProvider
        (compositeProvider.javaClass.getDeclaredField("providers")
            .also { it.isAccessible = true }
            .get(compositeProvider) as MutableList<PackageFragmentProvider>)
            .filter { it != this }
    }

    private val packagesByName =
        storageManager.createMemoizedFunction<FqName, List<PackageFragmentDescriptor>> { fqName ->
            listOf(
                ComposePackageFragmentDescriptor(
                    module,
                    storageManager.createLazyValue {
                        siblings.flatMap { it.getPackageFragments(fqName) }
                    },
                    lookupTracker, storageManager, fqName
                )
            )
        }

    override fun getPackageFragments(fqName: FqName): List<PackageFragmentDescriptor> =
        packagesByName(fqName)

    override fun getSubPackagesOf(
        fqName: FqName,
        nameFilter: (Name) -> Boolean
    ): Collection<FqName> = emptyList()
}

private class ComposePackageFragmentDescriptor(
    private val module: ModuleDescriptor,
    private val lazyFragments: () -> List<PackageFragmentDescriptor>,
    private val lookupTracker: LookupTracker,
    private val storageManager: StorageManager,
    fqName: FqName
) : PackageFragmentDescriptorImpl(module, fqName) {

    private val scope by storageManager.createLazyValue {
        val lazyScopes = storageManager.createLazyValue {
            lazyFragments()
                .map { it.getMemberScope() }
        }

        ComposeMemberScope(module, lazyScopes, lookupTracker, storageManager, this)
    }

    override fun getMemberScope(): MemberScope = scope

}

private val VIEW = FqName("android.view.View")
private val VIEW_COMPOSITION = FqName("androidx.compose.ViewComposition")

private class ComposeMemberScope(
    private val module: ModuleDescriptor,
    private val lazyScopes: () -> List<MemberScope>,
    private val lookupTracker: LookupTracker,
    private val storageManager: StorageManager,
    private val packageFragment: ComposePackageFragmentDescriptor
) : MemberScopeImpl() {

    private val functions by storageManager.createLazyValue {
        val viewClasses = getViewClasses()

        println("view classes $viewClasses")

        val settersByClass = viewClasses
            .flatMap { viewClass ->
                viewClass.unsubstitutedMemberScope
                    .getFunctionNames()
                    .flatMap {
                        viewClass.unsubstitutedMemberScope
                            .getContributedFunctions(it, NoLookupLocation.FROM_BACKEND)

                    }
                    .filter {
                        (it.valueParameters.size == 1
                                && it.name.asString().startsWith("set"))
                            .also { r ->
                                println("is $it a setter ? $r")
                            }
                    }
                    .map { viewClass to it }
            }
            .groupBy { it.first }

        println("setters by class $settersByClass")

        // todo add properties
        val funs = settersByClass.map { (clazz, setters) ->
            println("setters by class $clazz $setters")
            SimpleFunctionDescriptorImpl.create(
                packageFragment,
                Annotations.EMPTY,
                clazz.name,
                CallableMemberDescriptor.Kind.SYNTHESIZED,
                SourceElement.NO_SOURCE
            )
                .initialize(
                    module.resolveClassByFqName(
                        VIEW_COMPOSITION,
                        NoLookupLocation.FROM_BACKEND
                    )!!.thisAsReceiverParameter,
                    null,
                    emptyList(),
                    emptyList(),//setters.map { it.second.valueParameters.first() },
                    clazz.builtIns.unitType,
                    null,
                    clazz.visibility,
                    null
                )
        }

        funs.let { println("functions $it") }

        funs
    }

    override fun getFunctionNames(): Set<Name> = functions.map { it.name }.toSet()

    override fun getContributedFunctions(
        name: Name,
        location: LookupLocation
    ): Collection<SimpleFunctionDescriptor> = functions

    override fun printScopeStructure(p: Printer) {
    }

    private fun getViewClasses(): List<ClassDescriptor> = lazyScopes()
        .flatMap { scope ->
            (scope.getClassifierNames() ?: emptySet())
                .mapNotNull { scope.getContributedClassifier(it, NoLookupLocation.FROM_BACKEND) }
        }
        .filter { classifier ->
            (classifier.fqNameSafe == VIEW || classifier.getAllSuperClassifiers()
                .any { it.fqNameSafe == VIEW })
                .also { println("is $classifier a view ? $it") }
        }
        .filterIsInstance<ClassDescriptor>()
    //.filter { it.modality != Modality.ABSTRACT && it.modality != Modality.SEALED }
}