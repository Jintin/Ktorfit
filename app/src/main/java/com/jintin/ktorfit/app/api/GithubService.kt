package com.jintin.ktorfit.app.api

import com.jintin.githubbrowser.obj.Repo
import com.jintin.githubbrowser.obj.User
import com.jintin.ktorfit.GET
import com.jintin.ktorfit.POST
import com.jintin.ktorfit.Query
import com.jintin.ktorfit.Path
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

interface GithubService {
    @GET("/users/{userName}")
    suspend fun getUser(@Path userName: String): User
    @GET("/users/{name}/repos")
    suspend fun getRepos(@Path("name") userName: String, @Query("sort") sort: String?): List<Repo>
}