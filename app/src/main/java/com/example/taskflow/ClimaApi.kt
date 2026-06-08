package com.example.taskflow

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ClimaApi {
    @GET("data/3.0/wather")
    fun obtenerElTiempoActual(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): Call<ClimaModelo>
}