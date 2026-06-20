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
    }

    private fun configurarLista() {
        tareasAdapter = TareasAdapter()
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