package com.tabi.ai.data.repository;

import android.content.Context;

import com.tabi.ai.data.remote.RetrofitClient;
import com.tabi.ai.data.remote.weather.WeatherApiService;
import com.tabi.ai.data.remote.weather.WeatherResponse;
import com.tabi.ai.utils.PreferenceHelper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Fetches current weather conditions from OpenWeatherMap.
 */
public class WeatherRepository {

    public interface WeatherCallback {
        void onSuccess(String humanReadableSummary);
        void onError(String errorMessage);
    }

    private final WeatherApiService weatherApiService;
    private final Context appContext;

    public WeatherRepository(Context context) {
        this.appContext = context.getApplicationContext();
        this.weatherApiService = RetrofitClient.getWeatherService();
    }

    public void getWeatherByCoordinates(double lat, double lon, WeatherCallback callback) {
        String apiKey = resolveApiKey();
        if (apiKey.isEmpty()) {
            callback.onError("Missing OpenWeatherMap API key. Please add it in Settings.");
            return;
        }

        weatherApiService.getCurrentWeatherByCoordinates(lat, lon, apiKey, "metric")
                .enqueue(new Callback<WeatherResponse>() {
                    @Override
                    public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                        handleResponse(response, callback);
                    }

                    @Override
                    public void onFailure(Call<WeatherResponse> call, Throwable t) {
                        callback.onError("Network error: " + t.getMessage());
                    }
                });
    }

    public void getWeatherByCity(String city, WeatherCallback callback) {
        String apiKey = resolveApiKey();
        if (apiKey.isEmpty()) {
            callback.onError("Missing OpenWeatherMap API key. Please add it in Settings.");
            return;
        }

        weatherApiService.getCurrentWeatherByCity(city, apiKey, "metric")
                .enqueue(new Callback<WeatherResponse>() {
                    @Override
                    public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                        handleResponse(response, callback);
                    }

                    @Override
                    public void onFailure(Call<WeatherResponse> call, Throwable t) {
                        callback.onError("Network error: " + t.getMessage());
                    }
                });
    }

    private void handleResponse(Response<WeatherResponse> response, WeatherCallback callback) {
        if (response.isSuccessful() && response.body() != null) {
            WeatherResponse body = response.body();
            if (body.getCod() != 200 && body.getCod() != 0) {
                callback.onError(body.getMessage() != null ? body.getMessage() : "Weather lookup failed");
                return;
            }
            String description = (body.getWeather() != null && !body.getWeather().isEmpty())
                    ? body.getWeather().get(0).getDescription()
                    : "clear conditions";

            String summary = String.format(
                    "It's currently %.0f°C with %s in %s. Feels like %.0f°C.",
                    body.getMain().getTemp(),
                    description,
                    body.getCityName(),
                    body.getMain().getFeelsLike());

            callback.onSuccess(summary);
        } else {
            callback.onError("Weather request failed (HTTP " + response.code() + ")");
        }
    }

    private String resolveApiKey() {
        String userKey = PreferenceHelper.getOpenWeatherKey(appContext);
        if (userKey != null && !userKey.trim().isEmpty()) {
            return userKey.trim();
        }
        String buildConfigKey = com.tabi.ai.BuildConfig.OPENWEATHER_API_KEY;
        return buildConfigKey == null ? "" : buildConfigKey.trim();
    }
}
