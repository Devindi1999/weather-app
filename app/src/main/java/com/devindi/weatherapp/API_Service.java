package com.devindi.weatherapp;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
public interface API_Service {
    @GET("weather")
    Call<API_Response>
    getCurrentWeather(@Query("lat") double lat,
                      @Query("lon") double lon,
                      @Query("appid") String appid,
                      @Query("units") String units);
}
