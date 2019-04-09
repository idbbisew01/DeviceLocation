package com.bitm.devicelocation;

import android.Manifest;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.bitm.devicelocation.currentweather.CurrentWeatherResponse;
import com.bitm.devicelocation.currentweather.Main;
import com.bitm.devicelocation.databinding.ActivityMainBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private FusedLocationProviderClient client;
    private boolean isLocationPermissionGranted = false;
    private double latitude, longitude;
    private ActivityMainBinding binding;
    private static final String WEATHER_BASE_URL = "http://api.openweathermap.org/data/2.5/";
    private String units = "metric";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main);
        client = LocationServices.getFusedLocationProviderClient(this);
        checkLoationPermission();
    }

    private void checkLoationPermission(){
        if(ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    007);
        }else{
            isLocationPermissionGranted = true;
            getDeviceCurrentLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 007 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            isLocationPermissionGranted = true;
            getDeviceCurrentLocation();
        }else{
            Toast.makeText(this, "Please allow location permission", Toast.LENGTH_SHORT).show();
        }
    }

    private void getDeviceCurrentLocation() {
        if(isLocationPermissionGranted){
            client.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if(location == null){
                        return;
                    }

                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    getCurrentWeatherData();
                    getLocationAddress();

                    binding.latTV.setText(String.valueOf(latitude));
                    binding.lngTV.setText(String.valueOf(longitude));
                }
            });
        }

    }

    private void getCurrentWeatherData() {
        final String apiKey = getString(R.string.weather_api_key);
        final String endUrl = String.format("weather?lat=%f&lon=%f&units=%s&appid=%s",
                latitude, longitude, units, apiKey);
        WeatherApiService apiService = RetrofitClient.getClient(WEATHER_BASE_URL)
                .create(WeatherApiService.class);
        apiService.getCurrentWeatherData(endUrl)
                .enqueue(new Callback<CurrentWeatherResponse>() {
                    @Override
                    public void onResponse(Call<CurrentWeatherResponse> call, Response<CurrentWeatherResponse> response) {
                        if(response.isSuccessful()){
                            CurrentWeatherResponse weatherResponse = response.body();
                            String icon = weatherResponse.getWeather().get(0).getIcon();
                            Picasso.get().load("https://openweathermap.org/img/w/"+icon+".png")
                                    .into(binding.iconIV);
                            Log.e(TAG, "temp: "+weatherResponse.getMain().getTemp());
                        }
                    }

                    @Override
                    public void onFailure(Call<CurrentWeatherResponse> call, Throwable t) {
                        Log.e(TAG, t.getLocalizedMessage() );
                    }
                });
    }

    private void getLocationAddress() {
        final Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);
            String streetAddress = addressList.get(0).getAddressLine(0);
            binding.addressTV.setText(streetAddress);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
