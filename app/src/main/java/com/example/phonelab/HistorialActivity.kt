package com.example.phonelab

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistorialActivity : AppCompatActivity() {

    private val dao by lazy {
        AppDatabase.obtenerInstancia(applicationContext).lecturaDao()
    }
    private val adaptador = ReadAdapter()
    private lateinit var txtResumen: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial)

        txtResumen = findViewById(R.id.tvResumen)
        val lista = findViewById<RecyclerView>(R.id.listLecturas)
        val btnActualizar = findViewById<Button>(R.id.btnActualizar)
        val btnBorrar = findViewById<Button>(R.id.btnBorrar)

        lista.layoutManager = LinearLayoutManager(this)
        lista.adapter = adaptador
        btnActualizar.setOnClickListener { cargarHistorial() }
        btnBorrar.setOnClickListener { confirmarBorrado() }
    }

    override fun onResume() {
        super.onResume()
        cargarHistorial()
    }

    private fun cargarHistorial() {
        lifecycleScope.launch {
            txtResumen.text = "Consultando base de datos..."
            val (datos, total) = withContext(Dispatchers.IO) {
                Pair(dao.obtener(), dao.contar())
            }
            adaptador.actualizar(datos)
            txtResumen.text = "Mostrando ${datos.size} de $total registros"
        }
    }

    private fun confirmarBorrado() {
        AlertDialog.Builder(this)
            .setTitle("Eliminar historial")
            .setMessage("¿Eliminar todas las mediciones guardadas?")
            .setPositiveButton("Eliminar") { _, _ ->
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        dao.borrarTodo()
                    }
                    cargarHistorial()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
