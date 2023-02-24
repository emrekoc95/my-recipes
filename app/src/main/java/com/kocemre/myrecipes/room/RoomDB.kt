package com.kocemre.myrecipes.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kocemre.myrecipes.model.Recipe

@Database(entities = [Recipe::class], version = 1)
abstract class RoomDB : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
}