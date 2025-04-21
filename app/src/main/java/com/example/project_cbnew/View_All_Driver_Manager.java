package com.example.project_cbnew;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_cbnew.adapter.DriverAdapter;
import com.example.project_cbnew.model.User;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class View_All_Driver_Manager extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DriverAdapter adapter;
    private ArrayList<User> driverList = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String currentFilter = "ทั้งหมด";
    private TextView tvDriverStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_all_driver_manager);

        recyclerView = findViewById(R.id.recyclerViewDrivers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new DriverAdapter(this, driverList, null, false); // ไม่แสดงปุ่ม

        recyclerView.setAdapter(adapter);

        tvDriverStatus = findViewById(R.id.tvDriverStatus);
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());


        FloatingActionButton fabFilter = findViewById(R.id.fabDriverFilter);
        fabFilter.setOnClickListener(v -> showFilterDialog());

        loadDrivers();
    }

    private void showFilterDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.dialog_filter_status, null);

        LinearLayout container = sheetView.findViewById(R.id.statusContainer); // 🔧 FIXED!
        if (container == null) {
            Toast.makeText(this, "❌ ไม่พบ container ใน dialog_filter_status.xml", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] statuses = {"ทั้งหมด", "ว่าง", "ไม่ว่าง", "รับงานอยู่"};
        for (String status : statuses) {
            TextView statusItem = new TextView(this);
            statusItem.setText(status);
            statusItem.setTextSize(16);
            statusItem.setPadding(40, 30, 40, 30);
            statusItem.setOnClickListener(v -> {
                dialog.dismiss();
                filterByStatus(status);
            });
            container.addView(statusItem);
        }

        dialog.setContentView(sheetView);
        dialog.show();
    }


    private void loadDrivers() {
        db.collection("customer")
                .whereEqualTo("role", "driver")
                .get()
                .addOnSuccessListener(query -> {
                    driverList.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        User driver = doc.toObject(User.class);
                        driverList.add(driver);
                    }
                    filterByStatus(currentFilter);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "โหลดรายชื่อคนขับล้มเหลว", Toast.LENGTH_SHORT).show());
    }


    private void filterByStatus(String status) {
        currentFilter = status;
        if (tvDriverStatus != null) {
            tvDriverStatus.setText("สถานะคนขับ : " + status);
        }

        ArrayList<User> filtered = new ArrayList<>();
        for (User driver : driverList) {
            String stat = driver.getStatus() != null ? driver.getStatus().trim() : "";
            if (status.equals("ทั้งหมด") || status.equalsIgnoreCase(stat)) {
                filtered.add(driver);
            }
        }
        adapter.setDrivers(filtered);
    }
}
