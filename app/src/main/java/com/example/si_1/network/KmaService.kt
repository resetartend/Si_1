// KmaService.kt
package com.example.si_1.network

import retrofit2.http.GET
import retrofit2.http.Query
import com.example.si_1.network.WeatherResponse

interface KmaService {
    @GET("getVilageFcst")
    suspend fun getForecast(
        @Query("serviceKey") serviceKey: String,
        @Query("numOfRows") numOfRows: Int,
        @Query("pageNo") pageNo: Int,
        @Query("dataType") dataType: String = "XML",
        @Query("base_date") baseDate: String,
        @Query("base_time") baseTime: String,
        @Query("nx") nx: Int,
        @Query("ny") ny: Int
    ): WeatherResponse
}
