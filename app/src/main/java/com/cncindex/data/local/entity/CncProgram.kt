package com.cncindex.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "programs")
data class CncProgram(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "filename")
    val filename: String,

    @ColumnInfo(name = "program_number")
    val programNumber: String?,

    @ColumnInfo(name = "program_name")
    val programName: String?,

    @ColumnInfo(name = "tools_json")
    val toolsJson: String,

    @ColumnInfo(name = "tool_names")
    val toolNames: String,

    @ColumnInfo(name = "modified")
    val modified: Long,

    @ColumnInfo(name = "has_problem")
    val hasProblem: Boolean,

    @ColumnInfo(name = "is_duplicate")
    val isDuplicate: Boolean,

    // md5 grupa – duplikati iste grupe imaju isti md5_group
    @ColumnInfo(name = "md5_group")
    val md5Group: String?,

    // Apsolutna putanja na PC-u (samo za prikaz, ne može se otvoriti na Androidu)
    @ColumnInfo(name = "filepath")
    val filepath: String? = null
)
