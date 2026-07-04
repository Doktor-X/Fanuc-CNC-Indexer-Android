package com.cncindex.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tools")
data class Tool(
    @PrimaryKey
    @ColumnInfo(name = "tool_number")
    val toolNumber: Int,

    @ColumnInfo(name = "name")
    val name: String
)
