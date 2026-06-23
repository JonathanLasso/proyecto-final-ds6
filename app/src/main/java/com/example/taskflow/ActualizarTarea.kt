package com.example.taskflow

import android.app.DatePickerDialog
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.taskflow.dataBase.app.TareaApp
import com.example.taskflow.dataBase.tablas.TareaEntity
import com.example.taskflow.databinding.ActivityActualizarTareaBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.Locale

class ActualizarTarea : AppCompatActivity() {

    private lateinit var binding: ActivityActualizarTareaBinding
    private val database by lazy { (application as TareaApp).database }
    private var tareaId: Int = -1
    private var fechaSeleccionadaMilis: Long = System.currentTimeMillis()
    private var idCategoriaOriginal: Int = 0 // Guardamos la categoría que ya tenía
    private var estaCompletada: Boolean = false // Guardamos el estado de completada

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityActualizarTareaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Recuperar el ID enviado desde MainActivity
        tareaId = intent.getIntExtra("TAREA_ID", -1)
        if (tareaId == -1) {
            Toast.makeText(this, "Error al cargar la tarea", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        // Configurar Vista y Cargar Datos
        configurarDropdownPrioridad()
        mostrarDatePicker()
        cargarDatosDeTarea()
        //Configurar la barra de progreso
        configurarBarraProgreso()
        // Configurar Botón de Guardar Cambios
        configurarBotonActualizar()
        // Botón salir (puedes adaptarlo a tus IDs de flechas o botones de salida)
        regresarAlMenuConBoton()
        regresarAlMenuConFlecha()
    }
    private fun configurarDropdownPrioridad() {
        val prioridades = arrayOf("Alta", "Media", "Baja")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, prioridades)
        binding.etPrioridad.setAdapter(adapter)
    }
    private fun mostrarDatePicker() {
        binding.etFecha.setOnClickListener {
            val calendario = Calendar.getInstance()
            calendario.timeInMillis = fechaSeleccionadaMilis

            val año = calendario.get(Calendar.YEAR)
            val mes = calendario.get(Calendar.MONTH)
            val dia = calendario.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, { _, year, month, dayOfMonth ->
                val calSeleccionado = Calendar.getInstance()
                calSeleccionado.set(year, month, dayOfMonth)

                fechaSeleccionadaMilis = calSeleccionado.timeInMillis

                val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                binding.etFecha.setText(formato.format(calSeleccionado.time))
            }, año, mes, dia)

            datePickerDialog.show()
        }
    }

    private fun cargarDatosDeTarea() {
        lifecycleScope.launch {
            // 1. Obtener la tarea y la lista de categorías en paralelo/hilo IO
            val (tarea, listaCategorias) = withContext(Dispatchers.IO) {
                val t = database.tareasDao().obtenerTareaPorId(tareaId)
                val c = database.categoriasDao().obtenerTodasLasCategorias()
                Pair(t, c)
            }
            tarea?.let { t ->
                // Guardamos los estados iniciales
                idCategoriaOriginal = t.categoria_id
                estaCompletada = t.completada
                fechaSeleccionadaMilis = t.fechaLimite
                // 2. Configurar el adaptador de categorías en el Dropdown
                val nombresCategorias = listaCategorias.map { it.nombre }
                val adapterCategorias = ArrayAdapter(this@ActualizarTarea, android.R.layout.simple_dropdown_item_1line, nombresCategorias)
                binding.etCategoria.setAdapter(adapterCategorias)

                // 3. Buscar el nombre de la categoría actual de la tarea para preseleccionarla
                val categoriaActual = listaCategorias.find { it.id == t.categoria_id }
                categoriaActual?.let {
                    binding.etCategoria.setText(it.nombre, false)
                }
                // 4. Escuchar si el usuario cambia la categoría en el formulario
                binding.etCategoria.setOnItemClickListener { _, _, position, _ ->
                    val categoriaSeleccionada = listaCategorias[position]
                    idCategoriaOriginal = categoriaSeleccionada.id // Actualizamos con el nuevo ID
                }
                // 5. Llenar el resto de los campos de texto
                binding.etTitulo.setText(t.titulo)
                binding.etDescripcion.setText(t.descripcion)
                binding.etPrioridad.setText(t.prioridad, false)

                val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                binding.etFecha.setText(formato.format(Date(t.fechaLimite)))

                binding.sliderProgreso.value = t.progreso.toFloat()
                actualizarBarraProgreso(t.progreso)
            }
        }
    }

    private fun configurarBotonActualizar() {
        binding.btnGuardar.setOnClickListener {
            val titulo = binding.etTitulo.text.toString().trim()
            val descripcion = binding.etDescripcion.text.toString().trim()
            val prioridad = binding.etPrioridad.text.toString().trim()

            if (titulo.isEmpty()) {
                Toast.makeText(this, "El título es obligatorio", Toast.LENGTH_SHORT).show()
            }
            else if (prioridad.isEmpty()) {
                Toast.makeText(this, "Selecciona una prioridad", Toast.LENGTH_SHORT).show()
            }
            else {
                val progresoFinal = binding.sliderProgreso.value.toInt()
                val estaCompletada = (progresoFinal == 100)
                // Construimos la entidad manteniendo el mismo ID original para que Room sepa cuál actualizar
                val tareaActualizada = TareaEntity(
                    id = tareaId, // CRUCIAL: Mismo ID para sobreescribir
                    titulo = titulo,
                    descripcion = descripcion,
                    prioridad = prioridad,
                    completada = estaCompletada,
                    fechaLimite = fechaSeleccionadaMilis,
                    categoria_id = idCategoriaOriginal,
                    progreso = progresoFinal
                )

                lifecycleScope.launch {
                    try {
                        database.tareasDao().actualizarTarea(tareaActualizada)
                        Toast.makeText(this@ActualizarTarea, "Tarea actualizada con éxito", Toast.LENGTH_SHORT).show()
                        finish() // Regresa al MainActivity
                    } catch (e: Exception) {
                        Toast.makeText(this@ActualizarTarea, "Error al actualizar: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun configurarBarraProgreso(){
        binding.sliderProgreso.addOnChangeListener { _, value, _ ->
            actualizarBarraProgreso(value.toInt())
        }
    }

    private fun actualizarBarraProgreso(porcentaje: Int){
        binding.tvLabelProgreso.text = "Progreso de la Tarea: $porcentaje%"

        if (porcentaje == 100) {
            binding.tvEstadoCompletado.text = "Estado: Completado"
            binding.tvEstadoCompletado.setTextColor(android.graphics.Color.parseColor("#4CAF50")) // Verde
        } else {
            binding.tvEstadoCompletado.text = "Estado: No completado"
            binding.tvEstadoCompletado.setTextColor(android.graphics.Color.parseColor("#F44336")) // Rojo
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