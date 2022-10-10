package com.jintin.ktorfit.compiler.internal

import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Nullability
import com.jintin.ktorfit.GET
import com.jintin.ktorfit.POST
import com.jintin.ktorfit.Query
import com.jintin.ktorfit.Path
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName

internal fun ApiData.toFunSpec(): FunSpec {
    val type = String::class.asTypeName() //only support String for now
    return FunSpec.builder(this.name)
        .addModifiers(KModifier.SUSPEND, KModifier.OVERRIDE)
        .addParameters(this.parameters.map {
            ParameterSpec.builder(
                it.value,
                if (it.optional) {
                    type.copy(true)
                } else {
                    type
                },
            )
                .build()
        })
        .generateUrl(this)
        .addStatement(
            """val _result: %T = client.%M(_builder.toString()).%M()""",
            this.result,
            httpMethod.toMemberName(),
            MemberName("io.ktor.client.call", "body")
        )
        .addStatement("return _result")
        .returns(this.result)
        .build()
}

internal fun FunSpec.Builder.generateUrl(data: ApiData): FunSpec.Builder {
    var result = data.url
    data.parameters.filter { it.type == ApiData.ParameterType.PATH }
        .forEach {
            result = data.url.replace("{${it.key}}", "\$${it.value}")
        }
    val querys = data.parameters.filter { it.type == ApiData.ParameterType.QUERY }
    val queryString = querys.filter { !it.optional }.takeIf { it.isNotEmpty() }
        ?.joinToString(prefix = "?", separator = "&") { "${it.key}=\$${it.value}" } ?: ""
    addStatement("""val _builder = StringBuilder("${result + queryString}")""")
    var firstQueryParameter = queryString.isEmpty()
    querys.filter { it.optional }.forEach {
        beginControlFlow("if (${it.value} != null)")
        val pattern = if (firstQueryParameter) {
            firstQueryParameter = false
            "?${it.key}=\$${it.value}"
        } else {
            "&${it.key}=\$${it.value}"
        }
        addStatement("""_builder.append("$pattern")""")
        endControlFlow()

    }
    return this
}

internal fun KSFunctionDeclaration.toApiData(): ApiData {
    val annotation =
        this.annotations.first { HTTP_METHOD_LIST.contains(it.shortName.getShortName()) }
    val path = annotation.arguments.first {
        it.name?.getShortName() == "path"
    }.value.toString()

    val result = returnType?.resolve()?.declaration?.closestClassDeclaration()?.toClassName()
        ?: throw RuntimeException("Transform result type fail")
    val generics =
        returnType?.element?.typeArguments?.mapNotNull { it.type?.toTypeName() }.orEmpty()

    return ApiData(
        this.simpleName.getShortName(),
        if (generics.isEmpty()) {
            result
        } else {
            result.parameterizedBy(generics)
        },
        annotation.toHttpMethod(),
        path,
        this.parameters.mapNotNull {
            it.toParameter()
        })
}

internal fun ApiData.HttpMethod.toMemberName(): MemberName {
    return when (this) {
        ApiData.HttpMethod.GET -> MemberName("io.ktor.client.request", "get", true)
        ApiData.HttpMethod.POST -> MemberName("io.ktor.client.request", "post", true)
    }
}

private fun KSAnnotation.toHttpMethod(): ApiData.HttpMethod {
    return when (val name = this.shortName.getShortName()) {
        GET::class.simpleName -> ApiData.HttpMethod.GET
        POST::class.simpleName -> ApiData.HttpMethod.POST
        else -> throw RuntimeException("Not supported HTTP method $name")
    }
}

private fun KSValueParameter.toParameter(): ApiData.Parameter? {
    val parameter =
        this.annotations.firstOrNull { PARAMETER_LIST.contains(it.shortName.getShortName()) }
            ?: return null
    val key = parameter.arguments.first {
        it.name?.getShortName() == Query::value.name
    }.value.toString()
    return ApiData.Parameter(
        key.ifEmpty { this.name?.getShortName().orEmpty() },
        this.name?.getShortName().orEmpty(),
        this.type.resolve().nullability == Nullability.NULLABLE,
        parameter.toParameterType()
    )
}

private fun KSAnnotation.toParameterType(): ApiData.ParameterType {
    return when (this.shortName.getShortName()) {
        Query::class.simpleName -> ApiData.ParameterType.QUERY
        Path::class.simpleName -> ApiData.ParameterType.PATH
        else -> throw RuntimeException("Not supported type")
    }
}

private val HTTP_METHOD_LIST = listOf(GET::class.simpleName, POST::class.simpleName)
private val PARAMETER_LIST = listOf(Query::class.simpleName, Path::class.simpleName)