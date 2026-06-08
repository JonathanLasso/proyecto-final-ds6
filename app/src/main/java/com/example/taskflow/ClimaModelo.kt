package com.example.taskflow

data class ClimaModelo(
    val nombre: String,
    val principal: InformacionPrincipal,
    val clima: List<ClimaDescripcion>
)

data class InformacionPrincipal(
    val temperatura: Double,
    val humedad: Int
)

data class ClimaDescripcion(
    val descripcion: String,
    val icono: String
)
