package com.task08;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class OpenMeteoClient {

    private static final String BASE_URL = "https://api.open-meteo.com/v1/forecast";

    public static String getWeatherForecast(double latitude, double longitude) throws IOException {
        URL url = new URL(BASE_URL + "?latitude=" + latitude + "&longitude=" + longitude + "&hourly=temperature_2m");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new RuntimeException("HttpResponseCode: " + responseCode);
        } else {
            Scanner scanner = new Scanner(url.openStream());
            String response = scanner.useDelimiter("\\A").next();
            scanner.close();
            return response;
        }
    }
}
