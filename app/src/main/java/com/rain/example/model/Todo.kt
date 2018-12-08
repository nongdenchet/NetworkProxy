package com.rain.example.model

import com.google.gson.annotations.SerializedName

data class Todo(
        @SerializedName("id") val id: Int,
        @SerializedName("title") val title: String
)
