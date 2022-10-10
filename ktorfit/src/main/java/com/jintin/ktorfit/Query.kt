package com.jintin.ktorfit

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class Query(val value: String = "")