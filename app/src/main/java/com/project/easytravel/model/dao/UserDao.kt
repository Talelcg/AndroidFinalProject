package com.project.easytravel.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.project.easytravel.model.User

@Dao
interface UserDao {


    @Query("SELECT * FROM User WHERE id =:id")
    fun getUserById(id: String): User


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(vararg users: User)


    @Update
    fun updateUser(user: User)

}