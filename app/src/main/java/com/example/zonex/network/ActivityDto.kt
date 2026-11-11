package com.example.zonex.network

data class ActivityDto(
    val id: Long,
    val tipo: String, // ex: "Corrida" ou "Caminhada"
    val distanciaMetros: Float,
    val duracaoSegundos: Int,
    val data: String, // ex: "2023-10-27"
    val zonasConquistadas: Int
)