package com.tabi.ai.data.remote;

import com.tabi.ai.data.remote.weather.WeatherApiService;
import com.tabi.ai.utils.Constants;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Builds and caches the Retrofit + OkHttp clients used across the app.
 * Two Retrofit instances are exposed: one for OpenAI, one for OpenWeatherMap.
 */
public final class RetrofitClient {

    private static volatile OpenAiApiService openAiApiService;
    private static volatile WeatherApiService weatherApiService;

    private RetrofitClient() {
    }

    private static OkHttpClient buildHttpClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

        return new OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public static OpenAiApiService getOpenAiService() {
        if (openAiApiService == null) {
            synchronized (RetrofitClient.class) {
                if (openAiApiService == null) {
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(Constants.OPENAI_BASE_URL)
                            .client(buildHttpClient())
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                    openAiApiService = retrofit.create(OpenAiApiService.class);
                }
            }
        }
        return openAiApiService;
    }

    public static WeatherApiService getWeatherService() {
        if (weatherApiService == null) {
            synchronized (RetrofitClient.class) {
                if (weatherApiService == null) {
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(Constants.OPENWEATHER_BASE_URL)
                            .client(buildHttpClient())
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                    weatherApiService = retrofit.create(WeatherApiService.class);
                }
            }
        }
        return weatherApiService;
    }
}
