package com.ivianuu.compose.compiler

import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.SimpleFunctionDescriptorImpl
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.descriptorUtil.builtIns
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.extensions.SyntheticResolveExtension

class ComposeSyntheticResolveExtension : SyntheticResolveExtension {

    override fun getSyntheticFunctionNames(thisDescriptor: ClassDescriptor): List<Name> {
        println("get syn fun names $thisDescriptor name is ${thisDescriptor.fqNameSafe}")
        if (thisDescriptor.fqNameSafe.asString() == "androidx.compose.ViewComposition") {
            return listOf(Name.identifier("LinearLayout"))
        }
        return listOf(Name.identifier("helloWorldCompiler"))
    }

    override fun generateSyntheticMethods(
        thisDescriptor: ClassDescriptor,
        name: Name,
        bindingContext: BindingContext,
        fromSupertypes: List<SimpleFunctionDescriptor>,
        result: MutableCollection<SimpleFunctionDescriptor>
    ) {
        super.generateSyntheticMethods(thisDescriptor, name, bindingContext, fromSupertypes, result)

        if (name.asString() == "LinearLayout") {
            result += SimpleFunctionDescriptorImpl.create(
                thisDescriptor,
                Annotations.EMPTY, name,
                CallableMemberDescriptor.Kind.SYNTHESIZED, SourceElement.NO_SOURCE
            )
                .initialize(
                    null,
                    thisDescriptor.thisAsReceiverParameter,
                    emptyList(),
                    emptyList(),
                    thisDescriptor.builtIns.unitType,
                    Modality.FINAL,
                    Visibilities.PUBLIC
                )
        }

        if (name.asString() == "helloWorldCompiler") {
            result += SimpleFunctionDescriptorImpl.create(
                thisDescriptor,
                Annotations.EMPTY, name,
                CallableMemberDescriptor.Kind.SYNTHESIZED, SourceElement.NO_SOURCE
            )
                .initialize(
                    null,
                    thisDescriptor.thisAsReceiverParameter,
                    emptyList(),
                    emptyList(),
                    thisDescriptor.builtIns.unitType,
                    Modality.FINAL,
                    Visibilities.PUBLIC
                )
        }
    }

}