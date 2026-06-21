package com.example.taskflow

import android.R
import android.content.Intent
import android.app.DatePickerDialog
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.taskflow.dataBase.app.TareaApp
import com.example.taskflow.dataBase.tablas.CategoriasEntity
import com.example.taskflow.dataBase.tablas.TareaEntity
import com.example.taskflow.databinding.ActivityCrearTareaBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class CrearTarea : AppCompatActivity() {
    private lateinit var binding: ActivityCrearTareaBinding
    private var fechaSeleccionadaMilis: Long = System.currentTimeMillis()
    private var idCategoriaSeleccionada: Int? = null
    private var listaCategorias: List<CategoriasEntity> = emptyList()
    private val database by lazy { (application as TareaApp).database }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrearTareaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configurarDropdownPrioridad()
        cargarCategoriasDesdeBaseDeDatos()
        mostrarDatePicker()
        guardarTarea()
        limpiarCampos()
        regresarAlMenuConBoton()
        regresarAlMenuConFlecha()
    }

    private fun configurarDropdownPrioridad() {
        val prioridades = arrayOf("Alta", "Media", "Baja")
        val adapter = ArrayAdapter(this, R.layout.simple_dropdown_item_1line, prioridades)
        binding.etPrioridad.setAdapter(adapter)
    }

    private fun cargarCategoriasDesdeBaseDeDatos() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // LEER DIRECTAMENTE DE ROOM: Traemos las categorías reales insertadas por tu Callback
                listaCategorias = database.categoriasDao().obtenerTodasLasCategorias()

                // Mapeamos la lista de entidades para extraer únicamente los nombres (Strings)
                val nombresCategorias = listaCategorias.map { it.nombre }

                withContext(Dispatchers.Main) {
                    val adapter = ArrayAdapter(this@CrearTarea, R.layout.simple_dropdown_item_1line, nombresCategorias)
                    binding.etCategoria.setAdapter(adapter)

                    // Al seleccionar, buscamos el objeto real en la lista usando la posición
                    binding.etCategoria.setOnItemClickListener { _, _, position, _ ->
                        val categoriaSeleccionada = listaCategorias[position]
                        idCategoriaSeleccionada = categoriaSeleccionada.id // Asignamos su ID real de la BD
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun mostrarDatePicker() {
        binding.etFecha.setOnClickListener {
            val calendario = Calendar.getInstance()
            val año = calendario.get(Calendar.YEAR)
            val mes = calendario.get(Calendar.MONTH)
            val dia = calendario.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, { _, year, month, dayOfMonth ->
                val calSeleccionado = Calendar.getInstance()
                calSeleccionado.set(year, month, dayOfMonth)

                // Guardamos la fecha en milisegundos para Room
                fechaSeleccionadaMilis = calSeleccionado.timeInMillis

                // Mostramos la fecha formateada en el EditText para el usuario
                val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                binding.etFecha.setText(formato.format(calSeleccionado.time))
            }, año, mes, dia)

            datePickerDialog.show()
        }
    }


    private fun guardarTarea() {
        binding.btnGuardar.setOnClickListener {
            val titulo = binding.etTitulo.text.toString().trim()
            val descripcion = binding.etDescripcion.text.toString().trim()
            val prioridad = binding.etPrioridad.text.toString().trim()

            // Validaciones rigurosas de la interfaz
            if (titulo.isEmpty()) {
                Toast.makeText(this, "Por favor, el titulo es obligatorio", Toast.LENGTH_SHORT).show()
            }
            else if (idCategoriaSeleccionada == null) {
                Toast.makeText(this, "Por favor, selecciona una categoría válida", Toast.LENGTH_SHORT).show()
            }

            else if (prioridad.isEmpty()) {
                Toast.makeText(this, "Por favor, selecciona una prioridad", Toast.LENGTH_SHORT).show()
            }
            else {
                // 4. Construcción balanceada del objeto Entity con los tipos correctos
                val nuevaTarea = TareaEntity(
                    titulo = titulo,
                    descripcion = descripcion,
                    prioridad = prioridad,
                    completada = false,
                    fechaLimite = fechaSeleccionadaMilis, // Pasa el Long correctamente
                    categoria_id = idCategoriaSeleccionada!! // Pasa el Int correctamente
                )

                // 5. Inserción mediante corrutinas en el hilo correcto
                lifecycleScope.launch {
                    try {
                        database.tareasDao().insertarTarea(nuevaTarea)
                        Toast.makeText(this@CrearTarea, "Tarea guardada con éxito", Toast.LENGTH_SHORT).show()
                        finish() // Cierra el formulario y vuelve al menú principal
                    } catch (e: Exception) {
                        Toast.makeText(this@CrearTarea, "Error al guardar tarea: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

    }

    private fun limpiarCampos() {
        binding.btnLimpiar.setOnClickListener {
            binding.etTitulo.text?.clear()
            binding.etDescripcion.text?.clear()
            binding.etPrioridad.setText("", false)
            binding.etCategoria.setText("", false)
            binding.etFecha.text?.clear()
            idCategoriaSeleccionada = null
            fechaSeleccionadaMilis = System.currentTimeMillis()
        }
    }

    private fun regresarAlMenuConFlecha(){
        binding.btnSalirFlecha.setOnClickListener {
            val intent = Intent(
                this,
                MainActivity::class.java
            )
            finish()
            startActivity(intent)
        }
    }

    private fun regresarAlMenuConBoton(){
        binding.btnSalir.setOnClickListener {
            val intent = Intent(
                this,
                MainActivity::class.java
            )
            finish()
            startActivity(intent)
        }
    }

}