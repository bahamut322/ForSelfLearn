package com.sendi.deliveredrobot.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * id INTEGER PRIMARY KEY NOT NULL, file_id INTEGER, file_name TEXT, file_path TEXT
 */
@Entity(tableName = "file_info")
class FileInfo (
    @PrimaryKey(autoGenerate = true)val id: Int = 0,
    @ColumnInfo(name = "file_id")var fileId: Int?,
    @ColumnInfo(name = "file_name")var fileName: String?,
    @ColumnInfo(name = "file_path")var filePath: String?,
    @ColumnInfo(name = "file_type")var fileType: Int?,
    @ColumnInfo(name = "file_suffix")var fileSuffix: String?
)