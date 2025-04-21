package com.example.project_cbnew;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_cbnew.adapter.CarAdapter;
import com.example.project_cbnew.adapter.TripDayOverviewAdapter;
import com.example.project_cbnew.model.Booking;
import com.example.project_cbnew.model.Car;
import com.example.project_cbnew.model.Stop;
import com.example.project_cbnew.model.Trip;
import com.example.project_cbnew.model.TripDay;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Booking_Fragment extends Fragment {

    private static final String ARG_TRIP = "trip";
    private Trip trip;
    private String tripStart, tripEnd;

    // เพิ่ม global filter conditions
    private String selectedType = "";
    private int maxSeats = 13;

    private RecyclerView recyclerViewCars;
    private CarAdapter carAdapter;
    private List<Car> carList;
    private RadioGroup rgCarType;
    private EditText etSeats;
    private EditText etJourneyStart, etNote;
    private TextView tvStartDate, tvDistance, tvTripTitle, tvSelectedEndPlace;
    private Button btnNext;
    private Car selectedCar;
    private String selectedEndPlaceName = ""; // ใช้เก็บชื่อสถานที่สิ้นสุด

    public static Booking_Fragment newInstance(Trip trip) {
        Booking_Fragment fragment = new Booking_Fragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_TRIP, trip);
        fragment.setArguments(args);
        return fragment;
    }

    public Booking_Fragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            trip = getArguments().getParcelable(ARG_TRIP);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate layout
        View view = inflater.inflate(R.layout.fragment_booking, container, false);

        // 1) Bind all views into class fields (ห้ามประกาศซ้ำด้วยชนิดท้องถิ่น)
        recyclerViewCars    = view.findViewById(R.id.recyclerViewCars);
        RecyclerView rvTripOverview = view.findViewById(R.id.rvTripOverview);
        rgCarType           = view.findViewById(R.id.rgCarType);
        etSeats             = view.findViewById(R.id.etSeats);
        etNote              = view.findViewById(R.id.etNote);
        tvTripTitle         = view.findViewById(R.id.tvTripTitle);
        TextView tvJourneyStart  = view.findViewById(R.id.tvJourneyStart);
        TextView tvJourneyEnd    = view.findViewById(R.id.tvJourneyEnd);
        tvStartDate         = view.findViewById(R.id.tvStartDate);
        TextView tvDepartureTime = view.findViewById(R.id.tvDepartureTime);
        tvDistance          = view.findViewById(R.id.tvDistance);
        btnNext             = view.findViewById(R.id.btnNext);
        ImageButton btnBack  = view.findViewById(R.id.btnBack);

        // 2) GeoApiContext (reuse API key)
        GeoApiContext gctx = new GeoApiContext.Builder()
                .apiKey("AIzaSyArFeH3SFY8MUXxuoRjr4i-T5LQkn5j6qo")
                .build();

        // 3) Back button
        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                requireActivity().onBackPressed();
            }
        });

        // 4) ถ้า trip เป็น null ให้ซ่อน RV แล้วรีเทิร์น
        if (trip == null) {
            rvTripOverview.setVisibility(View.GONE);
            Toast.makeText(getContext(), "ไม่พบข้อมูลทริป", Toast.LENGTH_SHORT).show();
            return view;
        }

        // 5) แสดงชื่อทริป และ จุดเริ่ม/จุดสิ้นสุด
        tvTripTitle.setText("🚐 " + trip.getTitle());
        if (!trip.getTripDays().isEmpty()) {
            List<Stop> firstStops = trip.getTripDays().get(0).getStops();
            tvJourneyStart.setText(firstStops.isEmpty()
                    ? "🟢 จุดเริ่มเดินทาง: -"
                    : "🟢 จุดเริ่มเดินทาง: " + firstStops.get(0).getName());

            List<Stop> lastStops = trip.getTripDays().get(trip.getTripDays().size() - 1).getStops();
            tvJourneyEnd.setText(lastStops.isEmpty()
                    ? "🔴 จุดสิ้นสุด: -"
                    : "🔴 จุดสิ้นสุด: " + lastStops.get(lastStops.size() - 1).getName());
        }

        // 6) อ่านวันที่ + เวลาออกเดินทาง และคำนวณ tripEnd
        if (trip.getExtraFields() != null) {
            String rawDate = (String) trip.getExtraFields().get("T_StartDate");
            tripStart = convertDateToYMD(rawDate);
            // คำนวณ tripEnd
            try {
                Calendar cal = Calendar.getInstance();
                String[] p = tripStart.split("-");
                cal.set(Integer.parseInt(p[0]), Integer.parseInt(p[1]) - 1, Integer.parseInt(p[2]));
                cal.add(Calendar.DATE, trip.getTripDays().size() - 1);
                tripEnd = String.format("%04d-%02d-%02d",
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH) + 1,
                        cal.get(Calendar.DAY_OF_MONTH));
            } catch (Exception ignored) {}

            if (rawDate != null) {
                tvStartDate.setText("🗓️ วันที่เดินทาง: " + rawDate);
            }
            String rawTime = (String) trip.getExtraFields().get("T_DepartureTime");
            if (rawTime != null) {
                tvDepartureTime.setText("⏰ เวลาออกเดินทาง: " + rawTime);
            }
        }

        // 7) แสดงระยะทางรวม
        tvDistance.setText(String.format(Locale.getDefault(),
                "📍 ระยะทางรวม: %.1f กม.", (float) trip.getDistance()));

        // 8) เตรียม TripDayOverview RecyclerView + คำนวณ leg‑by‑leg
        TripDayOverviewAdapter dayAdapter = new TripDayOverviewAdapter(trip.getTripDays());
        rvTripOverview.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvTripOverview.setAdapter(dayAdapter);
        for (TripDay day : trip.getTripDays()) {
            calculateDayTravelAndRestTime(day, gctx, dayAdapter);
        }

        // 9) เตรียม RecyclerView รถ + filter controls
        carList   = new ArrayList<>();
        carAdapter = new CarAdapter(carList, car -> selectedCar = car);
        recyclerViewCars.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewCars.setAdapter(carAdapter);

        rgCarType.setOnCheckedChangeListener((group, checkedId) -> {
            selectedType = checkedId == R.id.rbVIP ? "VIP" : "ธรรมดา";
            filterCars();  // ตอนนี้ etSeats และ carList ไม่เป็น null แล้ว
        });
        etSeats.setOnFocusChangeListener((v, has) -> {
            if (!has) filterCars();
        });

        // 10) โหลด cars -> bookings -> กรองรถซ้อนกัน
        FirebaseFirestore.getInstance().collection("cars")
                .whereEqualTo("isAvailable", true)
                .get()
                .addOnSuccessListener(carsSnapshot -> {
                    carList.clear();
                    for (DocumentSnapshot doc : carsSnapshot.getDocuments()) {
                        // อ่าน Car
                        String id     = doc.getId();
                        String name   = doc.getString("name");
                        String type   = doc.getString("type");
                        int cap       = doc.getLong("capacity").intValue();
                        int pr        = doc.getLong("price").intValue();
                        String image  = doc.getString("imageUrl");
                        Boolean avail = doc.getBoolean("isAvailable");
                        if (name != null && type != null && image != null && avail != null) {
                            carList.add(new Car(id, name, type, cap, pr, image, avail));
                        }
                    }

                    FirebaseFirestore.getInstance().collection("bookings")
                            .get()
                            .addOnSuccessListener(bookSnap -> {
                                List<String> busyIds = new ArrayList<>();
                                if (tripStart != null && tripEnd != null) {
                                    for (DocumentSnapshot bdoc : bookSnap.getDocuments()) {
                                        String bStart = bdoc.getString("startDate");
                                        String bEnd   = bdoc.getString("endDate");
                                        String vanId  = bdoc.getString("vanId");
                                        if (vanId != null && bStart != null && bEnd != null
                                                && isDateOverlap(tripStart, tripEnd, bStart, bEnd)) {
                                            busyIds.add(vanId.trim().toLowerCase());
                                        }
                                    }
                                }
                                List<Car> filtered = new ArrayList<>();
                                for (Car c : carList) {
                                    if (!busyIds.contains(c.getId().trim().toLowerCase())) {
                                        filtered.add(c);
                                    }
                                }
                                carAdapter.setFilteredList(filtered);
                                carList.clear();
                                carList.addAll(filtered);
                                filterCars();
                            });
                });

        // 11) ปุ่ม Next -> สร้าง Booking -> ไปหน้า Confirm
        btnNext.setOnClickListener(v -> {
            if (selectedCar == null) {
                Toast.makeText(getContext(), "กรุณาเลือกรถ", Toast.LENGTH_SHORT).show();
                return;
            }

            String seatText = etSeats.getText().toString().trim();
            if (seatText.isEmpty() || rgCarType.getCheckedRadioButtonId() == -1) {
                Toast.makeText(getContext(), "กรุณาระบุประเภทรถและที่นั่ง", Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ ดึงจุดเริ่มต้น-สิ้นสุดจาก trip โดยตรง
            List<Stop> firstStops = trip.getTripDays().get(0).getStops();
            List<Stop> lastStops = trip.getTripDays().get(trip.getTripDays().size() - 1).getStops();

            String journeyStart = firstStops.isEmpty() ? "-" : firstStops.get(0).getName();
            String journeyEnd = lastStops.isEmpty() ? "-" : lastStops.get(lastStops.size() - 1).getName();

            // ✅ สร้าง Booking
            Booking booking = new Booking();
            booking.setVanId(selectedCar.getId());
            booking.setTypeVehicle(
                    rgCarType.getCheckedRadioButtonId() == R.id.rbVIP ? "VIP" : "ธรรมดา"
            );
            booking.setSeats(seatText);
            booking.setNote(etNote.getText().toString().trim());
            booking.setTripId(trip.getTripId());
            booking.setTripTitle(trip.getTitle());
            booking.setJourneyStart(journeyStart);
            booking.setJourneyEnd(journeyEnd);
            booking.setStartDate(tripStart);
            booking.setEndDate(tripEnd);
            booking.setTotalDistance(trip.getDistance());

            FirebaseAuth auth = FirebaseAuth.getInstance();
            if (auth.getCurrentUser() != null) {
                booking.setAccountId(auth.getCurrentUser().getUid());
            }

            // ✅ ไปหน้า Confirm
            Booking_Confirm_Fragment confirm =
                    Booking_Confirm_Fragment.newInstance(booking, selectedCar, trip);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frameLayout, confirm)
                    .addToBackStack(null)
                    .commit();
        });


        return view;
    }


    private void filterCars() {
        String seatStr = etSeats.getText().toString().trim();
        int inputSeats = seatStr.isEmpty() ? 0 : Integer.parseInt(seatStr);
        List<Car> filtered = new ArrayList<>();

        for (Car car : carList) {
            if (!car.isAvailable()) continue;
            if (!selectedType.isEmpty() && !car.getType().equals(selectedType)) continue;
            if (inputSeats > 0 && car.getCapacity() < inputSeats) continue;
            if (car.getCapacity() > maxSeats) continue;
            filtered.add(car);
        }

        carAdapter.setFilteredList(filtered);
    }

    private boolean isDateOverlap(String start1, String end1, String start2, String end2) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            Date s1 = sdf.parse(start1);
            Date e1 = sdf.parse(end1);
            Date s2 = sdf.parse(start2);
            Date e2 = sdf.parse(end2);

            // ไม่ซ้อนกันเมื่อช่วง A อยู่ก่อน B หรือ หลัง B
            return !(e1.before(s2) || s1.after(e2));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String convertDateToYMD(String dateStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateStr);
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return outputFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * คำนวณระยะทางและเวลาเดินทางของแต่ละ TripDay โดยไล่ request ทีละ leg (pair of stops)
     */
    private void calculateDayTravelAndRestTime(TripDay tripDay, GeoApiContext geoApiContext, TripDayOverviewAdapter adapter) {
        // เตรียมลิสต์พิกัดจาก Stop
        List<LatLng> points = new ArrayList<>();
        for (Stop stop : tripDay.getStops()) {
            if (stop.getLatitude() != null && stop.getLongitude() != null) {
                points.add(new LatLng(stop.getLatitude(), stop.getLongitude()));
            }
        }
        if (points.size() < 2) return;

        new Thread(() -> {
            try {
                double totalMeters = 0;
                double totalSeconds = 0;

                // วนทุกคู่จุดที่ติดกัน
                for (int i = 0; i < points.size() - 1; i++) {
                    com.google.maps.model.LatLng origin = new com.google.maps.model.LatLng(
                            points.get(i).latitude, points.get(i).longitude);
                    com.google.maps.model.LatLng dest = new com.google.maps.model.LatLng(
                            points.get(i+1).latitude, points.get(i+1).longitude);

                    DirectionsResult result = DirectionsApi.newRequest(geoApiContext)
                            .origin(origin)
                            .destination(dest)
                            .await();

                    for (DirectionsRoute route : result.routes) {
                        if (route.legs != null) {
                            for (var leg : route.legs) {
                                totalMeters  += leg.distance.inMeters;
                                totalSeconds += leg.duration.inSeconds;
                            }
                        }
                    }
                }

                // แปลงเป็น กม. และนาที
                int km           = (int)Math.ceil(totalMeters  / 1000.0);
                int travelMinute = (int)Math.ceil(totalSeconds / 60.0);

                // เซ็ตลง TripDay
                tripDay.setTotalDistanceKm(km);
                tripDay.setTotalTravelMinutes(travelMinute);

                // กลับมาที่ UI thread เพื่อรีเฟรช RecyclerView
                requireActivity().runOnUiThread(() -> {
                    adapter.notifyDataSetChanged();
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }





}
