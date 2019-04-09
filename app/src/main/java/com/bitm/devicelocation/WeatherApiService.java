package com.bitm.devicelocation;

import com.bitm.devicelocation.currentweather.CurrentWeatherResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface WeatherApiService {

    @GET
    Call<CurrentWeatherResponse> getCurrentWeatherData(@Url String endUrl);
}
