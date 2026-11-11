package com.example.zonex.network

data class PointDto(val latitude: Double, val longitude: Double)

data class ConquerPolygonRequest(
    val atividadeId: Long,
    val pontos: List<PointDto>,
    val distanciaMetros: Float
)

data class ConquerPolygonResponse(
    val sucesso: Boolean,
    val mensagem: String
)