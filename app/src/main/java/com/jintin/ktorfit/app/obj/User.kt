package com.jintin.githubbrowser.obj

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val login: String,
    val id: Long,
    @SerialName("avatar_url")
    val avatarUrl: String,
)