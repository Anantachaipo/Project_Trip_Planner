package com.example.project_cbnew;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_cbnew.adapter.BookingAdapter;
import com.example.project_cbnew.model.Booking;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MyBooking_Fragment extends Fragment {

    private RecyclerView recyclerView;
    private BookingAdapter adapter;
    private List<Booking> bookingList = new ArrayList<>();
    private String currentFilter = "ทั้งหมด";
    private TextView tvBookingStatus;

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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_booking, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewBookings);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new BookingAdapter(bookingList, this::onBookingClick);
        recyclerView.setAdapter(adapter);

        tvBookingStatus = view.findViewById(R.id.tvBookingStatus); // เชื่อมกับ TextView ด้านบน
        tvBookingStatus.setText("สถานะรายการจอง : " + currentFilter);

        FloatingActionButton fabFilter = view.findViewById(R.id.fabFilter);
        fabFilter.setOnClickListener(v -> showFilterDialog());

        loadBookings();
        return view;
    }

    private void showFilterDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View sheetView = getLayoutInflater().inflate(R.layout.dialog_filter_status, null);

        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(24, 24, 24, 24);

        String[] statuses = {"ทั้งหมด", "รอตรวจสอบ", "รอชำระเงิน", "รอคนขับ","รอคนขับตอบรับ", "รอเดินทาง/อยู่ระหว่างเดินทาง", "เดินทางเสร็จสิ้น", "ปฏิเสธการจอง"};
        for (String status : statuses) {
            TextView statusItem = new TextView(requireContext());
            statusItem.setText(status);
            statusItem.setTextSize(16);
            statusItem.setPadding(30, 30, 30, 30);
            statusItem.setOnClickListener(v -> {
                dialog.dismiss();
                filterByStatus(status);
            });
            container.addView(statusItem);
        }

        dialog.setContentView(container);
        dialog.show();
    }

    private void loadBookings() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (userId == null) return;

        FirebaseFirestore.getInstance().collection("bookings")
                .whereEqualTo("accountId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    bookingList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Booking booking = doc.toObject(Booking.class);
                        if (booking != null) {
                            booking.setBookingId(doc.getId());
                            bookingList.add(booking);
                        }
                    }
                    filterByStatus(currentFilter);
                });
    }

    private void filterByStatus(String status) {
        currentFilter = status;

        if (tvBookingStatus != null) {
            tvBookingStatus.setText("สถานะรายการจอง : " + status);
        }

        List<Booking> filtered = new ArrayList<>();
        for (Booking b : bookingList) {
            if (status.equals("ทั้งหมด") || status.equals(b.getStatus())) {
                filtered.add(b);
            }
        }

        // ✅ เรียงลำดับแบบ compatible กับ API 23
        Collections.sort(filtered, new Comparator<Booking>() {
            @Override
            public int compare(Booking a, Booking b) {
                int indexA = getStatusIndex(a.getStatus());
                int indexB = getStatusIndex(b.getStatus());
                return Integer.compare(indexA, indexB);
            }
        });

        adapter.setBookings(filtered);
    }

    private int getStatusIndex(String status) {
        for (int i = 0; i < STATUS_ORDER.length; i++) {
            if (STATUS_ORDER[i].equals(status)) return i;
        }
        return STATUS_ORDER.length; // ไม่พบ → ไปท้ายสุด
    }



    private void onBookingClick(Booking booking) {
        if ("รอชำระเงิน".equals(booking.getStatus())) {
            Payment_QR_Fragment fragment = Payment_QR_Fragment.newInstance(booking);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frameLayout, fragment)
                    .addToBackStack(null)
                    .commit();
        } else {
            Booking_Details_Fragment fragment = Booking_Details_Fragment.newInstance(booking);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frameLayout, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
}
