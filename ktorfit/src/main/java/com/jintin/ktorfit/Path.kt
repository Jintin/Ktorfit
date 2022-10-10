package com.jintin.ktorfit

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class Path(val value: String = "")