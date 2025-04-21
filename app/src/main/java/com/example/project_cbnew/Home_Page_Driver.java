package com.example.project_cbnew;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class Home_Page_Driver extends AppCompatActivity {

    private TextView tvRatingSummary, tvTotalJobs;
    private Button btnViewAssigned, btnToggleStatus;
    private RatingBar ratingBar;
    private String currentUserId;
    private boolean isAvailable = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("DEBUG_UID", "🔐 UID = " + FirebaseAuth.getInstance().getUid());

        setContentView(R.layout.activity_home_page_driver);

        tvRatingSummary = findViewById(R.id.tvRatingSummary);
        tvTotalJobs = findViewById(R.id.tvTotalJobs);
        ratingBar = findViewById(R.id.driverRatingBar);
        btnViewAssigned = findViewById(R.id.btnViewAssignedJobs);
        btnToggleStatus = findViewById(R.id.btnToggleDriverStatus);
        ImageButton btnLogout = findViewById(R.id.btnBackDriver); // ใช้ปุ่มนี้เป็น Logout

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Button btnCompletedJobs = findViewById(R.id.btnCompletedJobs);
        btnCompletedJobs.setOnClickListener(v -> {
            startActivity(new Intent(this, Driver_Completed_Jobs_Activity.class));
        });


        btnViewAssigned.setOnClickListener(v -> {
            Intent intent = new Intent(this, View_Assign_Driver.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(this, "ออกจากระบบแล้ว", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, Login.class));
            finish();
        });

        btnToggleStatus.setOnClickListener(v -> {
            isAvailable = !isAvailable;
            FirebaseFirestore.getInstance()
                    .collection("customer")
                    .document(currentUserId)
                    .update("status", isAvailable ? "ว่าง" : "ไม่ว่าง")
                    .addOnSuccessListener(unused -> {
                        btnToggleStatus.setText(isAvailable ? "สถานะ: ว่าง" : "สถานะ: ไม่ว่าง");
                        Toast.makeText(this, "อัปเดตสถานะสำเร็จ", Toast.LENGTH_SHORT).show();
                    });
        });

        fetchDriverRating();
    }

    private void fetchDriverRating() {
        FirebaseFirestore.getInstance()
                .collection("bookings")
                .whereEqualTo("driverId", currentUserId)
                .whereEqualTo("status", "เดินทางเสร็จสิ้น")
                .get()
                .addOnSuccessListener(query -> {
                    int jobCount = 0;
                    double ratingSum = 0;
                    for (QueryDocumentSnapshot doc : query) {
                        jobCount++;
                        Object ratingObj = doc.get("driverRating");
                        if (ratingObj instanceof Number) {
                            ratingSum += ((Number) ratingObj).doubleValue();
                        }
                    }

                    if (jobCount > 0) {
                        double avg = ratingSum / jobCount;
                        ratingBar.setRating((float) avg);
                        tvRatingSummary.setText(String.format("คะแนนเฉลี่ย: %.1f / 5", avg));
                    } else {
                        tvRatingSummary.setText("ยังไม่มีคะแนน");
                        ratingBar.setRating(0);
                    }

                    tvTotalJobs.setText("งานที่เสร็จสิ้น: " + jobCount + " งาน");
                });
    }
}
