package com.example.labs.database

import androidx.room.*
import androidx.room.Insert
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity)

    @Query("SELECT * FROM users WHERE email = :email")
    fun getUser(email: String): Flow<UserEntity?>

    @Delete
    suspend fun delete(user: UserEntity)

    @Query("UPDATE users SET coins = coins + :delta WHERE email = :email")
    suspend fun changeCoins(email: String, delta: Int)

    @Query("UPDATE users SET coins = :value WHERE email = :email")
    suspend fun setCoins(email: String, value: Int)

    @Query("SELECT coins FROM users WHERE email = :email")
    fun getCoins(email: String): Flow<Int?>
}