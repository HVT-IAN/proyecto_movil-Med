package com.meds.app.data.remote

import com.meds.app.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface CimaApi {
    @GET("medicamentos")
    suspend fun search(
        @Query("nombre") name: String,
        @Query("pagina") page: Int = 1
    ): CimaSearchResponse

    @GET("medicamento")
    suspend fun detail(
        @Query("nregistro") registry: String
    ): CimaDetail
}

object CimaClient {
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request()
                .newBuilder()
                .header("User-Agent", "MEDs-Android/${BuildConfig.VERSION_NAME}")
                .build()
            chain.proceed(request)
        }
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://cima.aemps.es/cima/rest/")
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: CimaApi = retrofit.create(CimaApi::class.java)
}
