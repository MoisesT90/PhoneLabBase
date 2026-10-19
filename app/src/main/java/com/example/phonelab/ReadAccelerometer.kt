package com.example.phonelab

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lecturas_acelerometro")
data class ReadAccelerometer(
    @PrimaryKey(autoGenerate = true)
    val id:Long=0L,
    val timestampMs:Long,
    val x:Float,
    val y:Float,
    val z:Float
)