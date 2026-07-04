package com.cncindex.model

import com.google.gson.annotations.SerializedName

data class IndexFile(
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("entries") val entries: List<IndexEntry>
)

data class IndexEntry(
    @SerializedName("filename") val filename: String,
    @SerializedName("program_number") val programNumber: String?,
    @SerializedName("program_name") val programName: String?,
    @SerializedName("filepath") val filepath: String?,
    @SerializedName("tools") val tools: List<Int>,
    @SerializedName("modified") val modified: Double,
    @SerializedName("problem") val problem: Boolean,
    @SerializedName("duplicate") val duplicate: Boolean,
    @SerializedName("md5") val md5: String?
)
