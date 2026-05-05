package com.ankispirelink.anki;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class HttpAnkiConnectTransport implements AnkiConnectTransport {
    public static final String DEFAULT_ENDPOINT = "http://127.0.0.1:8765";

    private final String endpoint;
    private final int timeoutMillis;

    public HttpAnkiConnectTransport() {
        this(DEFAULT_ENDPOINT, 3000);
    }

    public HttpAnkiConnectTransport(String endpoint, int timeoutMillis) {
        this.endpoint = endpoint == null || endpoint.trim().isEmpty() ? DEFAULT_ENDPOINT : endpoint.trim();
        this.timeoutMillis = Math.max(1, timeoutMillis);
    }

    @Override
    public String post(String requestJson) throws IOException {
        byte[] body = requestJson.getBytes(StandardCharsets.UTF_8);
        HttpURLConnection connection = (HttpURLConnection) new URL(endpoint).openConnection();
        connection.setConnectTimeout(timeoutMillis);
        connection.setReadTimeout(timeoutMillis);
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        connection.setRequestProperty("Accept", "application/json");
        try {
            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(body);
            }
            InputStream inputStream = connection.getResponseCode() >= 400
                    ? connection.getErrorStream()
                    : connection.getInputStream();
            return readAll(inputStream);
        } finally {
            connection.disconnect();
        }
    }

    private static String readAll(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }
        return builder.toString();
    }
}
