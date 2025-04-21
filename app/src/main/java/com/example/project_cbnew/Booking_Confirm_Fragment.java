package com.example.project_cbnew;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_cbnew.adapter.TripDayOverviewAdapter;
import com.example.project_cbnew.model.Booking;
import com.example.project_cbnew.model.Car;
import com.example.project_cbnew.model.Stop;
import com.example.project_cbnew.model.Trip;
import com.example.project_cbnew.model.TripDay;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import okhttp3.*;

public class Booking_Confirm_Fragment extends Fragment {

    private static final String ARG_BOOKING = "booking";
    private static final String ARG_CAR = "car";
    private static final String ARG_TRIP = "trip";

    private Trip trip;
    private Booking booking;
    private Car selectedCar;
    private MapView mapView;
    private GoogleMap googleMap;

    public static Booking_Confirm_Fragment newInstance(Booking booking, Car car, Trip trip) {
        Booking_Confirm_Fragment fragment = new Booking_Confirm_Fragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_BOOKING, booking);
        args.putParcelable(ARG_CAR, car);
        args.putParcelable(ARG_TRIP, trip);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_booking_confirm, container, false);

        if (getArguments() != null) {
            booking = getArguments().getParcelable(ARG_BOOKING);
            selectedCar = getArguments().getParcelable(ARG_CAR);
            trip = getArguments().getParcelable(ARG_TRIP);
        }

        // Views
        TextView tvTripName = view.findViewById(R.id.tvTripName);
        TextView tvCarName = view.findViewById(R.id.tvCarName);
        TextView tvType = view.findViewById(R.id.tvType);
        TextView tvStart = view.findViewById(R.id.tvStart);
        TextView tvEnd = view.findViewById(R.id.tvEnd);
        TextView tvDateRange = view.findViewById(R.id.tvDateRange);
        TextView tvDistance = view.findViewById(R.id.tvDistance);
        TextView tvNote = view.findViewById(R.id.tvNote);
        TextView tvSeats = view.findViewById(R.id.tvSeats);
        TextView tvDepartureTime = view.findViewById(R.id.tvDepartureTime);
        TextView tvEstimatedPrice = view.findViewById(R.id.tvEstimatedPrice);
        Button btnConfirm = view.findViewById(R.id.btnConfirmBooking);
        ImageButton btnBack = view.findViewById(R.id.btnBack);
        RecyclerView rvTripOverview = view.findViewById(R.id.rvTripOverview);
        mapView = view.findViewById(R.id.mapView);

        // Back
        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        // Set endDate ถ้าไม่ได้กำหนด
        if ((booking.getEndDate() == null || booking.getEndDate().isEmpty())
                && booking.getStartDate() != null && trip != null && trip.getTripDays() != null) {
            try {
                // 🔍 พยายามแปลง startDate ทั้งสองแบบ (dd MMM yyyy หรือ yyyy-MM-dd)
                SimpleDateFormat inputFormat1 = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH); // เช่น 11 Apr 2025
                SimpleDateFormat inputFormat2 = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);  // เช่น 2025-04-11
                SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                Date startDate;

                try {
                    // ลองแปลงแบบ yyyy-MM-dd ก่อน
                    startDate = inputFormat2.parse(booking.getStartDate());
                } catch (Exception e1) {
                    // ถ้าแปลงไม่ได้ ลองใช้แบบ dd MMM yyyy
                    startDate = inputFormat1.parse(booking.getStartDate());
                }

                // คำนวณ endDate
                Calendar cal = Calendar.getInstance();
                cal.setTime(startDate);
                cal.add(Calendar.DATE, trip.getTripDays().size() - 1);

                // แปลงกลับให้เป็น format เดียวกันเสมอ
                String standardizedStart = outputFormat.format(startDate);
                String standardizedEnd = outputFormat.format(cal.getTime());

                booking.setStartDate(standardizedStart);
                booking.setEndDate(standardizedEnd);

                Log.d("BOOKING_DATE_FIX", "✅ วันที่หลังแปลง: start=" + standardizedStart + ", end=" + standardizedEnd);

            } catch (Exception e) {
                Log.e("BOOKING_DATE_FIX", "❌ แปลงวันที่ไม่สำเร็จ: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Map
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(map -> {
            googleMap = map;
            UiSettings ui = googleMap.getUiSettings();
            ui.setZoomControlsEnabled(true);
            ui.setAllGesturesEnabled(true);
            if (trip != null) drawTripRouteWithDirections();
        });

        // Booking display
        if (booking != null) {
            tvTripName.setText(safeText("🚐 ทริป: ", trip != null ? trip.getTitle() : "-"));
            tvCarName.setText(safeText("🚘 รถ: ", selectedCar != null ? selectedCar.getName() : "-"));
            tvType.setText(safeText("🛻 ประเภทรถ: ", booking.getTypeVehicle()));
            tvStart.setText(safeText("📍 จุดเริ่มต้น: ", booking.getJourneyStart()));
            tvEnd.setText(safeText("🏁 จุดสิ้นสุด: ", booking.getJourneyEnd()));
            tvDateRange.setText("🗓 วันที่: " + formatDateRange(booking.getStartDate(), booking.getEndDate()));

            int seats = 0;
            try {
                seats = Integer.parseInt(booking.getSeats() != null ? booking.getSeats() : "0");
            } catch (Exception ignored) {}
            tvSeats.setText("🧍 จำนวนที่นั่ง: " + (seats > 0 ? seats : "-"));

            String time = trip != null && trip.getExtraFields() != null
                    ? (String) trip.getExtraFields().get("T_DepartureTime")
                    : null;
            tvDepartureTime.setText(safeText("🕒 เวลาออกเดินทาง: ", time));

            tvDistance.setText(String.format(Locale.getDefault(), "📏 ระยะทาง: %.1f กม.", (double) booking.getTotalDistance()));

            double tripPrice = booking.getTotalDistance() * 3.5;
            int carPricePerDay = selectedCar != null ? selectedCar.getPrice() : 0;
            int days = trip != null && trip.getTripDays() != null ? trip.getTripDays().size() : 1;
            double total = tripPrice + (carPricePerDay * days);
            tvEstimatedPrice.setText(String.format(Locale.getDefault(), "💰 ราคารวมทั้งหมด: %.2f บาท", total));

            tvNote.setText(safeText("📝 หมายเหตุ: ", booking.getNote()));
        }

        // Overview list
        if (trip != null && trip.getTripDays() != null) {
            TripDayOverviewAdapter adapter = new TripDayOverviewAdapter(trip.getTripDays());
            rvTripOverview.setLayoutManager(new LinearLayoutManager(getContext()));
            rvTripOverview.setAdapter(adapter);
            adapter.setOnDayClickListener((pos, day) -> drawSingleDayRoute(day));
        }

        // Confirm button
        btnConfirm.setOnClickListener(v -> {
            String uid = FirebaseAuth.getInstance().getUid();
            if (uid == null) {
                Toast.makeText(getContext(), "กรุณาเข้าสู่ระบบก่อน", Toast.LENGTH_SHORT).show();
                return;
            }

            booking.setAccountId(uid);
            booking.setBookingDate(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime()));
            booking.setStatus("รอตรวจสอบ");

            // ✅ ตั้งค่าเริ่มต้นป้องกัน null
            booking.setDriverRating(0.0);
            booking.setDriverReviewNote("");

            FirebaseFirestore.getInstance().collection("bookings")
                    .add(booking)
                    .addOnSuccessListener(docRef -> {
                        String generatedId = docRef.getId();
                        booking.setBookingId(generatedId); // ตั้ง bookingId

                        // ✅ บันทึกอีกครั้งให้รวม bookingId ด้วย
                        docRef.set(booking).addOnSuccessListener(unused -> {
                            Toast.makeText(getContext(), "บันทึกการจองเรียบร้อยแล้ว", Toast.LENGTH_SHORT).show();
                            requireActivity().getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.frameLayout, new Booking_Success_Fragment())
                                    .commit();
                        }).addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "เกิดข้อผิดพลาดตอนอัปเดต bookingId: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });

                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "เกิดข้อผิดพลาด: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        });

        return view;
    }

    private String safeText(String label, String value) {
        return label + (value != null && !value.trim().isEmpty() ? value : "-");
    }

    private String formatDateRange(String start, String end) {
        try {
            Log.d("BOOKING_DATE", "start=" + start + ", end=" + end);

            // ✅ ใช้รูปแบบที่ตรงกับข้อมูลใน booking (เช่น yyyy-MM-dd)
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

            Date startDate = inputFormat.parse(start);
            Date endDate;

            if (end != null && !end.trim().isEmpty()) {
                endDate = inputFormat.parse(end);
            } else if (trip != null && trip.getTripDays() != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(startDate);
                cal.add(Calendar.DATE, trip.getTripDays().size() - 1);
                endDate = cal.getTime();
            } else {
                return outputFormat.format(startDate);
            }

            return outputFormat.format(startDate) + " - " + outputFormat.format(endDate);
        } catch (Exception e) {
            e.printStackTrace();
            return "-";
        }
    }




    private void drawTripRouteWithDirections() {
        if (googleMap == null || trip == null || trip.getTripDays() == null) return;

        googleMap.clear();
        OkHttpClient client = new OkHttpClient();
        int[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.CYAN, Color.MAGENTA, Color.YELLOW, Color.DKGRAY};

        for (int i = 0; i < trip.getTripDays().size(); i++) {
            TripDay day = trip.getTripDays().get(i);
            List<Stop> stops = new ArrayList<>();
            for (Stop s : day.getStops()) {
                if (s.getLatitude() != 0 && s.getLongitude() != 0) stops.add(s);
            }
            if (stops.size() < 2) continue;

            StringBuilder url = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
            url.append("origin=").append(stops.get(0).getLatitude()).append(",").append(stops.get(0).getLongitude());
            url.append("&destination=").append(stops.get(stops.size() - 1).getLatitude()).append(",").append(stops.get(stops.size() - 1).getLongitude());

            if (stops.size() > 2) {
                url.append("&waypoints=");
                for (int j = 1; j < stops.size() - 1; j++) {
                    url.append(stops.get(j).getLatitude()).append(",").append(stops.get(j).getLongitude());
                    if (j < stops.size() - 2) url.append("|");
                }
            }

            url.append("&key=").append(getString(R.string.google_maps_key));
            int finalI = i;

            client.newCall(new Request.Builder().url(url.toString()).build())
                    .enqueue(new Callback() {
                        @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {}
                        @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            if (!response.isSuccessful() || response.body() == null) return;

                            try {
                                String json = response.body().string();
                                JSONObject root = new JSONObject(json);
                                JSONArray routes = root.optJSONArray("routes");
                                if (routes == null || routes.length() == 0) return;

                                JSONObject overviewPolyline = routes.getJSONObject(0).optJSONObject("overview_polyline");
                                if (overviewPolyline == null) return;

                                String encoded = overviewPolyline.optString("points", "");
                                List<LatLng> path = MapUtils.decodePolyline(encoded);

                                requireActivity().runOnUiThread(() -> {
                                    if (googleMap == null) return;
                                    googleMap.addPolyline(new PolylineOptions()
                                            .addAll(path)
                                            .color(colors[finalI % colors.length])
                                            .width(8f));

                                    for (Stop s : stops) {
                                        LatLng latLng = new LatLng(s.getLatitude(), s.getLongitude());
                                        googleMap.addMarker(new MarkerOptions().position(latLng).title("วัน " + (finalI + 1) + ": " + s.getName()));
                                    }

                                    if (finalI == 0 && !path.isEmpty()) {
                                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(path.get(0), 10f));
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
        }
    }

    private void drawSingleDayRoute(TripDay day) {
        if (googleMap == null || day == null || day.getStops().size() < 2) return;
        googleMap.clear();
        List<Stop> stops = day.getStops();
        for (Stop s : stops) {
            if (s.getLatitude() != 0 && s.getLongitude() != 0) {
                googleMap.addMarker(new MarkerOptions().position(new LatLng(s.getLatitude(), s.getLongitude())).title(s.getName()));
            }
        }
        MapUtils.drawPolylineWithDirections(requireContext(), googleMap, stops);
    }

    @Override public void onResume() { super.onResume(); if (mapView != null) mapView.onResume(); }
    @Override public void onPause() { if (mapView != null) mapView.onPause(); super.onPause(); }
    @Override public void onDestroy() { if (mapView != null) mapView.onDestroy(); super.onDestroy(); }
    @Override public void onLowMemory() { super.onLowMemory(); if (mapView != null) mapView.onLowMemory(); }
}
