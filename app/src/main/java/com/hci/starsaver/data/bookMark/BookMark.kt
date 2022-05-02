package com.hci.starsaver.data.bookMark

import android.graphics.Bitmap
import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class BookMark(
    @PrimaryKey(autoGenerate = true)
    val id:Long?,
    var parentId:Long?,
    var title: String,
    var description:String,
    var isLink:Int,
    var link:String?,
    var isRemind:Boolean=false,
    var isStar:Boolean = false,
    var bitmap: Bitmap? = null
):Serializable