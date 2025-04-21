package com.example.project_cbnew;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import java.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_cbnew.adapter.TripDayOverviewAdapter;
import com.example.project_cbnew.adapter.TripDayPreviewAdapter;
import com.example.project_cbnew.model.Stop;
import com.example.project_cbnew.model.TripDay;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Trip_Preview_Fragment extends Fragment {

    private CreateTripViewModel viewModel;
    private ProgressDialog progressDialog;

    public Trip_Preview_Fragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trip_preview, container, false);

        viewModel = new ViewModelProvider(requireActivity()).get(CreateTripViewModel.class);

        TextView tvTitle = view.findViewById(R.id.tvPreviewTitle);
        TextView tvDesc = view.findViewById(R.id.tvPreviewDescription);
        TextView tvDayCount = view.findViewById(R.id.tvPreviewDays);
        TextView tvStartDate = view.findViewById(R.id.tvPreviewStartDate);
        TextView tvStartLocation = view.findViewById(R.id.tvPreviewStartLocation);
        TextView tvTotalDistance = view.findViewById(R.id.tvPreviewTotalDistance);
        TextView tvTotalDuration = view.findViewById(R.id.tvPreviewTotalDuration);
        Button btnConfirmTrip = view.findViewById(R.id.btnConfirmTrip);
        ImageButton btnBack = view.findViewById(R.id.btnBack);
        RecyclerView rvTripDayPreview = view.findViewById(R.id.rvTripDayPreview);

        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        List<TripDay> tripDays = viewModel.getTripDays().getValue();
        final List<TripDay> finalTripDays = tripDays;
        final String title = viewModel.getTripTitle().getValue();
        final String desc = viewModel.getTripDescription().getValue();

        tvTitle.setText(title);
        tvDesc.setText(desc);
        tvDayCount.setText("จำนวนวัน: " + (tripDays != null ? tripDays.size() : 0));

        // ✅ Start Date
        Calendar startDate = viewModel.getStartDate().getValue();
        if (startDate != null) {
            String formatted = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(startDate.getTime());
            tvStartDate.setText("วันที่เริ่มเดินทาง: " + formatted);
        } else {
            tvStartDate.setText("วันที่เริ่มเดินทาง: -");
        }

        // ✅ Start Location
        String startLocationStr = "-";
        if (tripDays != null && !tripDays.isEmpty() && !tripDays.get(0).getStops().isEmpty()) {
            startLocationStr = tripDays.get(0).getStops().get(0).getName();
        }
        final String finalStartLocation = startLocationStr;
        tvStartLocation.setText("จุดเริ่มต้น: " + finalStartLocation);

        // ✅ Total Distance & Duration
        int distanceSum = 0;
        int restSum = 0;
        int travelSum = 0;

        if (tripDays != null) {
            for (TripDay day : tripDays) {
                distanceSum += day.getTotalDistanceKm();
                restSum += day.getTotalRestMinutes();
                travelSum += day.getTotalTravelMinutes();
            }
        }

        final int totalDistanceFinal = distanceSum;
        tvTotalDistance.setText("ระยะทางรวม: " + totalDistanceFinal + " กม.");
        int totalMinutes = restSum + travelSum;
        tvTotalDuration.setText(String.format("เวลารวม: %d ชม %d นาที", totalMinutes / 60, totalMinutes % 60));

        rvTripDayPreview.setLayoutManager(new LinearLayoutManager(requireContext()));
        TripDayOverviewAdapter adapter = new TripDayOverviewAdapter(tripDays);
        rvTripDayPreview.setAdapter(adapter);


        btnConfirmTrip.setOnClickListener(v -> {
            if (title == null || desc == null || finalTripDays == null || finalTripDays.isEmpty()) {
                Toast.makeText(getContext(), "กรุณากรอกข้อมูลให้ครบ", Toast.LENGTH_SHORT).show();
                return;
            }

            progressDialog = new ProgressDialog(getContext());
            progressDialog.setMessage("กำลังบันทึกทริป...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            FirebaseAuth auth = FirebaseAuth.getInstance();
            if (auth.getCurrentUser() == null) {
                Toast.makeText(getContext(), "กรุณาเข้าสู่ระบบก่อนบันทึก", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> tripData = new HashMap<>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Calendar startDateCal = viewModel.getStartDate().getValue();
            if (startDateCal != null) {
                String startDateStr = sdf.format(startDateCal.getTime());
                tripData.put("T_StartDate", startDateStr);
            }

            tripData.put("T_Name", title);
            tripData.put("T_Description", desc);
            tripData.put("T_Status", "draft");
            tripData.put("T_Recommended", false);
            tripData.put("createdBy", auth.getCurrentUser().getUid());
            tripData.put("T_Days", finalTripDays.size());
            tripData.put("T_StartLocation", finalStartLocation);
            tripData.put("T_Distance", totalDistanceFinal);

            // ✅ Summary by day
            List<Integer> dailyDistances = new ArrayList<>();
            List<Integer> dailyRestMinutes = new ArrayList<>();
            List<Integer> dailyTravelMinutes = new ArrayList<>();
            for (TripDay day : finalTripDays) {
                dailyDistances.add(day.getTotalDistanceKm());
                dailyRestMinutes.add(day.getTotalRestMinutes());
                dailyTravelMinutes.add(day.getTotalTravelMinutes());
            }
            tripData.put("T_DayDistances", dailyDistances);
            tripData.put("T_DayRestMinutes", dailyRestMinutes);
            tripData.put("T_DayDurations", dailyTravelMinutes);

            // ✅ Collect stops
            List<Stop> allStops = new ArrayList<>();
            List<Integer> stopToDayIndex = new ArrayList<>();
            for (int d = 0; d < finalTripDays.size(); d++) {
                TripDay day = finalTripDays.get(d);
                for (Stop stop : day.getStops()) {
                    allStops.add(stop);
                    stopToDayIndex.add(d);
                }
            }

            List<Map<String, Object>> places = new ArrayList<>();
            AtomicInteger uploadedCount = new AtomicInteger(0);

            for (int i = 0; i < allStops.size(); i++) {
                Stop stop = allStops.get(i);
                int index = i;
                int dayIndex = stopToDayIndex.get(index);

                if (stop.getPhotoBitmap() != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    stop.getPhotoBitmap().compress(Bitmap.CompressFormat.JPEG, 60, baos);
                    byte[] imageData = baos.toByteArray();

                    String filename = "trip_stops/" + System.currentTimeMillis() + "_" + index + ".jpg";
                    StorageReference ref = FirebaseStorage.getInstance().getReference(filename);

                    ref.putBytes(imageData)
                            .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                                Map<String, Object> stopMap = getStopMap(stop, uri.toString(), dayIndex);
                                places.add(stopMap);
                                checkIfAllUploadedAndSave(places, tripData, allStops.size());
                            }))
                            .addOnFailureListener(e -> {
                                Map<String, Object> stopMap = getStopMap(stop, null, dayIndex);
                                places.add(stopMap);
                                checkIfAllUploadedAndSave(places, tripData, allStops.size());
                            });

                } else if (stop.getImageUrl() != null && !stop.getImageUrl().isEmpty()) {
                    Map<String, Object> stopMap = getStopMap(stop, stop.getImageUrl(), dayIndex);
                    places.add(stopMap);
                    checkIfAllUploadedAndSave(places, tripData, allStops.size());

                } else {
                    Map<String, Object> stopMap = getStopMap(stop, null, dayIndex);
                    places.add(stopMap);
                    checkIfAllUploadedAndSave(places, tripData, allStops.size());
                }
            }
        });

        return view;
    }






    // ✅ เพิ่ม TP_Day
    private Map<String, Object> getStopMap(Stop stop, String photoUrl, int dayIndex) {
        Map<String, Object> map = new HashMap<>();
        map.put("TP_PlaceName", stop.getName());
        map.put("TP_Address", stop.getDescription());
        map.put("TP_Latitude", stop.getLatitude());
        map.put("TP_Longitude", stop.getLongitude());
        map.put("TP_DurationAtStop", stop.getDurationAtStop());
        map.put("TP_TravelTime", stop.getTravelTimeFromPrevious());
        map.put("TP_Day", dayIndex + 1); // ✅ เก็บเป็น 1-based index
        if (photoUrl != null) {
            map.put("TP_PhotoUrl", photoUrl);
        }
        return map;
    }

    private void checkIfAllUploadedAndSave(List<Map<String, Object>> places, Map<String, Object> tripData, int total) {
        if (places.size() == total) {
            tripData.put("Trip_Places", places);

            String tripId = viewModel.getTripId().getValue();  // ✅ ดึง tripId จาก ViewModel

            if (tripId != null && !tripId.isEmpty()) {
                // ✅ กรณี: แก้ไขทริปเดิม → ใช้ .set()
                FirebaseFirestore.getInstance().collection("trips")
                        .document(tripId)
                        .set(tripData)
                        .addOnSuccessListener(aVoid -> {
                            if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();

                            Toast.makeText(getContext(), "อัปเดตทริปเรียบร้อยแล้ว", Toast.LENGTH_SHORT).show();
                            viewModel.setTripId(null); // ✅ ล้าง tripId
                            viewModel.clear();         // ✅ ล้างข้อมูลใน ViewModel ทั้งหมด

                            requireActivity().getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.frameLayout, new Trip_Fragment())
                                    .commit();
                        })
                        .addOnFailureListener(e -> {
                            if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
                            Toast.makeText(getContext(), "อัปเดตล้มเหลว: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e("Trip_Preview", "Firebase error (update): ", e);
                        });

            } else {
                // ✅ กรณี: ทริปใหม่ → ใช้ .add()
                FirebaseFirestore.getInstance().collection("trips")
                        .add(tripData)
                        .addOnSuccessListener(doc -> {
                            if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();

                            Toast.makeText(getContext(), "บันทึกทริปเรียบร้อยแล้ว", Toast.LENGTH_SHORT).show();
                            viewModel.clear(); // ✅ เคลียร์ข้อมูลเมื่อสร้างทริปใหม่เสร็จ

                            requireActivity().getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.frameLayout, new Trip_Fragment())
                                    .commit();
                        })
                        .addOnFailureListener(e -> {
                            if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
                            Toast.makeText(getContext(), "บันทึกล้มเหลว: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e("Trip_Preview", "Firebase error (add): ", e);
                        });
            }
        }
    }


}
