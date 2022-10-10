package com.jintin.ktorfit

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class GET(val path: String)
