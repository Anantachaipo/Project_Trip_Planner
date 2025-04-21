package com.example.project_cbnew;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_cbnew.adapter.DriverReviewAdapter;
import com.example.project_cbnew.model.Booking;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Driver_Review_Activity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DriverReviewAdapter adapter;
    private List<Booking> reviews = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_review);

        ImageButton btnBack = findViewById(R.id.btnBackReview);
        btnBack.setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.rvReviews);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DriverReviewAdapter(reviews);
        recyclerView.setAdapter(adapter);

        loadReviews();
    }

    private void loadReviews() {
        String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance()
                .collection("bookings")
                .whereEqualTo("driverId", driverId)
                .whereEqualTo("status", "เดินทางเสร็จสิ้น")
                .get()
                .addOnSuccessListener(query -> {
                    reviews.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        Booking booking = doc.toObject(Booking.class);
                        if (booking.getDriverRating() > 0) {
                            reviews.add(booking);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "โหลดรีวิวล้มเหลว", Toast.LENGTH_SHORT).show();
                });
    }
}
