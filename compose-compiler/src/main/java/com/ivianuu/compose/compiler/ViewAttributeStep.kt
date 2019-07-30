package com.ivianuu.compose.compiler

import com.google.common.collect.SetMultimap
import com.ivianuu.compose.ViewAttribute
import com.ivianuu.processingx.filer
import com.ivianuu.processingx.getPackage
import com.ivianuu.processingx.messager
import com.ivianuu.processingx.steps.ProcessingStep
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.WildcardTypeName
import com.squareup.kotlinpoet.asTypeName
import me.eugeniomarletti.kotlin.metadata.shadow.name.FqName
import me.eugeniomarletti.kotlin.metadata.shadow.platform.JavaToKotlinClassMap
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.tools.Diagnostic.Kind.ERROR
import kotlin.reflect.KClass

class ViewAttributeStep : ProcessingStep() {
    override fun annotations() = setOf(ViewAttribute::class)

    override fun process(elementsByAnnotation: SetMultimap<KClass<out Annotation>, Element>): Set<Element> {
        elementsByAnnotation[ViewAttribute::class]
            .filterIsInstance<ExecutableElement>()
            .mapNotNull { createDescriptor(it) }
            .map { ViewAttributeGenerator(it) }
            .map { it.generate() }
            .forEach { it.writeTo(filer) }

        return emptySet()
    }

    private fun createDescriptor(element: ExecutableElement): ViewAttributeDescriptor? {
        // todo should we allow only receiver?
        if (element.parameters.isEmpty()) {
            messager.printMessage(ERROR, "Must have a receiver and atleast on parameter", element)
            return null
        }

        // todo
        if (element.returnType.toString() != "void") {
            messager.printMessage(ERROR, "Must return nothing", element)
            return null
        }

        val fileName =
            (element.simpleName.toString() + element.parameters.joinToString("") { it.asType().toString() + it.simpleName.toString() })
                .replace(".", "-")

        return ViewAttributeDescriptor(
            element.getPackage().toString(),
            fileName, // todo only temporary
            false, // todo find a way to check internal
            element.parameters.first().asType().asTypeName() as ClassName, // todo better way to get receiver
            element.simpleName.toString(),
            element.simpleName.toString().replaceFirst("set", "").decapitalize(),
            element.parameters
                .drop(1)
                .map {
                    ViewAttributeDescriptor.ParamDescriptor(
                        (it.asType().asTypeName() as ClassName).javaToKotlinType(),
                        it.simpleName.toString()
                    )
                }
        )
    }

    private fun TypeName.javaToKotlinType(): TypeName {
        return if (this is WildcardTypeName) {
            if (outTypes.isNotEmpty()) {
                outTypes.first().javaToKotlinType()
            } else {
                inTypes.first().javaToKotlinType()
            }
        } else if (this is ParameterizedTypeName) {
            (rawType.javaToKotlinType() as ClassName).parameterizedBy(
                *typeArguments.map { it.javaToKotlinType() }.toTypedArray()
            )
        } else {
            val className =
                JavaToKotlinClassMap.mapJavaToKotlin(FqName(toString()))?.asSingleFqName()
                    ?.asString()
            if (className == null) this
            else ClassName.bestGuess(className)
        }
    }

}

private val VIEW_DSL = ClassName("com.ivianuu.compose.view", "ViewDsl")

private data class ViewAttributeDescriptor(
    val packageName: String,
    val fileName: String,
    val isInternal: Boolean,
    val target: TypeName,
    val setterName: String,
    val dslSetterName: String,
    val parameters: List<ParamDescriptor>
) {
    data class ParamDescriptor(
        val type: TypeName,
        val name: String
    )
}

private class ViewAttributeGenerator(private val descriptor: ViewAttributeDescriptor) {

    fun generate(): FileSpec {
        return FileSpec.builder(descriptor.packageName, descriptor.fileName)
            .addImport("com.ivianuu.compose.view", "set")
            .addFunction(setterFunction())
            .build()
    }

    private fun setterFunction() = FunSpec.builder(descriptor.dslSetterName)
        .apply { if (descriptor.isInternal) addModifiers(KModifier.INTERNAL) }
        .addTypeVariable(TypeVariableName("T", descriptor.target))
        .receiver(VIEW_DSL.parameterizedBy(TypeVariableName("T", descriptor.target)))
        .apply {
            descriptor.parameters.forEach { addParameter(it.name, it.type) }

            // optimize on single parameters
            when (descriptor.parameters.size) {
                0 -> addCode("set { ${descriptor.setterName}() }")
                1 -> addCode("set(${descriptor.parameters.first().name}) { ${descriptor.setterName}(it) }")
                else -> {
                    val parameterNamesString = descriptor.parameters.joinToString(", ") { it.name }
                    addCode("set(listOf($parameterNamesString)) { ${descriptor.setterName}($parameterNamesString) }")
                }
            }
        }
        .build()

}