package com.example.project_cbnew;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_cbnew.adapter.StopImageSliderAdapter;
import com.example.project_cbnew.adapter.TripDayOverviewAdapter;
import com.example.project_cbnew.model.Stop;
import com.example.project_cbnew.model.Trip;
import com.example.project_cbnew.model.TripDay;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;


import java.text.SimpleDateFormat;
import java.util.*;

public class TripDetails_Fragment extends Fragment {
    private final GeoApiContext geoApiContext = new GeoApiContext.Builder()
            .apiKey("AIzaSyArFeH3SFY8MUXxuoRjr4i-T5LQkn5j6qo") // <-- อย่าลืมใส่ API key
            .build();


    private static final String ARG_TRIP = "trip";

    // ✅ พิกัดบริษัท: 58/554 Ram Inthra Rd
    private static final LatLng COMPANY_LOCATION = new LatLng(13.855969, 100.647614);

    private Trip trip;
    private RecyclerView recyclerViewDays;
    private TripDayOverviewAdapter dayOverviewAdapter;
    private boolean isStartLocationSelected = false;


    public static TripDetails_Fragment newInstance(Trip trip) {
        TripDetails_Fragment fragment = new TripDetails_Fragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_TRIP, trip);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            trip = getArguments().getParcelable(ARG_TRIP);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 1. ดึง currentUserId
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String currentUserId = auth.getCurrentUser() != null
                ? auth.getCurrentUser().getUid()
                : null;

        // Inflate layout
        View view = inflater.inflate(R.layout.fragment_trip_details, container, false);

        // 3. Bind views
        TextView tripTitle         = view.findViewById(R.id.tripTitleDetails);
        TextView tripDescription   = view.findViewById(R.id.tripDescription);
        TextView tripStartLocation = view.findViewById(R.id.tripStartLocation);
        TextView tripEndLocation   = view.findViewById(R.id.tripEndLocation);
        TextView tripTypeLabel     = view.findViewById(R.id.tripTypeLabel);
        TextView tripStartDate     = view.findViewById(R.id.tripStartDate);
        EditText tripDate          = view.findViewById(R.id.tripDate);
        EditText etDepartureTime   = view.findViewById(R.id.etDepartureTime);
        TextView tripDepartureTime = view.findViewById(R.id.tripDepartureTime);
        TextView tripDistance      = view.findViewById(R.id.tripDistance);
        TextView tripPrice         = view.findViewById(R.id.tripPrice);

        // — Image slider setup —
        RecyclerView imageSliderRecycler = view.findViewById(R.id.recyclerViewTripImages);
        imageSliderRecycler.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        imageSliderRecycler.setHasFixedSize(false);
        imageSliderRecycler.setClipToPadding(false);
        int pad = getResources().getDimensionPixelSize(R.dimen.back_button_size);
        imageSliderRecycler.setPadding(pad, 0, 0, 0);

        recyclerViewDays = view.findViewById(R.id.recyclerViewStops);
        recyclerViewDays.setLayoutManager(new LinearLayoutManager(requireContext()));

