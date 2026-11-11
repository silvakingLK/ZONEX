package com.example.zonex.network

import com.google.gson.annotations.SerializedName

data class GeoPointDto(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double
)

data class HexagonDto(
    @SerializedName("hex_id") val hexId: String,
    @SerializedName("status") val status: String,
    @SerializedName("dono_id") val donoId: Long?,
    @SerializedName("pontos_defesa") val pontosDefesa: Int,
    @SerializedName("dominado_em") val dominadoEm: String?,
    @SerializedName("poligono") val poligono: List<GeoPointDto>
)

data class VisibleTerritoriesResponse(
    @SerializedName("resolucao") val resolucao: Int,
    @SerializedName("total") val total: Int,
    @SerializedName("dominados") val dominados: Int,
    @SerializedName("hexagonos") val hexagonos: List<HexagonDto>
)

data class ConquerRequest(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("atividade_id") val atividadeId: Long
)

data class ConquerResponse(
    @SerializedName("hex_id") val hexId: String,
    @SerializedName("status") val status: String,
    @SerializedName("dono_id") val donoId: Long?,
    @SerializedName("dono_anterior_id") val donoAnteriorId: Long?,
    @SerializedName("pontos_defesa") val pontosDefesa: Int,
    @SerializedName("dominado_em") val dominadoEm: String?,
    @SerializedName("poligono") val poligono: List<GeoPointDto>
)
