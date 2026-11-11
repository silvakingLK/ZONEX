package com.example.zonex.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface ZoneXApiService {

    @GET("api/territorios/visiveis")
    suspend fun getVisibleTerritories(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("raio") raio: Double
    ): Response<VisibleTerritoriesResponse>

    @GET("api/territorios/visiveis")
    suspend fun getVisibleTerritoriesByBounds(
        @Query("min_lat") minLat: Double,
        @Query("min_lng") minLng: Double,
        @Query("max_lat") maxLat: Double,
        @Query("max_lng") maxLng: Double
    ): Response<VisibleTerritoriesResponse>

    @POST("api/territorios/conquistar")
    suspend fun conquerTerritory(
        @Header("X-Usuario-ID") userId: Long = 1,
        @Body request: ConquerRequest
    ): Response<ConquerResponse>
    
    @POST("api/territorios/conquistar-poligono")
    suspend fun conquerPolygon(
        @Header("X-Usuario-ID") userId: Long = 1,
        @Body request: ConquerPolygonRequest
    ): Response<ConquerPolygonResponse>

    @GET("api/atividades")
    suspend fun getActivities(
        @Header("X-Usuario-ID") userId: Long = 1
    ): Response<List<ActivityDto>>
}
