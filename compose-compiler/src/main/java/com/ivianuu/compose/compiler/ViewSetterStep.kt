package com.ivianuu.compose.compiler

import com.google.common.collect.SetMultimap
import com.ivianuu.compose.ViewSetter
import com.ivianuu.processingx.filer
import com.ivianuu.processingx.getPackage
import com.ivianuu.processingx.messager
import com.ivianuu.processingx.steps.ProcessingStep
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
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

class ViewSetterStep : ProcessingStep() {
    override fun annotations() = setOf(ViewSetter::class)

    override fun process(elementsByAnnotation: SetMultimap<KClass<out Annotation>, Element>): Set<Element> {
        elementsByAnnotation[ViewSetter::class]
            .filterIsInstance<ExecutableElement>()
            .mapNotNull { createDescriptor(it) }
            .map { ViewSetterGenerator(it) }
            .map { it.generate() }
            .forEach { it.writeTo(filer) }

        return emptySet()
    }

    private fun createDescriptor(element: ExecutableElement): ViewSetterDescriptor? {
        if (element.parameters.size != 2) {
            messager.printMessage(
                ERROR,
                "Must be a extension function and cannot have more than one parameter",
                element
            )
            return null
        }

        // todo
        if (element.returnType.toString() != "void") {
            messager.printMessage(ERROR, "Must return nothing", element)
            return null
        }

        return ViewSetterDescriptor(
            element.getPackage().toString(),
            (element.parameters.first().asType().toString() + element.parameters[1].asType().toString() + element.simpleName.toString())
                .replace(".", "_"), // todo only temporary
            element.parameters.first().asType().asTypeName() as ClassName,
            element.simpleName.toString(),
            element.simpleName.toString().replaceFirst("set", "").decapitalize(),
            (element.parameters[1].asType().asTypeName() as ClassName).javaToKotlinType(),
            element.parameters[1].simpleName.toString()
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

private data class ViewSetterDescriptor(
    val packageName: String,
    val fileName: String,
    val target: TypeName,
    val setterName: String,
    val dslSetterName: String,
    val parameter: TypeName,
    val parameterName: String
)

private class ViewSetterGenerator(private val descriptor: ViewSetterDescriptor) {

    fun generate(): FileSpec {
        return FileSpec.builder(descriptor.packageName, descriptor.fileName)
            .addImport("com.ivianuu.compose.view", "set")
            .addFunction(setterFunction())
            .build()
    }

    private fun setterFunction() = FunSpec.builder(descriptor.dslSetterName)
        .addTypeVariable(TypeVariableName("T", descriptor.target))
        .receiver(VIEW_DSL.parameterizedBy(TypeVariableName("T", descriptor.target)))
        .addParameter(descriptor.parameterName, descriptor.parameter)
        .addCode("set(${descriptor.parameterName}) { ${descriptor.setterName}(it) }")
        .build()

}