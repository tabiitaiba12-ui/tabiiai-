package com.tabi.ai.data.remote.weather;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApiService {

    @GET("data/2.5/weather")
    Call<WeatherResponse> getCurrentWeatherByCity(
            @Query("q") String cityName,
            @Query("appid") String apiKey,
            @Query("units") String units
    );

    @GET("data/2.5/weather")
    Call<WeatherResponse> getCurrentWeatherByCoordinates(
            @Query("lat") double latitude,
            @Query("lon") double longitude,
            @Query("appid") String apiKey,
            @Query("units") String units
    );
}
