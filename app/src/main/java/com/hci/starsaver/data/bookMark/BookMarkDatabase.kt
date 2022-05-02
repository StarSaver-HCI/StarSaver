package com.hci.starsaver.data.bookMark

import android.content.Context
import androidx.room.*
import com.hci.starsaver.data.Converters

@Database(entities = [BookMark::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class BookMarkDatabase: RoomDatabase() {

    abstract fun BookMarkDao(): BookMarkDao

    companion object{
        private var INSTANCE: BookMarkDatabase? = null

        fun getDatabase(context: Context): BookMarkDatabase{

            val tempInstance= INSTANCE
            if( tempInstance != null) {
                return tempInstance
            }
            // safe multithreading
            synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BookMarkDatabase::class.java,
                    "bm_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}