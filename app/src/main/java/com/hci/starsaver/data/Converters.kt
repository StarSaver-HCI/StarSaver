package com.hci.starsaver.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream

class Converters {
    @TypeConverter
    fun toByteArray(bitmap : Bitmap?) : ByteArray?{
        if(bitmap==null) return null
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }

    @TypeConverter
    fun toBitmap(bytes : ByteArray?) : Bitmap?{
        if(bytes==null) return null
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}