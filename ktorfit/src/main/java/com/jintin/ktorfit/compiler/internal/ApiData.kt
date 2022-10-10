package com.jintin.ktorfit.compiler.internal

import com.squareup.kotlinpoet.TypeName

internal data class ApiData(
    val name: String,
    val result: TypeName,
    val httpMethod: HttpMethod,
    val url: String,
    val parameters: List<Parameter>
) {

    internal enum class HttpMethod {
        GET, POST;
    }

    internal data class Parameter(val key: String, val value: String, val optional: Boolean, val type: ParameterType)

    internal enum class ParameterType {
        PATH, QUERY
    }
}
