package com.example.taskflow

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ClimaApi {
    @GET("weather")
    fun obtenerElTiempoActual(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String
    ): Call<ClimaModelo>
}