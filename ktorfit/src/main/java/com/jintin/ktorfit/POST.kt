package com.jintin.ktorfit

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class POST(val path: String)
