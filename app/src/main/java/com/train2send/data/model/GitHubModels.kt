package com.train2send.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitHubFile(
    val name: String,
    val path: String,
    val sha: String,
    val size: Int,
    val url: String,
    @SerialName("html_url") val htmlUrl: String,
    @SerialName("git_url") val gitUrl: String,
    @SerialName("download_url") val downloadUrl: String?,
    val type: String
)
