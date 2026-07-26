package com.tabi.ai.data.remote.weather;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Response body for OpenWeatherMap's "current weather" endpoint
 * (https://api.openweathermap.org/data/2.5/weather).
 */
public class WeatherResponse {

    @SerializedName("name")
    private String cityName;

    @SerializedName("main")
    private Main main;

    @SerializedName("weather")
    private List<WeatherInfo> weather;

    @SerializedName("wind")
    private Wind wind;

    @SerializedName("cod")
    private int cod;

    @SerializedName("message")
    private String message;

    public String getCityName() {
        return cityName;
    }

    public Main getMain() {
        return main;
    }

    public List<WeatherInfo> getWeather() {
        return weather;
    }

    public Wind getWind() {
        return wind;
    }

    public int getCod() {
        return cod;
    }

    public String getMessage() {
        return message;
    }

    public static class Main {
        @SerializedName("temp")
        private double temp;

        @SerializedName("feels_like")
        private double feelsLike;

        @SerializedName("humidity")
        private int humidity;

        public double getTemp() {
            return temp;
        }

        public double getFeelsLike() {
            return feelsLike;
        }

        public int getHumidity() {
            return humidity;
        }
    }

    public static class WeatherInfo {
        @SerializedName("main")
        private String main;

        @SerializedName("description")
        private String description;

        public String getMain() {
            return main;
        }

        public String getDescription() {
            return description;
        }
    }

    public static class Wind {
        @SerializedName("speed")
        private double speed;

        public double getSpeed() {
            return speed;
        }
    }
}
