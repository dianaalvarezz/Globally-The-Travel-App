package edu.niu.android.globally;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GooglePhotosHelper {

    public interface MediaCallback {
        void onMediaFetched(String json);
        void onError(Exception e);
    }

    public static void fetchAllPhotos(String accessToken, MediaCallback callback) {
        new Thread(() -> {
            try {
                URL url = new URL("https://photoslibrary.googleapis.com/v1/mediaItems?pageSize=100");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Authorization", "Bearer " + accessToken);
                conn.setRequestMethod("GET");

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    callback.onMediaFetched(response.toString());
                } else {
                    callback.onError(new Exception("HTTP error code: " + responseCode));
                }
            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }
}
