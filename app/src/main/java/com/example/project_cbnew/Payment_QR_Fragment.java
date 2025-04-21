package com.example.project_cbnew;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.project_cbnew.model.Booking;
import com.google.firebase.firestore.FirebaseFirestore;

public class Payment_QR_Fragment extends Fragment {

    private static final String ARG_BOOKING = "booking";
    private Booking booking;

    public static Payment_QR_Fragment newInstance(Booking booking) {
        Payment_QR_Fragment fragment = new Payment_QR_Fragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_BOOKING, booking);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_payment_qr, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            booking = getArguments().getParcelable(ARG_BOOKING);
        }

        ImageView qrImage = view.findViewById(R.id.qrImage);
        Button btnConfirmPayment = view.findViewById(R.id.btnConfirmPayment);
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        Button btnCancelBooking = view.findViewById(R.id.btnCancelBooking);

        tvTitle.setText("📌 ชำระเงินสำหรับทริป: " + booking.getTripTitle());

        Glide.with(requireContext())
                .load(R.drawable.qr_sample) // ใส่ QR code จริงได้ทีหลัง
                .into(qrImage);

        btnConfirmPayment.setOnClickListener(v -> {
            FirebaseFirestore.getInstance()
                    .collection("bookings")
                    .document(booking.getBookingId())
                    .update("status", "รอคนขับ")
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(requireContext(), "✅ ยืนยันการชำระเงินแล้ว", Toast.LENGTH_SHORT).show();
                        requireActivity().getSupportFragmentManager().popBackStack(); // ย้อนกลับ
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "❌ เกิดข้อผิดพลาดในการอัปเดต", Toast.LENGTH_SHORT).show();
                    });
        });

        btnCancelBooking.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("ยืนยันการยกเลิก")
                    .setMessage("คุณแน่ใจหรือไม่ว่าต้องการยกเลิกการจองนี้?")
                    .setPositiveButton("ยืนยัน", (dialog, which) -> {
                        FirebaseFirestore.getInstance()
                                .collection("bookings")
                                .document(booking.getBookingId())
                                .update("status", "ปฏิเสธการจอง")
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(requireContext(), "ยกเลิกการจองเรียบร้อย", Toast.LENGTH_SHORT).show();
                                    requireActivity().getSupportFragmentManager()
                                            .beginTransaction()
                                            .replace(R.id.frameLayout, new MyBooking_Fragment())
                                            .commit();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(requireContext(), "เกิดข้อผิดพลาด", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .setNegativeButton("ยกเลิก", null)
                    .show();
        });
    }
}
