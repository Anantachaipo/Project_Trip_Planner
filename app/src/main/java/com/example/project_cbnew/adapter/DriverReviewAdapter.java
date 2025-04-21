package com.example.project_cbnew.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_cbnew.R;
import com.example.project_cbnew.model.Booking;

import java.util.List;

public class DriverReviewAdapter extends RecyclerView.Adapter<DriverReviewAdapter.ReviewViewHolder> {

    private List<Booking> reviewList;

    public DriverReviewAdapter(List<Booking> reviewList) {
        this.reviewList = reviewList;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_driver_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Booking booking = reviewList.get(position);
        holder.tvTripTitle.setText("🚐 ทริป: " + booking.getTripTitle());
        holder.tvDate.setText("📅 " + booking.getStartDate() + " - " + booking.getEndDate());
        holder.ratingBar.setRating(booking.getDriverRating() != null ? booking.getDriverRating().floatValue() : 0f);
        holder.tvReview.setText("💬 " + (booking.getDriverReviewNote() == null || booking.getDriverReviewNote().isEmpty()
                ? "ไม่มีข้อความเพิ่มเติม" : booking.getDriverReviewNote()));
    }


    @Override
    public int getItemCount() {
        return reviewList != null ? reviewList.size() : 0;
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvTripTitle, tvDate, tvReview;
        RatingBar ratingBar;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTripTitle = itemView.findViewById(R.id.tvTripTitle);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvReview = itemView.findViewById(R.id.tvReview);
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }
    }
}
