package com.example.taskflow

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.taskflow.databinding.ActivityMainBinding
import android.widget.Toast
import com.bumptech.glide.Glide
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        obtenerDatosDelClima()
    }

    private fun obtenerDatosDelClima() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/3.0/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ClimaApi::class.java)
        val call = apiService.obtenerElTiempoActual("Panama City", "588a8259c8ffb569bdd98d4db4d50e3c")

        call.enqueue(object : retrofit2.Callback<ClimaModelo> {
            override fun onResponse(call: Call<ClimaModelo>, response: Response<ClimaModelo>) {
                if (response.isSuccessful) {
                    val weatherData = response.body()

                    // 4. Cambiar el texto usando ViewBinding de forma segura
                    weatherData?.let { weather ->
                        binding.tvCityName.text = weather.nombre
                        // OpenWeather devuelve Kelvin por defecto; puedes formatearlo si lo deseas
                        binding.tvTemperature.text = "${weather.principal.temperatura} °K"
                        binding.tvHumidity.text = "Humedad: ${weather.principal.humedad}%"
                        // 2. Extraer el código del icono (ej: "10d") de forma segura
                        val codigoIcono = weather.clima.firstOrNull()?.icono

                        // 3. Si el código existe, construimos la URL y dejamos que Glide haga la magia
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