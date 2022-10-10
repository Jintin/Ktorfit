package com.jintin.ktorfit.compiler

import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.jintin.ktorfit.GET
import com.jintin.ktorfit.POST
import com.jintin.ktorfit.compiler.internal.ApiData
import com.jintin.ktorfit.compiler.internal.primaryConstructor
import com.jintin.ktorfit.compiler.internal.toApiData
import com.jintin.ktorfit.compiler.internal.toFunSpec
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo

class KtorfitProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val map = mutableMapOf<KSClassDeclaration, MutableList<ApiData>>()
        val targetAnnotations = listOf(GET::class, POST::class)
        targetAnnotations.forEach {
            resolver.getSymbolsWithAnnotation(it.qualifiedName.orEmpty())
                .filterIsInstance<KSFunctionDeclaration>()
                .forEach {
                    val clazz = it.closestClassDeclaration()!!
                    val list = map.getOrPut(clazz, ::mutableListOf)
                    list.add(it.toApiData())
                }
        }

        map.forEach {
            genFiles(it.key.toClassName(), it.value).writeTo(codeGenerator, Dependencies(true))
        }
        return emptyList()
    }

    private fun genFiles(className: ClassName, data: List<ApiData>): FileSpec {
        val clzName = className.simpleName + "Impl"
        return FileSpec.builder(className.packageName, clzName)
            .addFunction(FunSpec.builder( "create${className.simpleName}")
                .receiver(HTTP_CLIENT)
                .addStatement("return ${clzName}(this)")
                .returns(className)
                .build())
            .addType(
                TypeSpec.classBuilder(clzName)
                    .addModifiers(KModifier.PRIVATE)
                    .addSuperinterface(className)
                    .primaryConstructor(
                        PropertySpec.builder("client", HTTP_CLIENT)
                            .addModifiers(KModifier.PRIVATE)
                            .build()
                    )
                    .addFunctions(data.map(ApiData::toFunSpec))
                    .build()
            )
            .build()
    }

    companion object {
        val HTTP_CLIENT = ClassName("io.ktor.client", "HttpClient")
    }
}
