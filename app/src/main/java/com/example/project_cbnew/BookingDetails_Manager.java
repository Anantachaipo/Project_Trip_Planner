package com.example.project_cbnew;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_cbnew.adapter.TripDayOverviewAdapter;
import com.example.project_cbnew.hardcodetrip.HardcodedTripProvider;
import com.example.project_cbnew.model.Booking;
import com.example.project_cbnew.model.Trip;
import com.example.project_cbnew.model.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class BookingDetails_Manager extends AppCompatActivity {

    private static final String TAG = "BookingDetails_Manager";

    private Booking booking;
    private TextView tvTripTitle, tvDateRange, tvStart, tvEnd, tvSeats, tvVehicle, tvDistance, tvStatus, tvNote;
    private TextView tvCustomerName, tvCustomerEmail, tvCustomerTel;
    private RecyclerView rvTripOverview;
    private Button btnApprove, btnReject, btnAssignDriver;
    private ImageButton btnBack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_details_manager);

        booking = getIntent().getParcelableExtra("booking");

        tvTripTitle = findViewById(R.id.tvTripName);
        tvDateRange = findViewById(R.id.tvDateRange);
        tvStart = findViewById(R.id.tvStart);
        tvEnd = findViewById(R.id.tvEnd);
        tvSeats = findViewById(R.id.tvSeats);
        tvVehicle = findViewById(R.id.tvType);
        tvDistance = findViewById(R.id.tvDistance);
        tvStatus = findViewById(R.id.tvBookingStatus);
        tvNote = findViewById(R.id.tvNote);
        btnApprove = findViewById(R.id.btnApprove);
        btnReject = findViewById(R.id.btnReject);
        btnAssignDriver = findViewById(R.id.btnAssignDriver);
        rvTripOverview = findViewById(R.id.rvTripOverview);
        btnBack = findViewById(R.id.btnBack);

        tvCustomerName = findViewById(R.id.tvCustomerName);
        tvCustomerEmail = findViewById(R.id.tvCustomerEmail);
        tvCustomerTel = findViewById(R.id.tvCustomerTel);

        btnBack.setOnClickListener(v -> finish());

        rvTripOverview.setLayoutManager(new LinearLayoutManager(this));

        if (booking != null) {
            showBookingDetails();
            loadTripOverview();
            loadCustomerInfo(booking.getAccountId());
        }

        if (!"รอตรวจสอบ".equals(booking.getStatus())) {
            btnApprove.setVisibility(View.GONE);
            btnReject.setVisibility(View.GONE);
        }

        if ("รอคนขับ".equals(booking.getStatus())) {
            btnAssignDriver.setVisibility(View.VISIBLE);
            btnAssignDriver.setOnClickListener(v -> {
                Intent intent = new Intent(this, AssignDriverActivity.class);
                intent.putExtra("booking", booking);
                startActivity(intent);
            });
        } else {
            btnAssignDriver.setVisibility(View.GONE);
        }

        btnApprove.setOnClickListener(v -> showConfirmationDialog(true));
        btnReject.setOnClickListener(v -> showConfirmationDialog(false));
    }

    private void showBookingDetails() {
        tvTripTitle.setText("🚐 ทริป: " + booking.getTripTitle());
        tvDateRange.setText("🗓 " + booking.getStartDate() + " - " + booking.getEndDate());
        tvStart.setText("🟢 จุดเริ่มต้น: " + booking.getJourneyStart());
        tvEnd.setText("🔴 จุดสิ้นสุด: " + booking.getJourneyEnd());
        tvSeats.setText("👥 จำนวนที่นั่ง: " + booking.getSeats());
        tvVehicle.setText("🚐 ประเภทรถ: " + booking.getTypeVehicle());
        tvDistance.setText("📏 ระยะทาง: " + booking.getTotalDistance() + " กม.");
        tvStatus.setText("📌 สถานะ: " + booking.getStatus());
        tvNote.setText("📝 หมายเหตุ: " + (booking.getNote() == null || booking.getNote().isEmpty() ? "-" : booking.getNote()));
    }

    private void loadCustomerInfo(String accountId) {
        Log.d(TAG, "🔍 Looking for customer accountId = " + accountId);
        FirebaseFirestore.getInstance().collection("customer")
                .document(accountId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        User user = doc.toObject(User.class);
                        if (user != null) {
                            tvCustomerName.setText("👤 ผู้จอง: " + user.getName());
                            tvCustomerEmail.setText("📧 อีเมล: " + user.getEmail());
                            tvCustomerTel.setText("📞 โทร: " + user.getTel());
                        } else {
                            Log.w(TAG, "⚠️ user object is null");
                        }
                    } else {
                        Log.w(TAG, "⚠️ Document does not exist for ID: " + accountId);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "❌ Failed to load customer info", e));
    }

    private void showConfirmationDialog(boolean isApprove) {
        String message = isApprove ? "ยืนยันการอนุมัติรายการจองนี้?" : "คุณแน่ใจว่าจะปฏิเสธรายการจองนี้?";
        new AlertDialog.Builder(this)
                .setTitle(isApprove ? "อนุมัติการจอง" : "ปฏิเสธการจอง")
                .setMessage(message)
                .setPositiveButton("ยืนยัน", (dialog, which) -> {
                    updateBookingStatus(isApprove ? "รอชำระเงิน" : "ปฏิเสธการจอง");
                })
                .setNegativeButton("ยกเลิก", null)
                .show();
    }

    private void updateBookingStatus(String newStatus) {
        if (booking == null || booking.getBookingId() == null) return;

        FirebaseFirestore.getInstance().collection("bookings")
                .document(booking.getBookingId())
                .update("status", newStatus)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "อัปเดตสถานะเป็น: " + newStatus, Toast.LENGTH_SHORT).show();
                    booking.setStatus(newStatus);
                    tvStatus.setText("📌 สถานะ: " + newStatus);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "เกิดข้อผิดพลาดในการอัปเดตสถานะ", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadTripOverview() {
        if (booking.getTripId() != null) {
            FirebaseFirestore.getInstance().collection("trips")
                    .document(booking.getTripId())
                    .get()
                    .addOnSuccessListener(doc -> {
                        Trip trip = Trip.fromBookingTripDoc(doc);
                        if (trip != null && trip.getTripDays() != null) {
                            TripDayOverviewAdapter adapter = new TripDayOverviewAdapter(trip.getTripDays());
                            rvTripOverview.setAdapter(adapter);
                        }
                    });
        } else {
            List<Trip> hardcodedTrips = HardcodedTripProvider.getTrips(this);
            for (Trip trip : hardcodedTrips) {
                if (trip.getTitle().equals(booking.getTripTitle())) {
                    TripDayOverviewAdapter adapter = new TripDayOverviewAdapter(trip.getTripDays());
                    rvTripOverview.setAdapter(adapter);
                    break;
                }
            }
        }
    }
}
