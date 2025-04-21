package com.example.project_cbnew;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_cbnew.adapter.TripDayOverviewAdapter;
import com.example.project_cbnew.model.Stop;
import com.example.project_cbnew.model.TripDay;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Trip_Overview_Fragment extends Fragment {

    private CreateTripViewModel viewModel;
    private final Calendar calendarStart = Calendar.getInstance();

    private EditText etStartDate, etEndDate;
    private TextView tvStartLocation, tvEndLocation, tvTotalDistance, tvTotalDuration;

    private RecyclerView recyclerView;
    private TripDayOverviewAdapter dayOverviewAdapter;
    private GoogleMap sharedMap;
    private boolean isMapPending = false;

    @Override
    public void onResume() {
        super.onResume();
        if (sharedMap != null && isMapPending) {
            isMapPending = false;
            drawAllRoutesOnMap();
        }
    }

    public void setSharedMap(GoogleMap map) {
        this.sharedMap = map;
        if (viewModel != null && viewModel.getTripDayList().size() > 0) {
            drawAllRoutesOnMap();
        } else {
            isMapPending = true;
        }
    }

    public Trip_Overview_Fragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_trip_overview, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(CreateTripViewModel.class);

        EditText etTripTitle = view.findViewById(R.id.etTripTitle);
        EditText etTripDescription = view.findViewById(R.id.etTripDescription);
        etStartDate = view.findViewById(R.id.etStartDate);
        etEndDate = view.findViewById(R.id.etEndDate);
        tvStartLocation = view.findViewById(R.id.etStartLocation);
        tvEndLocation = view.findViewById(R.id.etEndLocation);
        tvTotalDistance = view.findViewById(R.id.tvTotalDistance);
        tvTotalDuration = view.findViewById(R.id.tvTotalDuration);
        recyclerView = view.findViewById(R.id.recyclerViewDayOverview);

        etTripTitle.setText(viewModel.getTripTitle().getValue());
        etTripDescription.setText(viewModel.getTripDescription().getValue());

        etTripTitle.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) viewModel.setTripTitle(etTripTitle.getText().toString());
        });

        etTripDescription.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) viewModel.setTripDescription(etTripDescription.getText().toString());
        });

        etStartDate.setOnClickListener(v -> {
            new DatePickerDialog(requireContext(), (view1, year, month, dayOfMonth) -> {
                calendarStart.set(year, month, dayOfMonth);
                updateStartDateUI();
                updateEndDateBasedOnDays();
            }, calendarStart.get(Calendar.YEAR),
                    calendarStart.get(Calendar.MONTH),
                    calendarStart.get(Calendar.DAY_OF_MONTH)).show();
        });

        setupRecyclerView();
        updateStartDateUI();
        updateEndDateBasedOnDays();
        updateStartEndLocations();

        viewModel.getTripDays().observe(getViewLifecycleOwner(), tripDays -> {
            updateEndDateBasedOnDays();
            updateStartEndLocations();
            dayOverviewAdapter.updateData(tripDays);
            calculateDaySummaries();

            if (sharedMap != null) {
                drawAllRoutesOnMap();
            } else {
                isMapPending = true;
            }
        });

        view.findViewById(R.id.btnPreviewTrip).setOnClickListener(v -> {
            String title = etTripTitle.getText().toString().trim();
            List<TripDay> tripDays = viewModel.getTripDayList();

            if (title.isEmpty()) {
                Toast.makeText(getContext(), "กรุณากรอกชื่อทริปก่อน", Toast.LENGTH_SHORT).show();
                etTripTitle.requestFocus();
                return;
            }

            if (tripDays == null || tripDays.isEmpty()) {
                Toast.makeText(getContext(), "ต้องมีอย่างน้อย 1 วันในทริป", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean hasStop = false;
            for (TripDay day : tripDays) {
                if (!day.getStops().isEmpty()) {
                    hasStop = true;
                    break;
                }
            }

            if (!hasStop) {
                Toast.makeText(getContext(), "ต้องมีจุดแวะอย่างน้อย 1 จุด", Toast.LENGTH_SHORT).show();
                return;
            }

            // บันทึกชื่อทริปไว้ใน ViewModel ด้วย
            viewModel.setTripTitle(title);

            // ไปหน้าพรีวิว
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frameLayout, new Trip_Preview_Fragment())
                    .addToBackStack(null)
                    .commit();
        });


        return view;
    }

    private void updateStartDateUI() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        etStartDate.setText(sdf.format(calendarStart.getTime()));
    }

    private void updateEndDateBasedOnDays() {
        List<TripDay> tripDays = viewModel.getTripDayList();
        if (!tripDays.isEmpty()) {
            Calendar calendarEnd = (Calendar) calendarStart.clone();
            calendarEnd.add(Calendar.DATE, tripDays.size() - 1);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            etEndDate.setText(sdf.format(calendarEnd.getTime()));
        }
    }

    private void updateStartEndLocations() {
        List<TripDay> tripDays = viewModel.getTripDayList();
        if (tripDays.isEmpty()) return;

        List<Stop> firstDayStops = tripDays.get(0).getStops();
        if (!firstDayStops.isEmpty()) {
            tvStartLocation.setText("จุดเริ่มต้น: " + firstDayStops.get(0).getName());
        } else {
            tvStartLocation.setText("จุดเริ่มต้น: -");
        }

        List<Stop> lastDayStops = tripDays.get(tripDays.size() - 1).getStops();
        if (!lastDayStops.isEmpty()) {
            tvEndLocation.setText("จุดสิ้นสุด: " + lastDayStops.get(lastDayStops.size() - 1).getName());
        } else {
            tvEndLocation.setText("จุดสิ้นสุด: -");
        }
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        dayOverviewAdapter = new TripDayOverviewAdapter(viewModel.getTripDayList());
        recyclerView.setAdapter(dayOverviewAdapter);
    }

    private void drawAllRoutesOnMap() {
        if (sharedMap == null) return;
        requireActivity().runOnUiThread(sharedMap::clear);

        OkHttpClient client = new OkHttpClient();
        List<TripDay> allDays = viewModel.getTripDayList();
        int[] routeColors = {
                Color.RED, Color.BLUE, Color.GREEN, Color.CYAN,
                Color.MAGENTA, Color.YELLOW, Color.DKGRAY
        };

        AtomicInteger totalDistance = new AtomicInteger(0);
        AtomicInteger totalDuration = new AtomicInteger(0);

        // ✅ Reset distance & duration ของทุกวันก่อนคำนวณใหม่
        for (TripDay day : allDays) {
            day.setTotalDistanceKm(0);
            day.setTotalTravelMinutes(0);
        }

        Map<Integer, Integer> extraDistanceMap = new HashMap<>();
        Map<Integer, Integer> extraDurationMap = new HashMap<>();

        // 1️⃣ วาดเส้นทางในแต่ละวัน
        for (int dayIndex = 0; dayIndex < allDays.size(); dayIndex++) {
            TripDay day = allDays.get(dayIndex);
            List<Stop> stops = new ArrayList<>(day.getStops());

            if (stops.size() < 2) continue;

            StringBuilder urlBuilder = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
            urlBuilder.append("origin=").append(stops.get(0).getLatitude()).append(",").append(stops.get(0).getLongitude());
            urlBuilder.append("&destination=").append(stops.get(stops.size() - 1).getLatitude()).append(",").append(stops.get(stops.size() - 1).getLongitude());

            if (stops.size() > 2) {
                urlBuilder.append("&waypoints=");
                for (int i = 1; i < stops.size() - 1; i++) {
                    urlBuilder.append(stops.get(i).getLatitude()).append(",").append(stops.get(i).getLongitude());
                    if (i < stops.size() - 2) urlBuilder.append("|");
                }
            }

            urlBuilder.append("&key=").append(getString(R.string.google_maps_key));
            Request request = new Request.Builder().url(urlBuilder.toString()).build();

            int finalDayIndex = dayIndex;
            client.newCall(request).enqueue(new Callback() {
                @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                }

                @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (!response.isSuccessful() || response.body() == null) return;

                    try {
                        String json = response.body().string();
                        JSONObject root = new JSONObject(json);
                        JSONArray routes = root.optJSONArray("routes");
                        if (routes == null || routes.length() == 0) return;

                        JSONObject leg = routes.getJSONObject(0).optJSONArray("legs").optJSONObject(0);
                        JSONObject poly = routes.getJSONObject(0).optJSONObject("overview_polyline");

                        int distanceKm = leg.getJSONObject("distance").getInt("value") / 1000;
                        int durationMin = leg.getJSONObject("duration").getInt("value") / 60;

                        day.setTotalDistanceKm(distanceKm);
                        day.setTotalTravelMinutes(durationMin);

                        totalDistance.addAndGet(distanceKm);
                        totalDuration.addAndGet(durationMin);

                        List<LatLng> points = MapUtils.decodePolyline(poly.getString("points"));

                        requireActivity().runOnUiThread(() -> {
                            sharedMap.addPolyline(new PolylineOptions()
                                    .addAll(points)
                                    .color(routeColors[finalDayIndex % routeColors.length])
                                    .width(8f));

                            for (Stop stop : stops) {
                                sharedMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(stop.getLatitude(), stop.getLongitude()))
                                        .title("วัน " + (finalDayIndex + 1) + ": " + stop.getName()));
                            }

                            if (finalDayIndex == 0 && !points.isEmpty()) {
                                sharedMap.moveCamera(CameraUpdateFactory.newLatLngZoom(points.get(0), 10f));
                            }

                            int extraDist = getOrZero(extraDistanceMap, finalDayIndex);
                            int extraDur = getOrZero(extraDurationMap, finalDayIndex);
                            day.setTotalDistanceKm(day.getTotalDistanceKm() + extraDist);
                            day.setTotalTravelMinutes(day.getTotalTravelMinutes() + extraDur);

                            tvTotalDistance.setText("รวมระยะทาง: " + totalDistance.get() + " กม.");
                            tvTotalDuration.setText("รวมเวลาทั้งหมด: " + formatDuration(totalDuration.get()));
                            dayOverviewAdapter.notifyDataSetChanged();
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        // 2️⃣ วาดเส้นทางข้ามวัน (N → N+1) → บวกเข้า extraMap แยก
        for (int i = 0; i < allDays.size() - 1; i++) {
            final int crossDayIndex = i;
            List<Stop> currentStops = allDays.get(i).getStops();
            List<Stop> nextStops = allDays.get(i + 1).getStops();

            if (currentStops.isEmpty() || nextStops.isEmpty()) continue;

            Stop lastStop = currentStops.get(currentStops.size() - 1);
            Stop firstStop = nextStops.get(0);

            String crossUrl = "https://maps.googleapis.com/maps/api/directions/json?"
                    + "origin=" + lastStop.getLatitude() + "," + lastStop.getLongitude()
                    + "&destination=" + firstStop.getLatitude() + "," + firstStop.getLongitude()
                    + "&key=" + getString(R.string.google_maps_key);

            Request crossRequest = new Request.Builder().url(crossUrl).build();

            client.newCall(crossRequest).enqueue(new Callback() {
                @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                }

                @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (!response.isSuccessful() || response.body() == null) return;

                    try {
                        String json = response.body().string();
                        JSONObject root = new JSONObject(json);
                        JSONArray routes = root.optJSONArray("routes");
                        if (routes == null || routes.length() == 0) return;

                        JSONObject leg = routes.getJSONObject(0).optJSONArray("legs").optJSONObject(0);
                        int distanceKm = leg.getJSONObject("distance").getInt("value") / 1000;
                        int durationMin = leg.getJSONObject("duration").getInt("value") / 60;

                        totalDistance.addAndGet(distanceKm);
                        totalDuration.addAndGet(durationMin);

                        TripDay nextDay = allDays.get(crossDayIndex + 1);
                        int newDistance = nextDay.getTotalDistanceKm() + distanceKm;
                        int newDuration = nextDay.getTotalTravelMinutes() + durationMin;
                        nextDay.setTotalDistanceKm(newDistance);
                        nextDay.setTotalTravelMinutes(newDuration);



                        requireActivity().runOnUiThread(() -> {
                            tvTotalDistance.setText("รวมระยะทาง: " + totalDistance.get() + " กม.");
                            tvTotalDuration.setText("รวมเวลาทั้งหมด: " + formatDuration(totalDuration.get()));
                            dayOverviewAdapter.notifyDataSetChanged();
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private int getOrZero(Map<Integer, Integer> map, int key) {
        return map.containsKey(key) ? map.get(key) : 0;
    }





    private String formatDuration(int minutes) {
        int hours = minutes / 60;
        int mins = minutes % 60;
        if (hours > 0 && mins > 0) return hours + " ชม. " + mins + " นาที";
        else if (hours > 0) return hours + " ชม.";
        else return mins + " นาที";
    }


    private void calculateDaySummaries() {
        // (unchanged, kept as-is)
    }
}