        // — Back button —
        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                requireActivity().onBackPressed();
            }
        });

        // 5. Date picker
        tripDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(requireContext(),
                    (d, y, m, day)-> {
                        c.set(y, m, day);
                        String sel = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                                .format(c.getTime());
                        tripDate.setText(sel);
                        tripStartDate.setText("วันที่เริ่มเดินทาง: " + sel);
                        if (trip.getExtraFields()==null) trip.setExtraFields(new HashMap<>());
                        trip.getExtraFields().put("T_StartDate", sel);
                    },
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        // 6. Time picker
        etDepartureTime.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new TimePickerDialog(requireContext(),
                    (d, h, min)-> {
                        String ts = String.format(Locale.getDefault(), "%02d:%02d", h, min);
                        etDepartureTime.setText(ts);
                        tripDepartureTime.setText("เวลาออกเดินทาง: " + ts);
                        if (trip.getExtraFields()==null) trip.setExtraFields(new HashMap<>());
                        trip.getExtraFields().put("T_DepartureTime", ts);
                    },
                    c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true
            ).show();
        });

        // 7. เตรียม Autocomplete start
        AutocompleteSupportFragment autoStart =
                (AutocompleteSupportFragment) getChildFragmentManager()
                        .findFragmentById(R.id.autocompleteStartLocation);
        if (autoStart != null) {
            autoStart.setPlaceFields(Arrays.asList(
                    Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG
            ));
            autoStart.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    Stop s = new Stop(
                            place.getName(),
                            place.getAddress(),
                            place.getLatLng().latitude,
                            place.getLatLng().longitude
                    );
                    // แทรกเป็น stop แรกของวันแรก
                    TripDay firstDay = trip.getTripDays().get(0);
                    List<Stop> list = new ArrayList<>(firstDay.getStops());
                    list.add(0, s);
                    firstDay.setStops(list);

                    tripStartLocation.setText("จุดเริ่มต้น: " + place.getName());
                    isStartLocationSelected = true;
                }
                @Override
                public void onError(@NonNull Status status) {
                    Toast.makeText(getContext(),
                            "เลือกจุดเริ่มต้นไม่สำเร็จ", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // 8. เตรียม Autocomplete end
        AutocompleteSupportFragment autoEnd =
                (AutocompleteSupportFragment) getChildFragmentManager()
                        .findFragmentById(R.id.autocompleteEndLocation);
        if (autoEnd != null) {
            autoEnd.setPlaceFields(Arrays.asList(
                    Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG
            ));
            autoEnd.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    Stop s = new Stop(
                            place.getName(),
                            place.getAddress(),
                            place.getLatLng().latitude,
                            place.getLatLng().longitude
                    );
                    TripDay lastDay = trip.getTripDays()
                            .get(trip.getTripDays().size() - 1);
                    List<Stop> list = new ArrayList<>(lastDay.getStops());
                    list.add(s);
                    lastDay.setStops(list);

                    tripEndLocation.setText("จุดสิ้นสุด: " + place.getName());
                }
                @Override
                public void onError(@NonNull Status status) {
                    Toast.makeText(getContext(),
                            "เลือกจุดสิ้นสุดไม่สำเร็จ", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // 9. กรณี trip!=null กำหนด UI เบื้องต้น
        if (trip != null) {
            tripTitle.setText(trip.getTitle());
            tripDescription.setText(trip.getDescription());

            // image slider
            StopImageSliderAdapter slider = new StopImageSliderAdapter(flattenAllStops(trip.getTripDays()));
            imageSliderRecycler.setAdapter(slider);
            imageSliderRecycler.scrollToPosition(0);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                imageSliderRecycler.scrollToPosition(0);
                imageSliderRecycler.invalidate();
            }, 200);

            // overview list
            dayOverviewAdapter = new TripDayOverviewAdapter(trip.getTripDays());
            recyclerViewDays.setAdapter(dayOverviewAdapter);

            // สลับ โชว์/ซ่อน Autocomplete ตามประเภททริป
            if (trip.getCreatedBy() == null) {
                // ทริปแนะนำ ให้เลือกเองทั้งสองจุด
                tripTypeLabel.setText("ทริปแนะนำ");
                tripTypeLabel.setBackgroundTintList(
                        ContextCompat.getColorStateList(requireContext(), R.color.orange)
                );
                autoStart.getView().setVisibility(View.VISIBLE);
                autoEnd .getView().setVisibility(View.VISIBLE);
            } else {
                // ทริปของฉัน ซ่อนทั้งสอง แล้วดึง stop แรก/สุดท้ายมาแสดง
                tripTypeLabel.setText("ทริปของฉัน");
                tripTypeLabel.setBackgroundTintList(
                        ContextCompat.getColorStateList(requireContext(), R.color.blue_accent)
                );
                autoStart.getView().setVisibility(View.GONE);
                autoEnd .getView().setVisibility(View.GONE);

                // ดึง stop แรก
                Stop first = trip.getStops().get(0);
                tripStartLocation.setText("จุดเริ่มต้น: " + first.getName());
                isStartLocationSelected = true;

                // ดึง stop สุดท้าย
                Stop last = trip.getStops().get(trip.getStops().size() - 1);
                tripEndLocation  .setText("จุดสิ้นสุด: " + last.getName());
            }

            // 10. pre-fill วันที่/เวลา ถ้ามี
            if (trip.getExtraFields() != null) {
                if (trip.getExtraFields().containsKey("T_StartDate")) {
                    tripStartDate.setText("วันที่เริ่มเดินทาง: " +
                            trip.getExtraFields().get("T_StartDate"));
                }
                if (trip.getExtraFields().containsKey("T_DepartureTime")) {
                    tripDepartureTime.setText("เวลาออกเดินทาง: " +
                            trip.getExtraFields().get("T_DepartureTime"));
                }
            }

            // 11. คำนวณระยะทาง & ราคา
            List<LatLng> pts = new ArrayList<>();
            pts.add(COMPANY_LOCATION);
            for (TripDay d : trip.getTripDays()) {
                for (Stop s : d.getStops()) {
                    pts.add(new LatLng(s.getLatitude(), s.getLongitude()));
                }
            }
            calculateRealDistanceWithDirections(pts, tripDistance, tripPrice);

            // 12. ปุ่มจอง
            Button btnBookTrip = view.findViewById(R.id.btnBookTrip);
            btnBookTrip.setOnClickListener(v -> {
                if (trip.getCreatedBy()==null && !isStartLocationSelected) {
                    Toast.makeText(getContext(),
                            "กรุณาเลือกจุดเริ่มต้นก่อนจอง", Toast.LENGTH_SHORT).show();
                    return;
                }
                Booking_Fragment bf = Booking_Fragment.newInstance(trip);
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frameLayout, bf)
                        .addToBackStack(null)
                        .commit();
            });

            // ปุ่มแก้ไข/ลบ
            Button btnEdit = view.findViewById(R.id.btnEditTrip);
            Button btnDel  = view.findViewById(R.id.btnDeleteTrip);

            if (currentUserId != null && currentUserId.equals(trip.getCreatedBy())) {
                btnEdit.setVisibility(View.VISIBLE);
                btnDel.setVisibility(View.VISIBLE);

                // ✅ Listener แก้ไข
                btnEdit.setOnClickListener(v -> {
                    CreateTripViewModel viewModel = new ViewModelProvider(requireActivity()).get(CreateTripViewModel.class);
                    viewModel.setTripId(trip.getTripId()); // ✅ บรรทัดสำคัญ!
                    viewModel.setTripTitle(trip.getTitle());
                    viewModel.setTripDescription(trip.getDescription());
                    viewModel.setTripDays(trip.getTripDays());

                    Create_Trip_Fragment editFragment = Create_Trip_Fragment.newInstance(trip);
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.frameLayout, editFragment)
                            .addToBackStack(null)
                            .commit();
                });

                // ✅ Listener ลบ
                btnDel.setOnClickListener(v -> {
                    new AlertDialog.Builder(requireContext())
                            .setTitle("ลบทริป")
                            .setMessage("คุณแน่ใจหรือไม่ว่าต้องการลบทริปนี้?")
                            .setPositiveButton("ลบ", (dialog, which) -> {
                                FirebaseFirestore.getInstance().collection("trips")
                                        .document(trip.getTripId())
                                        .delete()
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(getContext(), "ลบทริปสำเร็จ", Toast.LENGTH_SHORT).show();
                                            requireActivity().getSupportFragmentManager().popBackStack();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(getContext(), "ลบทริปไม่สำเร็จ", Toast.LENGTH_SHORT).show();
                                            Log.e("TripDetails", "Delete failed", e);
                                        });
                            })
                            .setNegativeButton("ยกเลิก", null)
                            .show();
                });

            } else {
                btnEdit.setVisibility(View.GONE);
                btnDel.setVisibility(View.GONE);
            }



        }




        return view;
    }





    private void calculateRealDistanceWithDirections(List<LatLng> points, TextView tvDistance, TextView tvPrice) {
        if (points.size() < 2) {
            tvDistance.setText("กรุณาเพิ่มจุดแวะอย่างน้อย 2 จุด");
            tvPrice.setText("ราคา: -");
            return;
        }

        new Thread(() -> {
            try {
                GeoApiContext geoApiContext = new GeoApiContext.Builder()
                        .apiKey("AIzaSyArFeH3SFY8MUXxuoRjr4i-T5LQkn5j6qo") // 🔐 แทนที่ด้วย API Key จริง
                        .build();

                com.google.maps.model.LatLng origin = new com.google.maps.model.LatLng(
                        points.get(0).latitude, points.get(0).longitude
                );

                com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                        points.get(points.size() - 1).latitude, points.get(points.size() - 1).longitude
                );

                com.google.maps.model.LatLng[] waypoints = new com.google.maps.model.LatLng[points.size() - 2];
                for (int i = 1; i < points.size() - 1; i++) {
                    waypoints[i - 1] = new com.google.maps.model.LatLng(
                            points.get(i).latitude, points.get(i).longitude
                    );
                }

                DirectionsResult result = DirectionsApi.newRequest(geoApiContext)
                        .origin(origin)
                        .destination(destination)
                        .waypoints(waypoints)
                        .await();

                double totalMeters = 0;
                for (DirectionsRoute route : result.routes) {
                    if (route.legs != null) {
                        for (var leg : route.legs) {
                            totalMeters += leg.distance.inMeters;
                        }
                    }
                }

                double totalKm = totalMeters / 1000.0;
                trip.setDistance((int) totalKm); // ✅ เพิ่มบรรทัดนี้ เพื่อให้ Booking_Fragment ดึงค่าใหม่ได้
                int tripDays = trip.getTripDays().size();
                int vehicleCost = tripDays * 1800;
                int price = (int) Math.ceil(totalKm * 3.5) + vehicleCost;

                requireActivity().runOnUiThread(() -> {
                    tvDistance.setText(String.format(Locale.getDefault(), "ระยะทางโดยประมาณ: %.1f กม.", totalKm));
                    tvPrice.setText("ราคาโดยประมาณ: " + price + " บาท (รวมค่ารถ " + vehicleCost + " บาท)");
                });

            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> {
                    tvDistance.setText("เกิดข้อผิดพลาดในการคำนวณ");
                    tvPrice.setText("ราคา: -");
                });
            }
        }).start();
    }

    private void calculateDayTravelAndRestTime(TripDay tripDay) {
        List<LatLng> points = new ArrayList<>();
        for (Stop stop : tripDay.getStops()) {
            if (stop.getLatitude() != null && stop.getLongitude() != null) {
                points.add(new LatLng(stop.getLatitude(), stop.getLongitude()));
            }
        }

        if (points.size() < 2) return;

        new Thread(() -> {
            try {
                com.google.maps.model.LatLng origin = new com.google.maps.model.LatLng(points.get(0).latitude, points.get(0).longitude);
                com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(points.get(points.size() - 1).latitude, points.get(points.size() - 1).longitude);

                com.google.maps.model.LatLng[] waypoints = new com.google.maps.model.LatLng[points.size() - 2];
                for (int i = 1; i < points.size() - 1; i++) {
                    waypoints[i - 1] = new com.google.maps.model.LatLng(points.get(i).latitude, points.get(i).longitude);
                }

                DirectionsResult result = DirectionsApi.newRequest(geoApiContext)
                        .origin(origin)
                        .destination(destination)
                        .waypoints(waypoints)
                        .await();

                double totalMeters = 0;
                double totalSeconds = 0;

                for (DirectionsRoute route : result.routes) {
                    if (route.legs != null) {
                        for (com.google.maps.model.DirectionsLeg leg : route.legs) {
                            totalMeters += leg.distance.inMeters;
                            totalSeconds += leg.duration.inSeconds;
                        }
                    }
                }

                int totalKm = (int) Math.ceil(totalMeters / 1000.0);
                int totalTravelMinutes = (int) Math.ceil(totalSeconds / 60.0);

                tripDay.setTotalDistanceKm(totalKm);
                tripDay.setTotalTravelMinutes(totalTravelMinutes);

                requireActivity().runOnUiThread(() -> {
                    if (dayOverviewAdapter != null) {
                        dayOverviewAdapter.notifyDataSetChanged();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private List<Stop> flattenAllStops(List<TripDay> tripDays) {
        List<Stop> result = new ArrayList<>();
        for (TripDay day : tripDays) {
            if (day.getStops() != null) {
                result.addAll(day.getStops());
            }
        }
        return result;
    }





}
