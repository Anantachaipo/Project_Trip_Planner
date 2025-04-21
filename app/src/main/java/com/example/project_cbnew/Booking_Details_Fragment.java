package com.example.project_cbnew;

import android.app.AlertDialog;
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
import com.example.project_cbnew.hardcodetrip.HardcodedTripProvider;
import com.example.project_cbnew.model.Booking;
import com.example.project_cbnew.model.Stop;
import com.example.project_cbnew.model.Trip;
import com.example.project_cbnew.model.TripDay;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;

import java.util.*;

public class Booking_Details_Fragment extends Fragment {

    private static final String ARG_BOOKING = "booking";
    private Booking booking;
    private MapView mapView;
    private RecyclerView rvTripOverview;
    private static final LatLng COMPANY_LOCATION = new LatLng(13.855969, 100.647614);

    public static Booking_Details_Fragment newInstance(Booking booking) {
        Booking_Details_Fragment fragment = new Booking_Details_Fragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_BOOKING, booking);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            booking = getArguments().getParcelable(ARG_BOOKING);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_booking_details, container, false);

        // Views
        TextView tvTripTitle = view.findViewById(R.id.tvTripName);
        TextView tvDateRange = view.findViewById(R.id.tvDateRange);
        TextView tvStart = view.findViewById(R.id.tvStart);
        TextView tvEnd = view.findViewById(R.id.tvEnd);
        TextView tvSeats = view.findViewById(R.id.tvSeats);
        TextView tvVehicle = view.findViewById(R.id.tvType);
        TextView tvDistance = view.findViewById(R.id.tvDistance);
        TextView tvStatus = view.findViewById(R.id.tvBookingStatus);
        TextView tvNote = view.findViewById(R.id.tvNote);
        rvTripOverview = view.findViewById(R.id.rvTripOverview);
        ImageButton btnBack = view.findViewById(R.id.btnBack);
        LinearLayout containerLayout = view.findViewById(R.id.containerLayout); // ✅ ใช้ชื่อไม่ซ้ำ

        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        String status = null; //

        if (booking != null) {
            tvTripTitle.setText("🚐 ทริป: " + booking.getTripTitle());
            tvDateRange.setText("🗓 " + booking.getStartDate() + " - " + booking.getEndDate());
            tvStart.setText("🟢 จุดเริ่มต้น: " + booking.getJourneyStart());
            tvEnd.setText("🔴 จุดสิ้นสุด: " + booking.getJourneyEnd());
            tvSeats.setText("👥 จำนวนที่นั่ง: " + booking.getSeats());
            tvVehicle.setText("🚐 ประเภทรถ: " + booking.getTypeVehicle());
//            tvDistance.setText("📏 ระยะทาง: " + booking.getTotalDistance() + " กม.");
            tvDistance.setText("📏 ระยะทาง: " +
                    (booking.getTotalDistance() > 0 ? booking.getTotalDistance() : "-") + " กม.");

            tvNote.setText("📝 หมายเหตุ: " + (booking.getNote() == null || booking.getNote().isEmpty() ? "-" : booking.getNote()));

            status = booking.getStatus(); // ✅ ตั้งค่าที่นี่
            tvStatus.setText("📌 สถานะ: " + status);

            if ("เดินทางเสร็จสิ้น".equals(status) &&
                    (booking.getDriverRating() == null || booking.getDriverRating() == 0f)) {
                Button btnRate = new Button(getContext());
                btnRate.setText("ให้คะแนนคนขับ");
                btnRate.setBackgroundColor(Color.parseColor("#FF9800"));
                btnRate.setTextColor(Color.WHITE);
                btnRate.setPadding(20, 10, 20, 10);
                // ✅ เพิ่มปุ่มเข้าไปใน layout
                if (containerLayout != null) {
                    containerLayout.addView(btnRate);
                }

                btnRate.setOnClickListener(v -> showRatingDialog());
            }

            switch (status) {
                case "รอตรวจสอบ": tvStatus.setTextColor(Color.parseColor("#FF9800")); break;
                case "รอชำระเงิน": tvStatus.setTextColor(Color.parseColor("#2196F3")); break;
                case "รอคนขับ": tvStatus.setTextColor(Color.parseColor("#673AB7")); break;
                case "รอเดินทาง/อยู่ระหว่างเดินทาง": tvStatus.setTextColor(Color.parseColor("#009688")); break;
                case "เดินทางเสร็จสิ้น": tvStatus.setTextColor(Color.parseColor("#4CAF50")); break;
                case "ปฏิเสธการจอง": tvStatus.setTextColor(Color.RED); break;
                default: tvStatus.setTextColor(Color.GRAY); break;
            }
        }

        rvTripOverview.setLayoutManager(new LinearLayoutManager(getContext()));

        if (booking != null && booking.getTripId() != null) {
            FirebaseFirestore.getInstance().collection("trips")
                    .whereEqualTo(FieldPath.documentId(), booking.getTripId())
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            Trip trip = Trip.fromFirestore(doc);
                            if (trip != null && trip.getTripDays() != null) {
                                List<TripDay> tripDays = trip.getTripDays();
                                handleTripDaysWithJourneyPoints(tripDays);
                            } else {
                                rvTripOverview.setVisibility(View.GONE);
                            }
                        }
                    });
        } else {
            List<Trip> hardcodedTrips = HardcodedTripProvider.getTrips(getContext());
            for (Trip trip : hardcodedTrips) {
                if (trip.getTitle().equals(booking.getTripTitle())) {
                    handleTripDaysWithJourneyPoints(trip.getTripDays());
                    break;
                }
            }
        }

        Button btnCancel = view.findViewById(R.id.btnCancelBooking);

        // ✅ เช็คสถานะ และแสดงปุ่มยกเลิก
        if ("รอตรวจสอบ".equals(status) || "รอชำระเงิน".equals(status)) {
            btnCancel.setVisibility(View.VISIBLE);

            btnCancel.setOnClickListener(v -> {
                new AlertDialog.Builder(requireContext())
                        .setTitle("ยืนยันการยกเลิก")
                        .setMessage("คุณแน่ใจหรือไม่ว่าต้องการยกเลิกการจองนี้?")
                        .setPositiveButton("ยืนยัน", (dialog, which) -> {
                            FirebaseFirestore.getInstance()
                                    .collection("bookings")
                                    .document(booking.getBookingId())
                                    .update("status", "ปฏิเสธการจอง")
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(requireContext(), "ยกเลิกการจองเรียบร้อยแล้ว", Toast.LENGTH_SHORT).show();
                                        requireActivity().getSupportFragmentManager()
                                                .beginTransaction()
                                                .replace(R.id.frameLayout, new MyBooking_Fragment())
                                                .commit();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(requireContext(), "เกิดข้อผิดพลาด: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        })
                        .setNegativeButton("ยกเลิก", null)
                        .show();
            });
        }




        return view;
    }

    private void handleTripDaysWithJourneyPoints(List<TripDay> tripDays) {
        for (int i = 0; i < tripDays.size(); i++) {
            TripDay day = tripDays.get(i);
            List<Stop> originalStops = day.getStops();
            List<Stop> visibleStops = new ArrayList<>(originalStops);
            List<Stop> calculationStops = new ArrayList<>();

            // ✅ กรองเฉพาะ Stop ที่มีพิกัดถูกต้อง
            for (Stop stop : originalStops) {
                if (stop.getLatitude() != null && stop.getLongitude() != null) {
                    calculationStops.add(stop);
                } else {
                    Log.w("SkipStop", "❌ พิกัด stop เป็น null → ข้าม: " + stop.getName());
                }
            }

            if (i == 0) {
                calculationStops.add(0, new Stop("🏢 บริษัท", "สำนักงานใหญ่", COMPANY_LOCATION.latitude, COMPANY_LOCATION.longitude));

                if (booking.getJourneyStart() != null && !booking.getJourneyStart().trim().isEmpty()) {
                    LatLng latLngStart = MapUtils.getLatLngFromAddress(requireContext(), booking.getJourneyStart());
                    if (latLngStart != null) {
                        Stop startStop = new Stop(booking.getJourneyStart(), "จุดเริ่มต้นของคุณ", latLngStart.latitude, latLngStart.longitude);
                        if (booking.getTripId() == null) visibleStops.add(0, startStop);
                        calculationStops.add(1, startStop);
                    }
                }
            }

            if (i == tripDays.size() - 1 && booking.getJourneyEnd() != null && !booking.getJourneyEnd().trim().isEmpty()) {
                LatLng latLngEnd = MapUtils.getLatLngFromAddress(requireContext(), booking.getJourneyEnd());
                if (latLngEnd != null) {
                    Stop endStop = new Stop(booking.getJourneyEnd(), "", latLngEnd.latitude, latLngEnd.longitude);

                    boolean isDuplicate = false;
                    if (!calculationStops.isEmpty()) {
                        Stop lastStop = calculationStops.get(calculationStops.size() - 1);
                        if (lastStop.getLatitude() != null && lastStop.getLongitude() != null) {
                            double latDiff = Math.abs(lastStop.getLatitude() - endStop.getLatitude());
                            double lngDiff = Math.abs(lastStop.getLongitude() - endStop.getLongitude());
                            isDuplicate = latDiff < 0.0001 && lngDiff < 0.0001;
                        }
                    }

                    if (!isDuplicate) {
                        visibleStops.add(endStop);
                        calculationStops.add(endStop);
                    }
                }
            }

            day.setStops(visibleStops);
            calculateDayTravelAndRestTime(day, calculationStops);
        }

        TripDayOverviewAdapter adapter = new TripDayOverviewAdapter(tripDays);
        rvTripOverview.setAdapter(adapter);
    }



    private void calculateDayTravelAndRestTime(TripDay tripDay, List<Stop> stopsForCalc) {
        Log.d("TripCalc", "📆 เริ่มคำนวณวัน: " + tripDay.getTitle());

        // แปลง Stop เป็น LatLng และกรอง null
        List<LatLng> rawPoints = new ArrayList<>();
        for (Stop stop : stopsForCalc) {
            if (stop.getLatitude() != null && stop.getLongitude() != null) {
                rawPoints.add(new LatLng(stop.getLatitude(), stop.getLongitude()));
                Log.d("TripCalc", "📍 Raw Point: " + stop.getName() + " (" + stop.getLatitude() + ", " + stop.getLongitude() + ")");
            } else {
                Log.w("TripCalc", "⚠️ จุดไม่มีพิกัด: " + stop.getName());
            }
        }

        // ✅ ลบพิกัดซ้ำที่ติดกัน
        List<LatLng> points = new ArrayList<>();
        for (LatLng point : rawPoints) {
            if (points.isEmpty() || !isSameLocation(points.get(points.size() - 1), point)) {
                points.add(point);
            } else {
                Log.w("TripCalc", "⚠️ ลบพิกัดซ้ำติดกัน: " + point.latitude + ", " + point.longitude);
            }
        }

        Log.d("TripCalc", "✅ จำนวนจุดหลังกรองซ้ำ: " + points.size());

        if (points.size() < 2) {
            Log.w("DirectionSkip", "❌ จุดน้อยเกินไป: ไม่สามารถคำนวณเส้นทางได้");
            return;
        }

        new Thread(() -> {
            try {
                GeoApiContext geoApiContext = new GeoApiContext.Builder()
                        .apiKey(getString(R.string.google_maps_key))
                        .build();

                com.google.maps.model.LatLng origin = new com.google.maps.model.LatLng(points.get(0).latitude, points.get(0).longitude);
                com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(points.get(points.size() - 1).latitude, points.get(points.size() - 1).longitude);

                Log.d("TripCalc", "🚦 Origin: " + origin);
                Log.d("TripCalc", "🏁 Destination: " + destination);

                if (origin.equals(destination)) {
                    Log.w("DirectionSkip", "❌ จุดเริ่มและจุดจบเหมือนกัน: ข้ามการคำนวณ");
                    return;
                }

                List<com.google.maps.model.LatLng> waypointList = new ArrayList<>();
                for (int i = 1; i < points.size() - 1; i++) {
                    com.google.maps.model.LatLng waypoint = new com.google.maps.model.LatLng(points.get(i).latitude, points.get(i).longitude);
                    waypointList.add(waypoint);
                    Log.d("TripCalc", "📌 Waypoint: " + waypoint);
                }

                DirectionsResult result = DirectionsApi.newRequest(geoApiContext)
                        .origin(origin)
                        .destination(destination)
                        .waypoints(waypointList.toArray(new com.google.maps.model.LatLng[0]))
                        .await();

                double meters = 0;
                double seconds = 0;

                for (DirectionsRoute route : result.routes) {
                    for (DirectionsLeg leg : route.legs) {
                        meters += leg.distance.inMeters;
                        seconds += leg.duration.inSeconds;

                        Log.d("DayLeg", "From: " + leg.startAddress + " → " + leg.endAddress +
                                ", " + leg.distance.humanReadable + ", " + leg.duration.humanReadable);
                    }
                }

                int totalKm = (int) Math.round(meters / 1000);
                int travelMinutes = (int) Math.round(seconds / 60);

                tripDay.setTotalDistanceKm(totalKm);
                tripDay.setTotalTravelMinutes(travelMinutes);

                Log.d("TripCalc", "✅ รวมระยะทาง: " + totalKm + " กม., เวลาเดินทาง: " + travelMinutes + " นาที");

                requireActivity().runOnUiThread(() -> {
                    if (rvTripOverview.getAdapter() != null) {
                        rvTripOverview.getAdapter().notifyDataSetChanged();
                    }
                });

            } catch (Exception e) {
                Log.e("DirectionError", "❌ เกิดข้อผิดพลาดขณะขอเส้นทาง: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }


    // 🔧 ฟังก์ชันเปรียบเทียบพิกัดว่าตรงกันหรือไม่
    private boolean isSameLocation(LatLng a, LatLng b) {
        double threshold = 0.00001; // ระยะความต่างพอประมาณ
        return Math.abs(a.latitude - b.latitude) < threshold &&
                Math.abs(a.longitude - b.longitude) < threshold;
    }

    private void showRatingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_rate_driver, null);
        builder.setView(dialogView);

        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
        EditText etReview = dialogView.findViewById(R.id.etReview);

        builder.setTitle("ให้คะแนนคนขับ")
                .setPositiveButton("ส่ง", (dialog, which) -> {
                    float rating = ratingBar.getRating();
                    String review = etReview.getText().toString();

                    FirebaseFirestore.getInstance()
                            .collection("bookings")
                            .document(booking.getBookingId())
                            .update("driverRating", rating, "driverReviewNote", review)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "ส่งคะแนนเรียบร้อย", Toast.LENGTH_SHORT).show();

                                // ✅ กลับไปหน้า MyBooking_Fragment
                                requireActivity().getSupportFragmentManager()
                                        .beginTransaction()
                                        .replace(R.id.frameLayout, new MyBooking_Fragment())
                                        .addToBackStack(null)
                                        .commit();

                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "เกิดข้อผิดพลาด", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("ยกเลิก", null)
                .show();
    }











    @Override public void onResume() { super.onResume(); if (mapView != null) mapView.onResume(); }
    @Override public void onPause() { if (mapView != null) mapView.onPause(); super.onPause(); }
    @Override public void onDestroy() { if (mapView != null) mapView.onDestroy(); super.onDestroy(); }
    @Override public void onLowMemory() { super.onLowMemory(); if (mapView != null) mapView.onLowMemory(); }
}
