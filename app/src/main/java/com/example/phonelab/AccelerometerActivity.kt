package com.example.phonelab

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.SystemClock
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class AccelerometerActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private val dao by lazy {
        AppDatabase.obtenerInstancia(applicationContext).lecturaDao()
    }

    private lateinit var tvX: TextView
    private lateinit var tvY: TextView
    private lateinit var tvZ: TextView
    private lateinit var tvSensorState: TextView
    private lateinit var tvMuestras: TextView
    private lateinit var tvTotal: TextView
    private lateinit var btnIniciar: Button
    private lateinit var btnDetener: Button

    private var registrando = false
    private var ultimoGuardadoMs = 0L
    private var muestrasSesion = 0
    private var contadorTotal = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accelerometer)

        tvX = findViewById(R.id.tvX)
        tvY = findViewById(R.id.tvY)
        tvZ = findViewById(R.id.tvZ)
        tvSensorState = findViewById(R.id.tvSensorState)
        tvMuestras = findViewById(R.id.tvMuestras)
        tvTotal = findViewById(R.id.tvTotal)
        btnIniciar = findViewById(R.id.btnIniciar)
        btnDetener = findViewById(R.id.btnDetener)
        val btnHistorial = findViewById<Button>(R.id.btnHistorial)

        sensorManager = getSystemService(Context.SENSOR_SERVICE)
                as SensorManager
        accelerometer = sensorManager.getDefaultSensor(
            Sensor.TYPE_ACCELEROMETER
        )

        btnIniciar.setOnClickListener {
            if (accelerometer != null) {
                registrando = true
                ultimoGuardadoMs = 0L
                muestrasSesion = 0
                mostrarContadores()
                actualizarBotones()
            }
        }
        btnDetener.setOnClickListener { detenerRegistro() }
        btnHistorial.setOnClickListener {
            startActivity(Intent(this, HistorialActivity::class.java))
        }

        if (accelerometer == null) {
            Toast.makeText(this, "Sin acelerómetro", Toast.LENGTH_LONG).show()
        }
        mostrarContadores()
        actualizarBotones()
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let { sensor ->
            sensorManager.registerListener(
                this, sensor, SensorManager.SENSOR_DELAY_UI
            )
        }
        actualizarTotalDesdeBD()
    }

    override fun onPause() {
        detenerRegistro()
        sensorManager.unregisterListener(this)
        super.onPause()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        tvX.text = String.format(Locale.getDefault(), "X: %.2f m/s²", x)
        tvY.text = String.format(Locale.getDefault(), "Y: %.2f m/s²", y)
        tvZ.text = String.format(Locale.getDefault(), "Z: %.2f m/s²", z)

        val ahora = SystemClock.elapsedRealtime()
        if (registrando && ahora - ultimoGuardadoMs >= 1000L) {
            ultimoGuardadoMs = ahora
            guardarMuestra(x, y, z)
        }
    }

    private fun guardarMuestra(x: Float, y: Float, z: Float) {
        val lectura = ReadAccelerometer(
            timestampMs = System.currentTimeMillis(),
            x = x, y = y, z = z
        )
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    dao.insertar(lectura)
                }
                muestrasSesion++
                contadorTotal++
                mostrarContadores()
            } catch (e: Exception) {
                registrando = false
                actualizarBotones()
                Toast.makeText(
                    this@AccelerometerActivity,
                    "No fue posible guardar la muestra",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun actualizarTotalDesdeBD() {
        lifecycleScope.launch {
            contadorTotal = withContext(Dispatchers.IO) {
                dao.contar()
            }
            mostrarContadores()
        }
    }

    private fun mostrarContadores() {
        tvMuestras.text = "Muestras de esta sesión: $muestrasSesion"
        tvTotal.text = "Total almacenado: $contadorTotal"
    }

    private fun detenerRegistro() {
        registrando = false
        actualizarBotones()
    }

    private fun actualizarBotones() {
        btnIniciar.isEnabled = accelerometer != null && !registrando
        btnDetener.isEnabled = registrando
        tvSensorState.text = when {
            accelerometer == null -> "Sensor no disponible"
            registrando -> "Estado: registrando (1 muestra/s aprox.)"
            else -> "Estado: detenido"
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
}

