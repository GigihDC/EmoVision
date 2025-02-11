package com.verve.emovision.data.model

import com.google.gson.annotations.SerializedName

data class Response<T>(
    @SerializedName("code")
    val code: Int,
    @SerializedName("is_success")
    val isSuccess: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: T?,
)
