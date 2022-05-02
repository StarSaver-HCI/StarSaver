package com.hci.starsaver.config

import android.content.Context
import android.content.Context.MODE_PRIVATE

class Prefs(context: Context) {
    private val prefNm="mPref"
    private val prefs=context.getSharedPreferences(prefNm,MODE_PRIVATE)

    var userNickName : String?
        get() = prefs.getString("userNickName",null)
        set(value){
            prefs.edit().putString("userNickName",value).apply()
        }
    var homeName : String?
        get() = prefs.getString("homeName","home")
        set(value){
            prefs.edit().putString("homeName",value).apply()
        }

    var homeDescription : String?
        get() = prefs.getString("homeDescription","")
        set(value){
            prefs.edit().putString("homeDescription",value).apply()
        }

    var homeIsRemind : Boolean
        get() = prefs.getBoolean("homeIsRemind",false)
        set(value){
            prefs.edit().putBoolean("homeIsRemind",value).apply()
        }

    var remindAvailable : Boolean
        get() = prefs.getBoolean("remindAvailable",false)
        set(value){
            prefs.edit().putBoolean("remindAvailable",value).apply()
        }

    var amOrPm:String?
        get() = prefs.getString("amOrPm","오전")
        set(value){
            prefs.edit().putString("amOrPm", value).apply()
        }

    var hour : Int?
        get() = prefs.getInt("hour",12)
        set(value){
            prefs.edit().putInt("hour",value!!).apply()
        }
    var minute : Int?
        get() = prefs.getInt("minute",0)
        set(value){
            prefs.edit().putInt("minute",value!!).apply()
        }

    var week : Int?
        get() = prefs.getInt("week",0)
        set(value){
            prefs.edit().putInt("week",value!!).apply()
        }

    var notificationBun : Int?
        get() = prefs.getInt("notificationBun",0)
        set(value){
            prefs.edit().putInt("notificationBun",value!!).apply()
        }
    var notificationGae : Int?
        get() = prefs.getInt("notificationGae",0)
        set(value){
            prefs.edit().putInt("notificationGae",value!!).apply()
        }
}