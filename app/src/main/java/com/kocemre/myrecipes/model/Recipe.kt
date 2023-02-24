package com.kocemre.myrecipes.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Recipe(
    @ColumnInfo(name = "name")
    var name: String?,
    @ColumnInfo(name = "recipe")
    var recipe: String?,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    var byteArray: ByteArray
) {
    @PrimaryKey(autoGenerate = true)
    var id = 0
}