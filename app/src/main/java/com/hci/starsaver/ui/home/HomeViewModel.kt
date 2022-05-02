package com.hci.starsaver.ui.home

import android.app.Application
import androidx.lifecycle.*
import com.hci.starsaver.config.BookMarkApplication
import com.hci.starsaver.data.bookMark.BookMark
import com.hci.starsaver.data.bookMark.BookMarkDao
import com.hci.starsaver.data.bookMark.BookMarkDatabase
import com.hci.starsaver.data.bookMark.BookMarkRepository
import com.hci.starsaver.ui.home.PathName.Companion.HOME
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class HomeViewModel(applications: Application): AndroidViewModel(applications) {

    //not private
    val readAllData: LiveData<List<BookMark>>
    val readAllDataByName: LiveData<List<BookMark>>
    val currentBookMark: MutableLiveData<BookMark>
    val bmStack: LiveData<Stack<BookMark>>
    val sortByName : MutableLiveData<Boolean>

    private val repository: BookMarkRepository

    init{
        val bookMarkDao = BookMarkDatabase.getDatabase(applications).BookMarkDao()
        repository = BookMarkRepository(bookMarkDao)
        readAllData = repository.readAllData
        readAllDataByName = repository.readAllDataByName
        currentBookMark = MutableLiveData<BookMark>(BookMark(0,-1,BookMarkApplication.prefs.homeName!!,
            BookMarkApplication.prefs.homeDescription!!,-1,""
            , isRemind = BookMarkApplication.prefs.homeIsRemind))
        bmStack = MutableLiveData(Stack<BookMark>().apply { add(currentBookMark.value) })
        sortByName = MutableLiveData(false)
    }

    fun deleteBookMark(bm: BookMark){
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteBookMark(bm)
        }
    }

    fun addBookMark(bm: BookMark){
        viewModelScope.launch(Dispatchers.IO) {
            repository.addBookMark(bm)
        }
    }

    fun getStarCount(): LiveData<Int> {
        return repository.getStarCount()
    }

    fun moveFolder(bm:BookMark){
        currentBookMark.postValue(bm)
        bmStack.value!!.add(bm)
    }

    fun popFolder(){
        if(bmStack.value!!.size==1) return
        bmStack.value!!.pop()
        currentBookMark.postValue(bmStack.value!!.peek())
    }

    fun getPath():String{
        var str=""
        bmStack.value!!.forEachIndexed{ idx,bm->
            str+= bm.title
            if(idx!=bmStack.value!!.lastIndex)str+=" > "
        }
        return str
    }

}