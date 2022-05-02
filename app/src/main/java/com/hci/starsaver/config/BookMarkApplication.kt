package com.hci.starsaver.config
import android.app.Application
import com.hci.starsaver.data.datastore.DataStoreModule

class BookMarkApplication : Application() {

    private lateinit var dataStore: DataStoreModule

    companion object {
        private lateinit var bookMarkApplication: BookMarkApplication
        fun getInstance(): BookMarkApplication = bookMarkApplication
        lateinit var prefs: Prefs
    }

    override fun onCreate() {
        super.onCreate()
        bookMarkApplication = this
        dataStore = DataStoreModule(this)
        prefs = Prefs(applicationContext)
    }

    fun getDataStore(): DataStoreModule = dataStore

}