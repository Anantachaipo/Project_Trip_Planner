package com.example.project_cbnew;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_cbnew.adapter.BookingManagerAdapter;
import com.example.project_cbnew.model.Booking;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;

public class View_All_Booking_Manager extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BookingManagerAdapter adapter;
    private ArrayList<Booking> bookingList = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String currentFilter = "ทั้งหมด";
    private ImageButton btnBack;

    private TextView tvBookingCount;
    private TextView tvFilterStatus;

    private final String[] STATUS_ORDER = {
            "รอตรวจสอบ",
            "รอชำระเงิน",
            "รอคนขับ",
            "รอคนขับตอบรับ",
            "รอเดินทาง/อยู่ระหว่างเดินทาง",
            "เดินทางเสร็จสิ้น",
            "ปฏิเสธการจอง"
    };


    @Override
    protected void onResume() {
        super.onResume();
        loadAllBookings(); // ✅ โหลดใหม่เมื่อกลับมาที่หน้านี้
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_all_booking_manager);

        recyclerView = findViewById(R.id.recyclerViewManager);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookingManagerAdapter(bookingList, booking -> {
            Intent intent = new Intent(View_All_Booking_Manager.this, BookingDetails_Manager.class);
            intent.putExtra("booking", booking);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        btnBack = findViewById(R.id.btnBack);
        tvBookingCount = findViewById(R.id.tvBookingCount);
        tvFilterStatus = findViewById(R.id.tvFilterStatus);


        btnBack.setOnClickListener(v -> finish());

        FloatingActionButton fabFilter = findViewById(R.id.fabFilterManager);
        fabFilter.setOnClickListener(v -> showFilterBottomSheet());

        loadAllBookings();
    }

    private void loadAllBookings() {
        db.collection("bookings")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        bookingList.clear();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Booking booking = doc.toObject(Booking.class);
                            booking.setBookingId(doc.getId());
                            bookingList.add(booking);
                        }
                        applyFilter(currentFilter);
                    } else {
                        Toast.makeText(this, "โหลดข้อมูลล้มเหลว", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void applyFilter(String status) {
        currentFilter = status;
        ArrayList<Booking> filtered = new ArrayList<>();
        for (Booking b : bookingList) {
            String s = b.getStatus() != null ? b.getStatus().trim() : "";
            if (status.equals("ทั้งหมด") || status.equalsIgnoreCase(s)) {
                filtered.add(b);
            }
        }

        // 🔽 เรียงตามลำดับสถานะ
        Collections.sort(filtered, new java.util.Comparator<Booking>() {
            @Override
            public int compare(Booking a, Booking b) {
                int indexA = getStatusIndex(a.getStatus());
                int indexB = getStatusIndex(b.getStatus());
                return Integer.compare(indexA, indexB);
            }
        });


        adapter.setBookings(filtered);
        tvFilterStatus.setText("สถานะ: " + status);
        tvBookingCount.setText("พบ " + filtered.size() + " รายการ");
    }


    private void showFilterBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View sheetView = LayoutInflater.from(this).inflate(R.layout.dialog_filter_status, (ViewGroup) findViewById(android.R.id.content), false);

        String[] statuses = {"ทั้งหมด", "รอตรวจสอบ", "รอชำระเงิน", "รอคนขับ", "กำลังเดินทาง", "เสร็จสิ้น","ปฏิเสธการจอง"};
        ViewGroup container = sheetView.findViewById(R.id.statusContainer);
        for (String status : statuses) {
            TextView statusItem = new TextView(this);
            statusItem.setText(status);
            statusItem.setTextSize(16);
            statusItem.setPadding(40, 30, 40, 30);
            statusItem.setOnClickListener(v -> {
                dialog.dismiss();
                applyFilter(status);
            });
            container.addView(statusItem);
        }

        dialog.setContentView(sheetView);
        dialog.show();
    }


    private int getStatusIndex(String status) {
        for (int i = 0; i < STATUS_ORDER.length; i++) {
            if (STATUS_ORDER[i].equalsIgnoreCase(status)) return i;
        }
        return STATUS_ORDER.length; // ถ้าไม่พบ ให้ถือว่าเป็นอันดับท้ายสุด
    }

}
