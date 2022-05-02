package com.hci.starsaver.data.bookMark

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class BookMarkRepository(private val BookMarkDao: BookMarkDao) {

    val readAllData: LiveData<List<BookMark>> = BookMarkDao.readAllData()
    val readAllDataByName: LiveData<List<BookMark>> = BookMarkDao.readAllDataByName()
    var readAllDataFolder: LiveData<List<BookMark>> = BookMarkDao.readAllDataFolder(0)
    var readAllDataLink: LiveData<List<BookMark>> = BookMarkDao.readAllDataLink(0)


    suspend fun deleteBookMark(bm: BookMark){
        BookMarkDao.deleteBookMark(bm)
    }

    suspend fun addBookMark(bm: BookMark){
        BookMarkDao.addBookMark(bm)
    }

    fun getStarCount(): LiveData<Int>{
        return BookMarkDao.getCount()
    }

    fun readAllDataFolder(parentId:Long?): LiveData<List<BookMark>> {
        readAllDataFolder = BookMarkDao.readAllDataFolder(parentId)
        return readAllDataFolder
    }

    fun readAllDataLink(parentId:Long?): LiveData<List<BookMark>> {
        readAllDataLink = BookMarkDao.readAllDataLink(parentId)
        return readAllDataLink
    }
}