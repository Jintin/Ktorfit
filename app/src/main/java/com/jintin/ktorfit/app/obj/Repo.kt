package com.jintin.githubbrowser.obj

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Repo(
    val id: Long,
    val name: String,
    val owner: User,
    @SerialName("stargazers_count")
    val starCount: Int
)