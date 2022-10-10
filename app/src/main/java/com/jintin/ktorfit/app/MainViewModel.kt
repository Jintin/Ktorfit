package com.jintin.ktorfit.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jintin.ktorfit.app.api.createGithubService
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

class MainViewModel : ViewModel() {

    suspend fun load(): String {
        val client = HttpClient(CIO) {
            defaultRequest {
                url("https://api.github.com")
            }
            install(Logging)
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
        }
        val builder = StringBuilder()
        val service = client.createGithubService()
        val repos = service.getRepos("Jintin", null)
        builder.append(repos.toString() + "\n")
        val user = service.getUser("Jintin")
        builder.append(user.toString())
        return builder.toString()
    }

}
