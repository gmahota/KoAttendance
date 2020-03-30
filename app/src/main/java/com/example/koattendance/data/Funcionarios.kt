package com.example.koattendance.data

import androidx.room.*
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@Entity
@IgnoreExtraProperties
data class User(
        @PrimaryKey val uid: Int,
        @ColumnInfo(name = "user") var user: String?,
        @ColumnInfo(name = "name") var name: String?,
        @ColumnInfo(name = "phoneNumber") var phoneNumber: String?,
        @ColumnInfo(name = "location") var location: String?,
        @ColumnInfo(name = "token") var token: String?,
        @ColumnInfo(name = "validated") var validated: Boolean


){
    @Exclude
    fun toMap(): Map<String, String?> {
        return mapOf(
                "Codigo" to user,
                "Nome" to name,
                "Telefone" to phoneNumber,
                "Localização" to location
        )
    }
}


@Dao
interface UserDao {
    @Query("SELECT * FROM user")
    fun getAll(): List<User>

    @Query("SELECT * FROM user WHERE uid IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<User>

    @Query("SELECT * FROM user WHERE user LIKE :user LIMIT 1")
    fun findByName(user: String): User

    @Insert
    fun insertAll(vararg users: User)

    @Delete
    fun delete(user: User)

    @Query("DELETE FROM user")
    fun deleteAll()
}

@Database(entities = arrayOf(User::class), version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}