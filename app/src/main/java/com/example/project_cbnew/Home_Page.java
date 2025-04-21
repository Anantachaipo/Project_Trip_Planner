package com.example.project_cbnew;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.project_cbnew.databinding.ActivityHomePageBinding;

public class Home_Page extends AppCompatActivity {

    private ActivityHomePageBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomePageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // โหลด Fragment เฉพาะตอนเปิดแอปครั้งแรก
        if (savedInstanceState == null) {
            replaceFragment(new Home_Page_Fragment());
        }

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment selectedFragment = null;

            if (id == R.id.home) {
                selectedFragment = new Home_Page_Fragment();
            } else if (id == R.id.trip) {
                selectedFragment = new Trip_Fragment();
            } else if (id == R.id.allBooking) {
                selectedFragment = new MyBooking_Fragment();
            } else if (id == R.id.setting) {
                selectedFragment = new Setting_Fragment();
            }

            if (selectedFragment != null) {
                replaceFragment(selectedFragment);
            }
            return true;

        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frameLayout, fragment);
        transaction.commit();
    }
}
