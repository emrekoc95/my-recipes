package com.kocemre.myrecipes.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.kocemre.myrecipes.model.Recipe
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single

@Dao
interface RecipeDao {

    @Insert
    fun insert(recipe: Recipe) : Completable

    @Delete
    fun delete(recipe: Recipe) : Completable

    @Query("SELECT * FROM Recipe")
    fun getAll(): Flowable<List<Recipe>>

    @Query("SELECT * FROM Recipe WHERE id = :id")
    fun getFromId(id: Int) : Single<Recipe>
}