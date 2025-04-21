package com.example.project_cbnew;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.project_cbnew.adapter.StopAdapter;
import com.example.project_cbnew.model.Stop;
import com.example.project_cbnew.model.TripDay;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.Collections;

public class Trip_Day_Fragment extends Fragment {

    private static final String ARG_DAY_NUMBER = "day_number";
    private int dayNumber;

    private CreateTripViewModel viewModel;
    private StopAdapter stopAdapter;
    private GoogleMap sharedMap;
    private TripDay tripDay;
    private TextView tvTotalDistance, tvTotalRestTime, tvTotalTravelTime;
    private boolean isMapPending = false;
    private boolean hasResumed = false;

    private List<Stop> originalStopsOrder = null;

    public static Trip_Day_Fragment newInstance(int dayNumber) {
        Trip_Day_Fragment fragment = new Trip_Day_Fragment();
        Bundle args = new Bundle();
        args.putInt(ARG_DAY_NUMBER, dayNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public int getDayNumber() {
        return dayNumber;
    }

    @Override
    public void onResume() {
        super.onResume();
        hasResumed = true;

        if (sharedMap != null) {
            drawDayRoute();
        } else {
            isMapPending = true;
        }
    }

    public void setSharedMap(GoogleMap map) {
        this.sharedMap = map;
        if (hasResumed && isMapPending && sharedMap != null) {
            isMapPending = false;
            drawDayRoute();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            dayNumber = getArguments().getInt(ARG_DAY_NUMBER);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (sharedMap != null) {
            drawDayRoute();
        } else {
            isMapPending = true;
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trip_day, container, false);

        Places.initialize(requireContext(), getString(R.string.google_maps_key));
        PlacesClient placesClient = Places.createClient(requireContext());

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        if (autocompleteFragment != null) {
            autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG , Place.Field.PHOTO_METADATAS));
            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    LatLng latLng = place.getLatLng();
                    if (latLng != null) {
                        // ถ้ามีรูปภาพ
                        List<PhotoMetadata> photoMetadatas = place.getPhotoMetadatas();
                        Log.d("PhotoDebug", "place=" + place.getName() + ", photos=" + (photoMetadatas != null ? photoMetadatas.size() : "null"));
                        String photoUrl = null;
                        if (photoMetadatas != null && !photoMetadatas.isEmpty()) {
                            // ส่ง metadata ไปโหลดรูป
                            PhotoMetadata photoMetadata = photoMetadatas.get(0);

                            FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                                    .setMaxWidth(500)
                                    .setMaxHeight(500)
                                    .build();

                            PlacesClient placesClient = Places.createClient(requireContext());
                            placesClient.fetchPhoto(photoRequest)
                                    .addOnSuccessListener(fetchPhotoResponse -> {
                                        Bitmap bitmap = fetchPhotoResponse.getBitmap();
                                        Log.d("PhotoLoad", "โหลดรูป bitmap สำเร็จ: " + (bitmap != null));
                                        showStopDetailDialog(place.getName(), latLng.latitude, latLng.longitude, bitmap); // ✅ ถูกแล้ว
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("PhotoLoad", "❌ โหลดรูปไม่สำเร็จ: " + e.getMessage());
                                        showStopDetailDialog(place.getName(), latLng.latitude, latLng.longitude, null); // fallback
                                    });

                        } else {
                            showStopDetailDialog(place.getName(), latLng.latitude, latLng.longitude, null);
                        }
                        autocompleteFragment.setText("");
                    }
                }

                @Override
                public void onError(@NonNull Status status) {
                    Toast.makeText(getContext(), "ไม่สามารถค้นหาได้: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        tvTotalDistance = view.findViewById(R.id.tvDayTotalDistance);
        tvTotalRestTime = view.findViewById(R.id.tvTotalRestTime);
        tvTotalTravelTime = view.findViewById(R.id.tvTotalTravelTime);

        viewModel = new ViewModelProvider(requireActivity()).get(CreateTripViewModel.class);
        tripDay = viewModel.getTripDay(dayNumber - 1);
        if (tripDay == null) {
            tripDay = new TripDay("วัน " + dayNumber);
            viewModel.updateDay(dayNumber - 1, tripDay);
        }

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewDayStops);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        stopAdapter = new StopAdapter(new ArrayList<>(tripDay.getStops()), new StopAdapter.OnStopClickListener() {
            @Override
            public void onStopClick(Stop stop) {
                if (sharedMap != null) {
                    LatLng latLng = new LatLng(stop.getLatitude(), stop.getLongitude());
                    sharedMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14f));
                }
            }

            @Override
            public void onStopLongClick(Stop stop, int position) {
                // disabled
            }
        });

        recyclerView.setAdapter(stopAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int fromPos = viewHolder.getAdapterPosition();
                int toPos = target.getAdapterPosition();
                Collections.swap(tripDay.getStops(), fromPos, toPos);
                stopAdapter.updateStops(new ArrayList<>(tripDay.getStops()));
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                tripDay.removeStop(position);
                viewModel.updateDay(dayNumber - 1, tripDay);
                stopAdapter.updateStops(new ArrayList<>(tripDay.getStops()));
                new Handler(Looper.getMainLooper()).post(() -> {
                    stopAdapter.notifyDataSetChanged();
                    drawDayRoute();
                });
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);

        CheckBox cbAutoSort = view.findViewById(R.id.cbAutoSortStops);
        cbAutoSort.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                originalStopsOrder = new ArrayList<>(tripDay.getStops());
                sortStopsByDistance();
            } else if (originalStopsOrder != null) {
                tripDay.setStops(new ArrayList<>(originalStopsOrder));
                originalStopsOrder = null;
            }
            stopAdapter.updateStops(new ArrayList<>(tripDay.getStops()));
            drawDayRoute();
        });

        view.findViewById(R.id.btnClearAllStops).setOnClickListener(v -> {
            tripDay.getStops().clear();
            viewModel.updateDay(dayNumber - 1, tripDay);
            stopAdapter.updateStops(new ArrayList<>());
            drawDayRoute();
        });

        return view;
    }

    private void drawDayRoute() {
        if (tripDay == null || sharedMap == null) return;

        List<Stop> stops = tripDay.getStops();
        if (stops.isEmpty()) {
            requireActivity().runOnUiThread(() -> {
                sharedMap.clear();
                tvTotalDistance.setText("ระยะทางรวม: -");
                tvTotalTravelTime.setText("เวลาเดินทางรวม: -");
                tvTotalRestTime.setText("รวมเวลาทั้งวัน: -");
            });
            return;
        }

        if (stops.size() == 1) {
            requireActivity().runOnUiThread(() -> {
                sharedMap.clear();
                LatLng latLng = new LatLng(stops.get(0).getLatitude(), stops.get(0).getLongitude());
                sharedMap.addMarker(new MarkerOptions().position(latLng).title(stops.get(0).getName()));
                sharedMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14f));
                tvTotalDistance.setText("ระยะทางรวม: -");
                tvTotalTravelTime.setText("เวลาเดินทางรวม: -");
                tvTotalRestTime.setText("รวมเวลาทั้งวัน: " + stops.get(0).getDurationAtStop() + " นาที");
            });
            return;
        }

        calculateTravelTimesForDay();
    }

    private void showStopDetailDialog(String name, double lat, double lng, @Nullable Bitmap photoBitmap) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("เพิ่มรายละเอียดจุดแวะ");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 20, 20, 20);

        EditText etDesc = new EditText(getContext());
        etDesc.setHint("คำอธิบายสถานที่");
        layout.addView(etDesc);

        TextView tvDurationPicker = new TextView(getContext());
        tvDurationPicker.setText("แตะเพื่อเลือกเวลาพัก");
        layout.addView(tvDurationPicker);

        final int[] duration = {0};
        tvDurationPicker.setOnClickListener(v -> {
            new TimePickerDialog(getContext(), (view, h, m) -> {
                duration[0] = h * 60 + m;
                tvDurationPicker.setText(h + " ชม. " + m + " นาที");
            }, 0, 0, true).show();
        });

        builder.setView(layout);
        builder.setPositiveButton("เพิ่ม", (dialog, which) -> {
            Stop stop = new Stop(name, etDesc.getText().toString(), R.drawable.place_1, lat, lng);
            stop.setDurationAtStop(duration[0]);

            if (photoBitmap != null) {
                stop.setPhotoBitmap(photoBitmap); // 🔸 อย่าลืมให้ model Stop รองรับ field นี้ด้วย
                Log.d("StopBitmap", "Stop " + stop.getName() + " has bitmap? " + (stop.getPhotoBitmap() != null));
            }

            tripDay.addStop(stop);
            viewModel.updateDay(dayNumber - 1, tripDay);
            stopAdapter.updateStops(tripDay.getStops());
            // 🔽 โหลดรูปจาก imageUrl → photoBitmap (ถ้ายังไม่มี)
            for (Stop s : tripDay.getStops()) {
                if (s.getPhotoBitmap() == null && s.getImageUrl() != null) {
                    loadImageFromUrl(s.getImageUrl(), s);
                }
            }


            drawDayRoute();
        });
        builder.setNegativeButton("ยกเลิก", null);
        builder.show();
    }


    private void calculateTravelTimesForDay() {
        List<Stop> stops = tripDay.getStops();
        if (stops.size() < 2) return;

        String apiKey = getString(R.string.google_maps_key);
        StringBuilder urlBuilder = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
        urlBuilder.append("origin=").append(stops.get(0).getLatitude()).append(",").append(stops.get(0).getLongitude());
        urlBuilder.append("&destination=").append(stops.get(stops.size() - 1).getLatitude()).append(",").append(stops.get(stops.size() - 1).getLongitude());

        if (stops.size() > 2) {
            urlBuilder.append("&waypoints=");
            for (int i = 1; i < stops.size() - 1; i++) {
                Stop s = stops.get(i);
                urlBuilder.append(s.getLatitude()).append(",").append(s.getLongitude());
                if (i < stops.size() - 2) urlBuilder.append("|");
            }
        }

        urlBuilder.append("&key=").append(apiKey);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(urlBuilder.toString()).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) return;

                try {
                    String json = response.body().string();
                    JSONObject root = new JSONObject(json);
                    JSONObject route = root.getJSONArray("routes").getJSONObject(0);
                    JSONArray legs = route.getJSONArray("legs");

                    int totalTravelMinutes = 0;
                    int totalDistanceMeters = 0;

                    for (int i = 1; i < stops.size(); i++) {
                        JSONObject leg = legs.getJSONObject(i - 1);
                        int durationSec = leg.getJSONObject("duration").getInt("value");
                        int distanceMeters = leg.getJSONObject("distance").getInt("value");

                        stops.get(i).setTravelTimeFromPrevious(durationSec / 60);
                        totalTravelMinutes += durationSec / 60;
                        totalDistanceMeters += distanceMeters;
                    }

                    int totalRestMinutes = getTotalRestMinutes(stops);
                    tripDay.setTotalDistanceKm(totalDistanceMeters / 1000);
                    tripDay.setTotalRestMinutes(totalRestMinutes);
                    tripDay.setTotalTravelMinutes(totalTravelMinutes); // ✅ สำคัญ!!


                    String encodedPolyline = route.getJSONObject("overview_polyline").getString("points");
                    List<LatLng> points = MapUtils.decodePolyline(encodedPolyline);
                    PolylineOptions polylineOptions = new PolylineOptions().addAll(points).color(Color.BLUE).width(8f);

                    final int finalTotalDistanceMeters = totalDistanceMeters;
                    final int finalTotalTravelMinutes = totalTravelMinutes;
                    final int finalTotalRestMinutes = totalRestMinutes;

                    requireActivity().runOnUiThread(() -> {
                        if (sharedMap != null) {
                            sharedMap.clear();
                            sharedMap.addPolyline(polylineOptions);

                            for (Stop stop : stops) {
                                LatLng latLng = new LatLng(stop.getLatitude(), stop.getLongitude());
                                sharedMap.addMarker(new MarkerOptions().position(latLng).title(stop.getName()));
                            }

                            if (!points.isEmpty()) {
                                sharedMap.animateCamera(CameraUpdateFactory.newLatLngZoom(points.get(0), 12f));
                            }
                        }

                        String distanceKm = String.format(Locale.getDefault(), "%.2f กม.", finalTotalDistanceMeters / 1000.0);
                        String travelFormatted = formatMinutesToReadable(finalTotalTravelMinutes);
                        String restFormatted = formatMinutesToReadable(finalTotalRestMinutes);
                        String totalFormatted = formatMinutesToReadable(finalTotalRestMinutes + finalTotalTravelMinutes);

                        tvTotalDistance.setText("ระยะทางรวม: " + distanceKm);
                        tvTotalTravelTime.setText("เวลาเดินทางรวม: " + travelFormatted);
                        tvTotalRestTime.setText("รวมเวลาทั้งวัน: " + totalFormatted + "\n• เดินทาง: " + travelFormatted + " | พัก: " + restFormatted);

                        stopAdapter.notifyDataSetChanged();
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private int getTotalRestMinutes(List<Stop> stops) {
        int total = 0;
        for (Stop s : stops) total += s.getDurationAtStop();
        return total;
    }

    private String formatMinutesToReadable(int minutes) {
        int hours = minutes / 60;
        int remaining = minutes % 60;
        if (hours > 0 && remaining > 0) {
            return String.format(Locale.getDefault(), "%d ชม. %d นาที", hours, remaining);
        } else if (hours > 0) {
            return String.format(Locale.getDefault(), "%d ชม.", hours);
        } else {
            return String.format(Locale.getDefault(), "%d นาที", remaining);
        }
    }

    private void sortStopsByDistance() {
        if (tripDay.getStops().size() <= 1) return;

        List<Stop> sorted = new ArrayList<>();
        List<Stop> remaining = new ArrayList<>(tripDay.getStops());
        Stop current = remaining.remove(0);
        sorted.add(current);

        while (!remaining.isEmpty()) {
            Stop nearest = remaining.get(0);
            float[] minResult = new float[1];
            Location.distanceBetween(current.getLatitude(), current.getLongitude(), nearest.getLatitude(), nearest.getLongitude(), minResult);
            float minDistance = minResult[0];

            for (Stop s : remaining) {
                float[] results = new float[1];
                Location.distanceBetween(current.getLatitude(), current.getLongitude(), s.getLatitude(), s.getLongitude(), results);
                if (results[0] < minDistance) {
                    nearest = s;
                    minDistance = results[0];
                }
            }

            current = nearest;
            sorted.add(current);
            remaining.remove(current);
        }

        tripDay.setStops(sorted);
        viewModel.updateDay(dayNumber - 1, tripDay);
    }

    private void loadImageFromUrl(String url, Stop stop) {
        Glide.with(requireContext())
                .asBitmap()
                .load(url)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        stop.setPhotoBitmap(resource);
                        stopAdapter.notifyDataSetChanged(); // รีเฟรชภาพใน list
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {}
                });
    }


}