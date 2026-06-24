package com.example.taskflow

import android.R
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class CrearTareaActivity : AppCompatActivity() {
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
                // 🌟 CORREGIDO: Agregamos .first() para extraer la lista desde el Flow de Room
                listaCategorias = database.categoriasDao().obtenerTodasLasCategorias().first()

                // Mapeamos la lista de entidades para extraer únicamente los nombres
                val nombresCategorias = listaCategorias.map { it.nombre }

                withContext(Dispatchers.Main) {
                    val adapter = ArrayAdapter(this@CrearTareaActivity, R.layout.simple_dropdown_item_1line, nombresCategorias)
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

            // 1. Validar Título
            if (titulo.isEmpty()) {
                Toast.makeText(this, "Por favor, el título es obligatorio", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Detiene la ejecución aquí
            }

            // 2. Validar Categoría de forma segura capturando el valor en una constante local
            val categoriaId = idCategoriaSeleccionada
            if (categoriaId == null) {
                Toast.makeText(this, "Por favor, selecciona una categoría válida de la lista", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Detiene la ejecución aquí
            }

            // 3. Validar Prioridad
            if (prioridad.isEmpty()) {
                Toast.makeText(this, "Por favor, selecciona una prioridad", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Detiene la ejecución aquí
            }

            // Si llegó aquí, los datos están 100% validados y seguros
            val nuevaTarea = TareaEntity(
                titulo = titulo,
                descripcion = descripcion,
                prioridad = prioridad,
                completada = false,
                fechaLimite = fechaSeleccionadaMilis,
                categoria_id = categoriaId // Usamos la variable local segura y sin '!!'
            )

            // 5. Inserción en la Base de Datos
            lifecycleScope.launch {
                try {
                    database.tareasDao().insertarTarea(nuevaTarea)
                    Toast.makeText(this@CrearTareaActivity, "Tarea guardada con éxito", Toast.LENGTH_SHORT).show()
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this@CrearTareaActivity, "Error al guardar tarea: ${e.message}", Toast.LENGTH_LONG).show()
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
            finish()
        }
    }

    private fun regresarAlMenuConBoton(){
        binding.btnSalir.setOnClickListener {
            finish()
        }
    }

}