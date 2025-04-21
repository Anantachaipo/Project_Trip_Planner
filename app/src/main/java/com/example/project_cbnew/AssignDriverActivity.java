package com.example.project_cbnew;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_cbnew.adapter.DriverAdapter;
import com.example.project_cbnew.model.Booking;
import com.example.project_cbnew.model.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AssignDriverActivity extends AppCompatActivity {

    private RecyclerView rvDrivers;
    private DriverAdapter adapter;
    private List<User> driverList = new ArrayList<>();
    private Booking booking;
    private List<Booking> allBookings = new ArrayList<>();
    private ImageButton btnBack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign_driver);

        booking = getIntent().getParcelableExtra("booking");
        rvDrivers = findViewById(R.id.recyclerViewDrivers);
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        rvDrivers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DriverAdapter(this, driverList, this::assignDriverToBooking, true); // แสดงปุ่ม

        rvDrivers.setAdapter(adapter);

        loadAllBookings();
    }

    private void loadAllBookings() {
        FirebaseFirestore.getInstance().collection("bookings")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    allBookings.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Booking b = doc.toObject(Booking.class);
                        b.setBookingId(doc.getId());
                        allBookings.add(b);
                    }
                    loadAvailableDrivers();
                });
    }

    private void loadAvailableDrivers() {
        FirebaseFirestore.getInstance().collection("customer")
                .whereEqualTo("role", "driver")
                .whereEqualTo("status", "ว่าง")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    driverList.clear();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        User driver = doc.toObject(User.class);
                        driver.setUserId(doc.getId());

                        // เงื่อนไข: ว่าง และไม่ซ้อนวัน
                        if ("ว่าง".equals(driver.getStatus()) && !isDriverBusy(driver)) {
                            driverList.add(driver);
                        }
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "เกิดข้อผิดพลาดในการโหลดคนขับ", Toast.LENGTH_SHORT).show());
    }

    private boolean isDriverBusy(User driver) {
        for (Booking b : allBookings) {
            if (driver.getUserId().equals(b.getDriverId()) &&
                    isDateOverlap(booking.getStartDate(), booking.getEndDate(), b.getStartDate(), b.getEndDate())) {
                return true; // 🚫 ซ้อนวัน
            }
        }
        return false;
    }

    private boolean isDateOverlap(String start1, String end1, String start2, String end2) {
        return start1.compareTo(end2) <= 0 && end1.compareTo(start2) >= 0;
    }

    private void assignDriverToBooking(User driver) {
        FirebaseFirestore.getInstance().collection("bookings")
                .document(booking.getBookingId())
                .update("driverId", driver.getUserId(), "status", "รอคนขับตอบรับ")
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "ส่งงานให้คนขับเรียบร้อย", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AssignDriverActivity.this, View_All_Booking_Manager.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // ✅ ป้องกันย้อนกลับซ้ำ
                    startActivity(intent);
                    finish(); // ปิด AssignDriverActivity
                })
                .addOnFailureListener(e -> Toast.makeText(this, "เกิดข้อผิดพลาด", Toast.LENGTH_SHORT).show());
    }

}

