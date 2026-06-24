package com.example.taskflow

import android.content.Intent
import android.os.Bundle
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

    private var tareasJob: kotlinx.coroutines.Job? = null
    // 🌟 NUEVO: Job para controlar la corrutina de categorías de forma limpia
    private var categoriasJob: kotlinx.coroutines.Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        obtenerDatosDelClima()
        configurarLista()

        // 🌟 NUEVO: Primero empezamos a escuchar las categorías para que el adaptador
        // esté listo antes de que carguen las tareas.
        observarCategorias()
        obtenerTareas()

        pantallaDeAgregarTarea()
        configurarFiltros()
        configurarMenu()
    }

    private fun configurarMenu(){
        binding.btnMenu.setOnClickListener { view ->
            val popup = androidx.appcompat.widget.PopupMenu(this,view)
            popup.menuInflater.inflate(R.menu.menus_pantallas, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId){
                    R.id.menu_estadistica ->{
                        val intent = Intent(this, EstadisticasActivity::class.java)
                        startActivity(intent)
                        true
                    }
                    R.id.menu_categorias ->{
                        val intent = Intent(this, CategoriaActivity::class.java)
                        startActivity(intent)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    private fun configurarFiltros() {
        binding.btnFiltros.setOnClickListener { view ->
            val popup = androidx.appcompat.widget.PopupMenu(this, view)
            popup.menuInflater.inflate(R.menu.filtros, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                val dao = database.tareasDao()
                when (item.itemId) {
                    R.id.filtro_todos -> {
                        obtenerTareas(dao.obtenerTodasLasTareas())
                        Toast.makeText(this, "Mostrando todas las tareas", Toast.LENGTH_SHORT).show()
                        true
                    }
                    // Nota: Si el usuario crea categorías personalizadas, tus filtros fijos del menú
                    // seguirán funcionando para las 4 por defecto.
                    R.id.filtro_personal -> {
                        obtenerTareas(dao.obtenerTareasPersonal())
                        Toast.makeText(this, "Filtrado: Personal", Toast.LENGTH_SHORT).show()
                        true
                    }

                    R.id.filtro_trabajo -> {
                        obtenerTareas(dao.obtenerTareasTrabajo())
                        Toast.makeText(this, "Filtrado: Trabajo", Toast.LENGTH_SHORT).show()
                        true
                    }

                    R.id.filtro_estudio -> {
                        obtenerTareas(dao.obtenerTareasEstudios())
                        Toast.makeText(this, "Filtrado: Estudio", Toast.LENGTH_SHORT).show()
                        true
                    }

                    R.id.filtro_compras -> {
                        obtenerTareas(dao.obtenerTareasCompras())
                        Toast.makeText(this, "Filtrado: Compras", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.filtro_fecha -> {
                        obtenerTareas(dao.obtenerTareasPorFecha())
                        Toast.makeText(this, "Ordenado por fecha de vencimiento", Toast.LENGTH_SHORT).show()
                        true
                    }

                    R.id.filtro_tareas_pendientes -> {
                        obtenerTareas(dao.obtenerTareasPendientes())
                        Toast.makeText(this, "Filtrado: Tareas Pendientes", Toast.LENGTH_SHORT).show()
                        true
                    }

                    R.id.filtro_tareas_completadas -> {
                        obtenerTareas(dao.obtenerTareasCompletadas())
                        Toast.makeText(this, "Filtrado: Tareas Completadas", Toast.LENGTH_SHORT).show()
                        true
                    }

                    R.id.filtro_prioridad -> {
                        obtenerTareas(dao.obtenerTareasPorPrioridad())
                        Toast.makeText(this, "Ordenado por prioridad", Toast.LENGTH_SHORT).show()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    private fun configurarLista() {
        tareasAdapter = TareasAdapter(
            listaTareas = emptyList(),
            listaCategorias = emptyList(), // 🌟 NUEVO: Mandamos una lista vacía inicial
            onTareaClick = { tarea ->
                val intent = Intent(this, ActualizarTareaActivity::class.java).apply {
                    putExtra("TAREA_ID", tarea.id)
                }
                startActivity(intent)
            },
            onEliminarClick = { tarea ->
                mostrarDialogoConfirmacion(tarea)
            }
        )

        binding.rvTareas.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = tareasAdapter
        }
    }

    // 🌟 NUEVO MÉTODO: Escucha de forma reactiva la tabla de categorías
    private fun observarCategorias() {
        categoriasJob?.cancel()
        categoriasJob = lifecycleScope.launch {
            // Asegúrate de que el método en tu dao se llame así o cámbialo por el tuyo
            database.categoriasDao().obtenerTodasLasCategorias().collectLatest { listaDeCategorias ->
                // Pasamos las nuevas categorías creadas al adaptador
                tareasAdapter.actualizarCategorias(listaDeCategorias)
            }
        }
    }

    private fun obtenerTareas(
        flujoQuery: kotlinx.coroutines.flow.Flow<List<TareaEntity>> = database.tareasDao().obtenerTodasLasTareas()
    ) {
        tareasJob?.cancel()

        tareasJob = lifecycleScope.launch {
            flujoQuery.collectLatest { listaDeTareas ->
                // Llamamos a actualizar lista pasándole los datos de Room
                tareasAdapter.actualizarLista(listaDeTareas)

                if (listaDeTareas.isEmpty()) {
                    binding.rvTareas.visibility = android.view.View.GONE
                    binding.tvSinTareas.visibility = android.view.View.VISIBLE
                } else {
                    binding.rvTareas.visibility = android.view.View.VISIBLE
                    binding.tvSinTareas.visibility = android.view.View.GONE
                }
            }
        }
    }

    private fun mostrarDialogoConfirmacion(tarea: TareaEntity) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("¿Eliminar tarea?")
        builder.setMessage("¿Estás seguro de que deseas eliminar la tarea \"${tarea.titulo}\"? Esta acción no se puede deshacer.")

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

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun pantallaDeAgregarTarea() {
        val btnAgregarTarea = binding.AgregarTarea
        btnAgregarTarea.setOnClickListener {
            val intent = Intent(this, CrearTareaActivity::class.java)
            startActivity(intent)
        }
    }

    private fun obtenerDatosDelClima() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ClimaApi::class.java)
        val call = apiService.obtenerElTiempoActual(
            "Ciudad de Panamá",
            "588a8259c8ffb569bdd98d4db4d50e3c",
            "metric"
        )

        call.enqueue(object : retrofit2.Callback<ClimaModelo> {
            override fun onResponse(call: Call<ClimaModelo>, response: Response<ClimaModelo>) {
                if (response.isSuccessful) {
                    val weatherData = response.body()
                    weatherData?.let { weather ->
                        binding.tvCityName.text = weather.nombre
                        binding.tvTemperature.text = "${weather.principal.temperatura} °C"
                        binding.tvHumidity.text = "Humedad: ${weather.principal.humedad}%"
                        val codigoIcono = weather.clima.firstOrNull()?.icono
                        if (codigoIcono != null) {
                            val urlIcono = "https://openweathermap.org/img/wn/$codigoIcono@2x.png"
                            Glide.with(this@MainActivity).load(urlIcono).into(binding.ivWeatherIcon)
                        }
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Error en la respuesta del servidor", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ClimaModelo>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Fallo de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}