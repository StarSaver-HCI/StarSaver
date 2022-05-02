package com.hci.starsaver.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class DataStoreModule(private val context : Context) {

    private val Context.dataStore  by preferencesDataStore(name = "dataStore")

    private val remindKey = stringPreferencesKey("remindKey") // string 저장 키값
    private val weekKey = stringPreferencesKey("weekKey") // string 저장 키값
    private val countForWeekKey = stringPreferencesKey("countForWeekKey") // string 저장 키값
    private val numOfRemindKey = stringPreferencesKey("numOfRemindKey") // string 저장 키값
    private val hourKey = stringPreferencesKey("hourKey") // string 저장 키값
    private val minuteKey = stringPreferencesKey("minuteKey") // string 저장 키값
    private val amPmKey = stringPreferencesKey("amPmKey") // string 저장 키값

    val amPm : Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map {preferences ->
            preferences[amPmKey] ?: "오전"
        }

    // String값을 stringKey 키 값에 저장
    suspend fun setAmPm(text : String){
        context.dataStore.edit { preferences ->
            preferences[amPmKey] = text
        }
    }

    val minute : Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map {preferences ->
            preferences[minuteKey] ?: "0"
        }

    // String값을 stringKey 키 값에 저장
    suspend fun setMinute(text : String){
        context.dataStore.edit { preferences ->
            preferences[minuteKey] = text
        }
    }

    val hour : Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map {preferences ->
            preferences[hourKey] ?: "0"
        }

    // String값을 stringKey 키 값에 저장
    suspend fun setHour(text : String){
        context.dataStore.edit { preferences ->
            preferences[hourKey] = text
        }
    }

    val numOfRemind : Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map {preferences ->
            preferences[numOfRemindKey] ?: "0"
        }

    // String값을 stringKey 키 값에 저장
    suspend fun setNumOfRemind(text : String){
        context.dataStore.edit { preferences ->
            preferences[numOfRemindKey] = text
        }
    }

    val remind : Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map {preferences ->
            preferences[remindKey] ?: "0"
        }

    // String값을 stringKey 키 값에 저장
    suspend fun setRemind(text : String){
        context.dataStore.edit { preferences ->
            preferences[remindKey] = text
        }
    }

    val week : Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map {preferences ->
            preferences[weekKey] ?: "0"
        }

    // String값을 stringKey 키 값에 저장
    suspend fun setWeek(text : String){
        context.dataStore.edit { preferences ->
            preferences[weekKey] = text
        }
    }

    val countForWeek : Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map {preferences ->
            preferences[countForWeekKey] ?: "0"
        }

    // String값을 stringKey 키 값에 저장
    suspend fun setCountOfWeek(text : String){
        context.dataStore.edit { preferences ->
            preferences[countForWeekKey] = text
        }
    }
}