package com.example.project_cbnew;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class Home_Page_Manager extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 🔐 ป้องกันเข้าโดยไม่ได้ login
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Intent intent = new Intent(this, Login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_home_page_manager);
        setTitle("หน้าหลักของผู้ดูแลระบบ"); // 🖼️ เปลี่ยน title บน ActionBar

        // 🔘 ปุ่มต่าง ๆ
        Button btnAllBookings = findViewById(R.id.btnViewBookings);
        Button btnManageDrivers = findViewById(R.id.btnViewDrivers);
        ImageButton btnLogout = findViewById(R.id.btnLogout);

        // 🔗 ไปหน้ารายการจองทั้งหมด
        btnAllBookings.setOnClickListener(v -> {
            Intent intent = new Intent(Home_Page_Manager.this, View_All_Booking_Manager.class);
            startActivity(intent);
        });

        // 🔗 ไปหน้าจัดการคนขับ
        btnManageDrivers.setOnClickListener(v -> {
            Intent intent = new Intent(Home_Page_Manager.this, View_All_Driver_Manager.class);
            startActivity(intent);
        });

        // 🔓 ออกจากระบบ
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(Home_Page_Manager.this, Login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // ❌ ปิดหน้าปัจจุบัน
        });
    }
}
