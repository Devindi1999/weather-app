package com.devindi.weatherapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;
    private TextView latitudeLongitudeTextView;
    private TextView geoAddressTextView;
    private TextView systemTimeTextView;
    private TextView weatherMainTextView;
    private TextView weatherDescriptionTextView;
    private TextView temperatureTextView;
    private TextView humidityTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        latitudeLongitudeTextView = findViewById(R.id.latitude_longitude);
        geoAddressTextView = findViewById(R.id.geo_address);
        systemTimeTextView = findViewById(R.id.system_time);

        weatherMainTextView = findViewById(R.id.weather_main);
        weatherDescriptionTextView = findViewById(R.id.weather_description);
        temperatureTextView = findViewById(R.id.temperature);
        humidityTextView = findViewById(R.id.humidity);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        } else {
            getLastLocation();
        }

        displayCurrentTime();
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            latitudeLongitudeTextView.setText(String.format(Locale.getDefault(), "Latitude: %.4f, Longitude: %.4f", latitude, longitude));
                            getAddressFromLocation(latitude, longitude);
                            fetchWeatherData(latitude, longitude);
                        }
                    }
                });
    }

    private void getAddressFromLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                geoAddressTextView.setText(address.getAddressLine(0));
            } else {
                geoAddressTextView.setText("Address Cannot be Find");
            }
        } catch (IOException e) {
            e.printStackTrace();
            geoAddressTextView.setText("Geocoder service not available");
        }
    }

    private void fetchWeatherData(double latitude, double longitude) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/data/2.5/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        API_Service service = retrofit.create(API_Service.class);
        Call<API_Response> call = service.getCurrentWeather(latitude, longitude, "253c91b141c98374a6cd216fe572149f", "metric");

        call.enqueue(new Callback<API_Response>() {
            @Override
            public void onResponse(Call<API_Response> call, Response<API_Response> response) {
                if (response.isSuccessful() && response.body() != null) {
                    API_Response apiResponse = response.body();

                    if (apiResponse.weather != null && !apiResponse.weather.isEmpty()) {
                        API_Response.Weather weather = apiResponse.weather.get(0);
                        weatherMainTextView.setText(weather.main);
                        weatherDescriptionTextView.setText(weather.description);
                    }

                    weatherMainTextView.setText(apiResponse.weather.get(0).main);
                    weatherDescriptionTextView.setText(apiResponse.weather.get(0).description);
                    temperatureTextView.setText(String.format(Locale.getDefault(), "%.2f°C", apiResponse.main.temp));
                    humidityTextView.setText(String.format(Locale.getDefault(), "%d%%", apiResponse.main.humidity));
                }
            }

            @Override
            public void onFailure(Call<API_Response> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void displayCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String currentTime = sdf.format(new Date());
        systemTimeTextView.setText(currentTime);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }
}