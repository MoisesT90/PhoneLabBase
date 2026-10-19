package com.example.phonelab

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ReadAccelerometer::class],
    version = 1,
    exportSchema = false)
abstract class AppDatabase:RoomDatabase(){
    abstract fun lecturaDao():LecturaDao
    companion object{
        @Volatile
        private var INSTANCIA:AppDatabase?=null
        fun obtenerInstancia(context:Context):AppDatabase{
            return INSTANCIA ?: synchronized(this){
                val nueva=Room.databaseBuilder(context.applicationContext,
                    AppDatabase::class.java,"sensores_room.db").build()
                INSTANCIA= nueva
                nueva
            }
        }
    }
}