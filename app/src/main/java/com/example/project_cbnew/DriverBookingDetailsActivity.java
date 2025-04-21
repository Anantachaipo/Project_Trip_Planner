package com.example.project_cbnew;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_cbnew.adapter.TripDayOverviewAdapter;
import com.example.project_cbnew.model.Booking;
import com.example.project_cbnew.model.Trip;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class DriverBookingDetailsActivity extends AppCompatActivity {

    private Booking booking;
    private Trip trip;

    private Button btnAccept, btnReject, btnComplete;
    private TextView tvTripTitle, tvStatus, tvDateRange, tvStartPoint, tvEndPoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_booking_details);

        booking = getIntent().getParcelableExtra("booking");
        trip = getIntent().getParcelableExtra("trip");

        findViewById(R.id.btnBackDriverDetail).setOnClickListener(v -> finish());

        tvTripTitle = findViewById(R.id.tvTripTitle);
        tvStatus = findViewById(R.id.tvBookingStatus);
        tvDateRange = findViewById(R.id.tvDateRange);
        tvStartPoint = findViewById(R.id.tvStartPoint);
        tvEndPoint = findViewById(R.id.tvEndPoint);

        btnAccept = findViewById(R.id.btnAcceptJob);
        btnReject = findViewById(R.id.btnRejectJob);
        btnComplete = findViewById(R.id.btnMarkComplete);

        if (booking != null) {
            tvTripTitle.setText("🚐 " + booking.getTripTitle());
            tvStatus.setText("สถานะ: " + booking.getStatus());
            tvDateRange.setText("📅 " + booking.getStartDate() + " - " + booking.getEndDate());
            tvStartPoint.setText("🟢 จุดเริ่มต้น: " + booking.getJourneyStart());
            tvEndPoint.setText("🔴 จุดสิ้นสุด: " + booking.getJourneyEnd());

            String status = booking.getStatus();

            btnAccept.setVisibility(View.GONE);
            btnReject.setVisibility(View.GONE);
            btnComplete.setVisibility(View.GONE);

            if ("รอคนขับตอบรับ".equals(status)) {
                btnAccept.setVisibility(View.VISIBLE);
                btnReject.setVisibility(View.VISIBLE);
            } else if ("รอเดินทาง/อยู่ระหว่างเดินทาง".equals(status)) {
                btnComplete.setVisibility(View.VISIBLE);
            }
        }

        btnAccept.setOnClickListener(v -> updateBookingStatus("รอเดินทาง/อยู่ระหว่างเดินทาง"));
        btnReject.setOnClickListener(v -> rejectBooking());
        btnComplete.setOnClickListener(v -> updateBookingStatus("เดินทางเสร็จสิ้น"));

        loadTripIfNeeded();
    }

    private void updateBookingStatus(String newStatus) {
        FirebaseFirestore.getInstance().collection("bookings")
                .document(booking.getBookingId())
                .update("status", newStatus)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "อัปเดตสถานะเรียบร้อย", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void rejectBooking() {
        FirebaseFirestore.getInstance().collection("bookings")
                .document(booking.getBookingId())
                .update("status", "รอคนขับ", "driverId", null)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "ปฏิเสธงานเรียบร้อย", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void loadTripIfNeeded() {
        if (trip != null && trip.getTripDays() != null) {
            setupRecyclerView();
        } else if (booking.getTripId() != null) {
            FirebaseFirestore.getInstance().collection("trips")
                    .document(booking.getTripId())
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            trip = Trip.fromBookingTripDoc(doc);  // ✅ ใช้ตัวช่วยแปลงครบ
                            setupRecyclerView();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "โหลดข้อมูลทริปล้มเหลว", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void setupRecyclerView() {
        RecyclerView rv = findViewById(R.id.rvTripDayOverview);
        rv.setLayoutManager(new LinearLayoutManager(this));
        if (trip != null && trip.getTripDays() != null) {
            rv.setAdapter(new TripDayOverviewAdapter(trip.getTripDays()));
        }
    }
}
