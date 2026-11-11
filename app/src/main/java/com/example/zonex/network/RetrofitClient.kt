package com.example.zonex.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // 10.0.2.2 mapeia para o localhost da máquina hospedeira no emulador padrão do Android.
    // Em dispositivo físico via Wi-Fi, substitua pelo IP local da sua máquina (ex: http://192.168.1.100:8080/).
    private const val BASE_URL = "https://zonex-backend-8f9k.onrender.com/"

    val apiService: ZoneXApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ZoneXApiService::class.java)
    }
}
