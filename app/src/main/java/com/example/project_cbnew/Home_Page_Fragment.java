package com.example.project_cbnew;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class Home_Page_Fragment extends Fragment {

    private TextView welcomeText, termsText;
    private ImageView profileImage;
    private Button logoutButton;

    public Home_Page_Fragment() {
        // Required empty public constructor
    }

    public static Home_Page_Fragment newInstance() {
        return new Home_Page_Fragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_page, container, false);

        TextView tvWelcome = view.findViewById(R.id.tvWelcome);
        ImageButton btnLogout = view.findViewById(R.id.btnLogout);
        Button btnTrips = view.findViewById(R.id.btnTrips);
        Button btnMyBooking = view.findViewById(R.id.btnMyBooking);
        TextView tvBookingInfo = view.findViewById(R.id.tvBookingInfo);
        TextView btnToggle = view.findViewById(R.id.btnToggleBookingInfo);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid(); // ✅ ใช้ getUid()

            FirebaseFirestore.getInstance().collection("customer").document(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            tvWelcome.setText("สวัสดี " + (name != null ? name : "ผู้ใช้"));
                        } else {
                            tvWelcome.setText("สวัสดีผู้ใช้");
                        }
                    })
                    .addOnFailureListener(e -> {
                        tvWelcome.setText("สวัสดีผู้ใช้");
                    });
        }


        btnTrips.setOnClickListener(v -> navigateTo(new Trip_Fragment()));
        btnMyBooking.setOnClickListener(v -> navigateTo(new MyBooking_Fragment()));


        // ✅ ออกจากระบบ
        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("ออกจากระบบ")
                    .setMessage("คุณต้องการออกจากระบบหรือไม่?")
                    .setPositiveButton("ออกจากระบบ", (dialog, which) -> {
                        FirebaseAuth.getInstance().signOut();

                        // กลับไปหน้า Login
                        Intent intent = new Intent(getContext(), Login.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    })
                    .setNegativeButton("ยกเลิก", null)
                    .show();
        });


        btnToggle.setOnClickListener(v -> {
            if (tvBookingInfo.getMaxLines() == Integer.MAX_VALUE) {
                tvBookingInfo.setMaxLines(6);
                btnToggle.setText("แสดงเพิ่มเติม ▼");
            } else {
                tvBookingInfo.setMaxLines(Integer.MAX_VALUE);
                btnToggle.setText("ย่อข้อมูล ▲");
            }
        });


        return view;
    }

    private void navigateTo(Fragment fragment) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frameLayout, fragment)
                .addToBackStack(null)
                .commit();
    }



}
