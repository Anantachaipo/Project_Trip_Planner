package com.example.project_cbnew;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_cbnew.adapter.BookingAdapter;
import com.example.project_cbnew.model.Booking;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Driver_Completed_Jobs_Activity extends AppCompatActivity {

    private RecyclerView rvCompletedJobs;
    private BookingAdapter adapter;
    private List<Booking> completedBookings = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_completed_jobs);

        rvCompletedJobs = findViewById(R.id.rvCompletedJobs);
        rvCompletedJobs.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookingAdapter(completedBookings, booking -> {
            // คลิกแต่ละรายการ (ถ้าต้องการ)
        });
        rvCompletedJobs.setAdapter(adapter);

        ImageButton btnBack = findViewById(R.id.btnBackCompleted);
        btnBack.setOnClickListener(v -> finish());

        loadCompletedJobs();
    }

    private void loadCompletedJobs() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("bookings")
                .whereEqualTo("driverId", currentUserId)
                .whereEqualTo("status", "เดินทางเสร็จสิ้น")
                .get()
                .addOnSuccessListener(query -> {
                    completedBookings.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        Booking booking = doc.toObject(Booking.class);
                        completedBookings.add(booking);
                    }
                    adapter.setBookings(completedBookings);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "โหลดข้อมูลล้มเหลว", Toast.LENGTH_SHORT).show());
    }
}
