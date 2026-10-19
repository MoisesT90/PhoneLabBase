package com.example.phonelab

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao interface LecturaDao {
    @Insert
    suspend fun insertar(lectura:ReadAccelerometer):Long
    @Query("SELECT*FROM lecturas_acelerometro "+
            "ORDER BY timestampMS DESC LIMIT 200")
    suspend fun obtener():List<ReadAccelerometer>
    @Query("SELECT COUNT(*) FROM lecturas_acelerometro")
    suspend fun contar():Int
    @Query("DELETE FROM lecturas_acelerometro")
    suspend fun borrarTodo():Int
}