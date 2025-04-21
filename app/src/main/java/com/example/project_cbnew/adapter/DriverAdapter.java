package com.example.project_cbnew.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_cbnew.R;
import com.example.project_cbnew.model.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;

public class DriverAdapter extends RecyclerView.Adapter<DriverAdapter.DriverViewHolder> {

    private final boolean showAssignButton;
    private final List<User> driverList;
    private final Context context;
    private final OnDriverClickListener listener;

    public interface OnDriverClickListener {
        void onDriverClick(User driver);
    }

    public DriverAdapter(Context context, List<User> driverList, OnDriverClickListener listener, boolean showAssignButton) {
        this.context = context;
        this.driverList = driverList;
        this.listener = listener;
        this.showAssignButton = showAssignButton;
    }

    @NonNull
    @Override
    public DriverViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_driver, parent, false);
        return new DriverViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DriverViewHolder holder, int position) {
        User driver = driverList.get(position);

        holder.tvDriverName.setText("👨‍✈️ " + driver.getName());
        holder.tvDriverEmail.setText("📧 " + driver.getEmail());
        holder.tvDriverTel.setText("📞 " + driver.getTel());
        holder.tvDriverStatus.setText("สถานะ: " + driver.getStatus());

        // Default text for rating/jobs
        holder.tvDriverRating.setText("⭐ คะแนนเฉลี่ย: -");
        holder.tvCompletedJobs.setText("📦 งานที่เสร็จสิ้น: -");

        // 🔄 โหลดข้อมูลจาก Firestore
        FirebaseFirestore.getInstance().collection("bookings")
                .whereEqualTo("driverId", driver.getUserId())
                .whereEqualTo("status", "เดินทางเสร็จสิ้น")
                .get()
                .addOnSuccessListener(snapshot -> {
                    int completedJobs = snapshot.size();
                    float totalRating = 0f;
                    int ratedCount = 0;

                    for (QueryDocumentSnapshot doc : snapshot) {
                        Double rating = doc.getDouble("driverRating");
                        if (rating != null && rating > 0) {
                            totalRating += rating;
                            ratedCount++;
                        }
                    }

                    float avg = ratedCount > 0 ? totalRating / ratedCount : 0f;

                    holder.tvCompletedJobs.setText("📦 งานที่เสร็จสิ้น: " + completedJobs);
                    holder.tvDriverRating.setText(String.format("⭐ คะแนนเฉลี่ย: %.1f", avg));
                });

        // 🔘 ปุ่มส่งงาน
        if (showAssignButton) {
            holder.btnAssign.setVisibility(View.VISIBLE);

            if ("ว่าง".equals(driver.getStatus())) {
                holder.tvDriverStatus.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
                holder.btnAssign.setEnabled(true);
                holder.btnAssign.setAlpha(1f);
            } else {
                holder.tvDriverStatus.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
                holder.btnAssign.setEnabled(false);
                holder.btnAssign.setAlpha(0.5f);
            }

            holder.btnAssign.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDriverClick(driver);
                }
            });
        } else {
            holder.btnAssign.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return driverList.size();
    }

    public void setDrivers(List<User> newList) {
        driverList.clear();
        driverList.addAll(newList);
        notifyDataSetChanged();
    }

    static class DriverViewHolder extends RecyclerView.ViewHolder {
        TextView tvDriverName, tvDriverEmail, tvDriverTel, tvDriverStatus, tvDriverRating, tvCompletedJobs;
        Button btnAssign;

        public DriverViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDriverName = itemView.findViewById(R.id.tvDriverName);
            tvDriverEmail = itemView.findViewById(R.id.tvDriverEmail);
            tvDriverTel = itemView.findViewById(R.id.tvDriverTel);
            tvDriverStatus = itemView.findViewById(R.id.tvDriverStatus);
            tvDriverRating = itemView.findViewById(R.id.tvDriverRating);
            tvCompletedJobs = itemView.findViewById(R.id.tvCompletedJobs);
            btnAssign = itemView.findViewById(R.id.btnAssignDriver);
        }
    }
}
