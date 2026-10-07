package com.example.data.remote

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MyMemoryResponse(
    val responseData: MyMemoryData? = null,
    val responseStatus: Any? = null
)

@JsonClass(generateAdapter = true)
data class MyMemoryData(
    val translatedText: String? = null,
    val match: Double? = null
)
