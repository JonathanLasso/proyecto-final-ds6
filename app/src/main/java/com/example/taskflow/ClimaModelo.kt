package com.example.taskflow

import com.google.gson.annotations.SerializedName

data class ClimaModelo(
    @SerializedName("name") val nombre: String,
    @SerializedName("main") val principal: InformacionPrincipal,
    @SerializedName("weather") val clima: List<ClimaDescripcion>
)

data class InformacionPrincipal(
    @SerializedName("temp") val temperatura: Double,
    @SerializedName("humidity") val humedad: Int
)

data class ClimaDescripcion(
    @SerializedName("description") val descripcion: String,
    @SerializedName("icon") val icono: String
)