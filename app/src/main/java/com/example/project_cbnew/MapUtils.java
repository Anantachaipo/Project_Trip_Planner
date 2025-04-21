package com.example.project_cbnew;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.project_cbnew.model.Stop;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.*;

public class MapUtils {

    public static List<LatLng> decodePolyline(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            poly.add(new LatLng(lat / 1E5, lng / 1E5));
        }

        return poly;
    }

    // ✅ METHOD ใหม่: วาดเส้นถนนจาก Google Directions API
    public static void drawPolylineWithDirections(Context context, GoogleMap map, List<Stop> stops) {
        if (stops == null || stops.size() < 2) return;

        Stop origin = stops.get(0);
        Stop destination = stops.get(stops.size() - 1);

        StringBuilder url = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
        url.append("origin=").append(origin.getLatitude()).append(",").append(origin.getLongitude());
        url.append("&destination=").append(destination.getLatitude()).append(",").append(destination.getLongitude());

        if (stops.size() > 2) {
            url.append("&waypoints=");
            for (int i = 1; i < stops.size() - 1; i++) {
                url.append(stops.get(i).getLatitude()).append(",").append(stops.get(i).getLongitude());
                if (i < stops.size() - 2) url.append("|");
            }
        }

        url.append("&key=").append(context.getString(R.string.google_maps_key));

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url.toString()).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("MapUtils", "Directions API failed: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) return;

                try {
                    String json = response.body().string();
                    JSONObject root = new JSONObject(json);
                    JSONArray routes = root.optJSONArray("routes");
                    if (routes == null || routes.length() == 0) return;

                    JSONObject overviewPolyline = routes.getJSONObject(0).optJSONObject("overview_polyline");
                    if (overviewPolyline == null) return;

                    String encoded = overviewPolyline.optString("points");
                    List<LatLng> path = decodePolyline(encoded);

                    new Handler(Looper.getMainLooper()).post(() -> {
                        map.clear();
                        map.addPolyline(new PolylineOptions().addAll(path).width(8f).color(0xFF2196F3));
                        for (Stop stop : stops) {
                            LatLng point = new LatLng(stop.getLatitude(), stop.getLongitude());
                            map.addMarker(new MarkerOptions().position(point).title(stop.getName()));
                        }
                    });

                } catch (Exception e) {
                    Log.e("MapUtils", "Parse Directions API response error: " + e.getMessage());
                }
            }
        });
    }

    // ใน MapUtils.java
    public static LatLng getLatLngFromAddress(Context context, String address) {
        try {
            android.location.Geocoder geocoder = new android.location.Geocoder(context, java.util.Locale.getDefault());
            List<android.location.Address> addresses = geocoder.getFromLocationName(address + ", Thailand", 1); // 🔥 Force country hint

            if (addresses != null && !addresses.isEmpty()) {
                android.location.Address location = addresses.get(0);

                // ✅ ตรวจประเทศ
                if (!"TH".equalsIgnoreCase(location.getCountryCode())) {
                    Log.w("MapUtils", "❌ ที่อยู่อยู่นอกประเทศไทย: " + address + " → " + location.getCountryCode());
                    return null;
                }

                return new LatLng(location.getLatitude(), location.getLongitude());
            }
        } catch (Exception e) {
            Log.e("MapUtils", "Geocoding failed for address: " + address, e);
        }
        return null;
    }


    public static void drawPolylineWithLatLng(Context context, GoogleMap map, List<LatLng> points) {
        if (points == null || points.size() < 2) return;

        StringBuilder url = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
        url.append("origin=").append(points.get(0).latitude).append(",").append(points.get(0).longitude);
        url.append("&destination=").append(points.get(points.size() - 1).latitude).append(",").append(points.get(points.size() - 1).longitude);

        if (points.size() > 2) {
            url.append("&waypoints=");
            for (int i = 1; i < points.size() - 1; i++) {
                url.append(points.get(i).latitude).append(",").append(points.get(i).longitude);
                if (i < points.size() - 2) url.append("|");
            }
        }

        url.append("&key=").append(context.getString(R.string.google_maps_key));

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url.toString()).build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {}
            @Override public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) return;

                try {
                    String json = response.body().string();
                    JSONObject root = new JSONObject(json);
                    JSONArray routes = root.optJSONArray("routes");
                    if (routes == null || routes.length() == 0) return;

                    JSONObject overviewPolyline = routes.getJSONObject(0).optJSONObject("overview_polyline");
                    if (overviewPolyline == null) return;

                    String encoded = overviewPolyline.optString("points");
                    List<LatLng> path = decodePolyline(encoded);

                    new Handler(Looper.getMainLooper()).post(() -> {
                        map.addPolyline(new PolylineOptions().addAll(path).width(8f).color(Color.BLUE));
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


}
