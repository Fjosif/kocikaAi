package cz.kocika.game.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Replace with your Render/Railway URL after deployment (e.g., https://kocika-api.onrender.com/)
    private const val BASE_URL = "http://10.0.2.2:5000/" 

    val service: CatApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CatApiService::class.java)
    }
}
