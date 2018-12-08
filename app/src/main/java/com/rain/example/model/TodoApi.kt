package com.rain.example.model

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface TodoApi {
    @GET("todos/{id}")
    fun getTodo(@Path("id") id: Int): Call<Todo>
}
