package com.devindi.weatherapp;
import java.util.List;
public class API_Response {
    public List<Weather> weather;
    public Main main;

    public class Weather {
        public String main;
        public String description;
    }

    public class Main {
        public double temp;
        public int humidity;
    }
}
