package com.ivianuu.compose.compiler

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.extensions.SyntheticResolveExtension

class ComposeSyntheticResolveExtension : SyntheticResolveExtension {

    override fun getSyntheticFunctionNames(thisDescriptor: ClassDescriptor): List<Name> {
        error("hello kotlin")
        return listOf(Name.identifier("helloWorld"))
    }

}