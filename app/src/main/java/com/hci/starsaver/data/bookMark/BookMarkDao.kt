package com.hci.starsaver.data.bookMark

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.*

@Dao
interface BookMarkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addBookMark(bm: BookMark)

    @Delete
    suspend fun deleteBookMark(bm: BookMark)

    @Query("SELECT * FROM BookMark ORDER BY id ASC")
    fun readAllData(): LiveData<List<BookMark>>

    @Query("SELECT * FROM BookMark ORDER BY title ASC")
    fun readAllDataByName(): LiveData<List<BookMark>>

    @Query("SELECT * FROM BookMark WHERE isLink = 0 AND parentId =:parentId ORDER BY id ASC ")
    fun readAllDataFolder(parentId:Long?): LiveData<List<BookMark>>

    @Query("SELECT * FROM BookMark WHERE isLink = 0 AND parentId =:parentId ORDER BY title ASC")
    fun readAllDataByNameFolder(parentId:Long?): LiveData<List<BookMark>>

    @Query("SELECT * FROM BookMark WHERE isLink = 1 AND parentId =:parentId ORDER BY id ASC")
    fun readAllDataLink(parentId:Long?): LiveData<List<BookMark>>

    @Query("SELECT * FROM BookMark WHERE isLink = 1 AND parentId =:parentId ORDER BY title ASC")
    fun readAllDataByNameLink(parentId:Long?): LiveData<List<BookMark>>

    @Query("SELECT COUNT(*) FROM BookMark WHERE isLink = 1")
    fun getCount(): LiveData<Int>

}