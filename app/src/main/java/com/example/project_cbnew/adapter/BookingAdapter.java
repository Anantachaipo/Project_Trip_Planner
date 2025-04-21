package com.example.project_cbnew.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_cbnew.R;
import com.example.project_cbnew.model.Booking;

import java.util.ArrayList;
import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    private final List<Booking> bookings = new ArrayList<>();
    private final OnBookingClickListener listener;

    public interface OnBookingClickListener {
        void onClick(Booking booking);
    }

    public BookingAdapter(List<Booking> bookings, OnBookingClickListener listener) {
        if (bookings != null) this.bookings.addAll(bookings);
        this.listener = listener;
    }

    public void setBookings(List<Booking> newList) {
        Log.d("BOOKING_ADAPTER", "🧹 เคลียร์ bookings เดิมจำนวน: " + bookings.size());
        bookings.clear();

        if (newList != null) {
            for (Booking b : newList) {
                Log.d("BOOKING_ADAPTER", "➕ title=" + b.getTripTitle() + ", status=" + b.getStatus());
                bookings.add(b);
            }
        }

        Log.d("BOOKING_ADAPTER", "✅ เพิ่ม bookings ใหม่จำนวน: " + bookings.size());
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookings.get(position);
        String title = booking.getTripTitle() != null ? booking.getTripTitle() : "ไม่ทราบชื่อทริป";
        String status = booking.getStatus() != null ? booking.getStatus() : "ไม่ทราบสถานะ";

        holder.tvTitle.setText("🚐 " + title);
        holder.tvStatus.setText("📌 " + status);

        holder.itemView.setOnClickListener(v -> listener.onClick(booking));
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvStatus;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvBookingTitle);
            tvStatus = itemView.findViewById(R.id.tvBookingStatus);
        }
    }
}
