package com.example.taskflow

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.taskflow.adapter.TareasAdapter
import com.example.taskflow.api.ClimaApi
import com.example.taskflow.api.ClimaModelo
import com.example.taskflow.dataBase.app.TareaApp
import com.example.taskflow.dataBase.tablas.TareaEntity
import com.example.taskflow.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val database by lazy { (application as TareaApp).database }
    private lateinit var tareasAdapter: TareasAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        obtenerDatosDelClima()
        configurarLista()
        obtenerTareas()
        pantallaDeAgregarTarea()
        configurarMenu()
    }

    private fun configurarMenu() {
        binding.btnMenu.setOnClickListener { view ->
            val popup = androidx.appcompat.widget.PopupMenu(this, view)
            popup.menuInflater.inflate(R.menu.main_menu, popup.menu)

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_categorias -> {
                        // En lugar de ir directo a otra Activity, abrimos las opciones de ordenamiento/filtro
                        mostrarOpcionesDelFiltro(view)
                        true
                    }
                    R.id.menu_estadisticas -> {
                        Toast.makeText(this, "Próximamente: Estadísticas", Toast.LENGTH_SHORT).show()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    /**
     * Despliega un segundo menú flotante con los criterios específicos de ordenamiento
     */
    private fun mostrarOpcionesDelFiltro(anchorView: View) {
        val filtroPopup = androidx.appcompat.widget.PopupMenu(this, anchorView)

        // Añadimos las opciones dinámicamente al menú
        filtroPopup.menu.add(0, 1, 0, "Por Fecha de Creación")
        filtroPopup.menu.add(0, 2, 1, "Por Prioridad")
        filtroPopup.menu.add(0, 3, 2, "Ver solo Completadas")
        filtroPopup.menu.add(0, 4, 3, "Ver solo Pendientes")

        filtroPopup.setOnMenuItemClickListener { filtroItem ->
            when (filtroItem.itemId) {
                1 -> {
                    // TODO: Lógica para ordenar por fecha (ej. viewModel.ordenarPorFecha())
                    Toast.makeText(this, "Ordenado por fecha", Toast.LENGTH_SHORT).show()
                    true
                }
                2 -> {
                    // TODO: Lógica para ordenar por prioridad
                    Toast.makeText(this, "Ordenado por prioridad", Toast.LENGTH_SHORT).show()
                    true
                }
                3 -> {
                    // TODO: Filtrar completadas
                    Toast.makeText(this, "Filtrado: Completadas", Toast.LENGTH_SHORT).show()
                    true
                }
                4 -> {
                    // TODO: Filtrar pendientes
                    Toast.makeText(this, "Filtrado: Pendientes", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
        filtroPopup.show()
    }

    private fun configurarLista() {
        // Inicializamos el adaptador pasando las acciones (lambdas)
        tareasAdapter = TareasAdapter(
            listaTareas = emptyList(),
            onTareaClick = { tarea ->
                // Acción 1: Ir a la pantalla de actualizar enviando el ID o el objeto completo
                val intent = Intent(this, ActualizarTarea::class.java).apply {
                    putExtra("TAREA_ID", tarea.id) // O puedes pasar más datos si tu Entidad es Serializable/Parcelable
                }
                startActivity(intent)
            },
            onEliminarClick = { tarea ->
                // Acción 2: Mostrar mensaje de confirmación para eliminar
                mostrarDialogoConfirmacion(tarea)
            }
        )

        binding.rvTareas.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = tareasAdapter
        }
    }

    private fun obtenerTareas() {
        // Usamos lifecycleScope ya que obtenerTodasLasTareas() devuelve un Flow reactivo
        lifecycleScope.launch {
            database.tareasDao().obtenerTodasLasTareas().collectLatest { listaDeTareas ->
                // Cada vez que la tabla cambie (inserciones, eliminaciones) este bloque se ejecuta solo
                tareasAdapter.actualizarLista(listaDeTareas)
            }
        }
    }

    private fun mostrarDialogoConfirmacion(tarea: TareaEntity) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("¿Eliminar tarea?")
        builder.setMessage("¿Estás seguro de que deseas eliminar la tarea \"${tarea.titulo}\"? Esta acción no se puede deshacer.")

        // Si dice que sí, borramos de la base de datos usando Corrutinas
        builder.setPositiveButton("Eliminar") { dialog, _ ->
            lifecycleScope.launch {
                try {
                    database.tareasDao().eliminarTarea(tarea)
                    Toast.makeText(this@MainActivity, "Tarea eliminada", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, "Error al eliminar: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            dialog.dismiss()
        }

        // Si dice que no, simplemente cerramos el mensaje
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun pantallaDeAgregarTarea() {
        val btnAgregarTarea = binding.AgregarTarea
        btnAgregarTarea.setOnClickListener {
            val intent = Intent(
                this,
                CrearTarea::class.java
            )
            startActivity(intent)
        }
    }

    private fun obtenerDatosDelClima() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ClimaApi::class.java)
        val call = apiService.obtenerElTiempoActual("Ciudad de Panamá", "588a8259c8ffb569bdd98d4db4d50e3c", "metric")

        call.enqueue(object : retrofit2.Callback<ClimaModelo> {
            override fun onResponse(call: Call<ClimaModelo>, response: Response<ClimaModelo>) {
                if (response.isSuccessful) {
                    val weatherData = response.body()

                    // 4. Cambiar el texto usando ViewBinding de forma segura
                    weatherData?.let { weather ->
                        binding.tvCityName.text = weather.nombre

                        binding.tvTemperature.text = "${weather.principal.temperatura} °C"
                        binding.tvHumidity.text = "Humedad: ${weather.principal.humedad}%"

                        val codigoIcono = weather.clima.firstOrNull()?.icono

                        if (codigoIcono != null) {
                            val urlIcono = "https://openweathermap.org/img/wn/$codigoIcono@2x.png"

                            Glide.with(this@MainActivity)
                                .load(urlIcono)
                                .into(binding.ivWeatherIcon) // <- El ID de tu ImageView
                        }
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Error en la respuesta del servidor", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ClimaModelo>, t: Throwable) {
                // Manejar el error de conexión
                Toast.makeText(this@MainActivity, "Fallo de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}