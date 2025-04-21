package com.example.project_cbnew;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_cbnew.adapter.BookingAdapter;
import com.example.project_cbnew.model.Booking;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class View_Assign_Driver extends AppCompatActivity {

    private RecyclerView rvAssigned;
    private BookingAdapter adapter;
    private List<Booking> allBookings = new ArrayList<>();
    private Spinner sortSpinner;
    private String currentUserId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_assign_driver);

        rvAssigned = findViewById(R.id.recyclerViewAssigned);
        sortSpinner = findViewById(R.id.spinnerSort);
        ImageButton btnBack = findViewById(R.id.btnBackAssigned);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();
            Log.d("AUTH_DEBUG", "🔐 Firebase UID = " + currentUserId);
        } else {
            Toast.makeText(this, "กรุณาเข้าสู่ระบบใหม่", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        rvAssigned.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookingAdapter(allBookings, booking -> {
            Intent intent = new Intent(this, DriverBookingDetailsActivity.class);
            intent.putExtra("booking", booking);
            startActivity(intent);
        });
        rvAssigned.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        setupSpinner();
        loadAssignedBookings();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAssignedBookings(); // 🔄 โหลดข้อมูลใหม่ทุกครั้งที่กลับมา
    }


    private void setupSpinner() {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item,
                new String[]{"ใหม่สุด", "เก่าสุด"}
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(spinnerAdapter);

        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                sortBookings(pos == 0);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadAssignedBookings() {
        FirebaseFirestore.getInstance().collection("bookings")
                .whereEqualTo("driverId", currentUserId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    allBookings.clear();
                    Log.d("BOOKING_DEBUG", "📦 พบ booking ทั้งหมด: " + snapshot.size());

                    for (QueryDocumentSnapshot doc : snapshot) {
                        Booking booking = doc.toObject(Booking.class);
                        booking.setBookingId(doc.getId());
                        Log.d("BOOKING_DEBUG", "📄 bookingId = " + booking.getBookingId()
                                + ", title = " + booking.getTripTitle()
                                + ", status = " + booking.getStatus()
                                + ", driverId = " + booking.getDriverId());
                        if (!"เดินทางเสร็จสิ้น".equals(booking.getStatus())) {
                            allBookings.add(booking);
                        }

                    }

                    sortBookings(true); // ✅ เรียกตรงนี้เท่านั้น
                });
    }

    private void sortBookings(boolean newestFirst) {
        Collections.sort(allBookings, (a, b) -> {
            String d1 = a.getStartDate();
            String d2 = b.getStartDate();
            return newestFirst ? d2.compareTo(d1) : d1.compareTo(d2);
        });
        adapter.setBookings(allBookings);
    }
}
