package com.example.project_cbnew.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.project_cbnew.R;
import com.example.project_cbnew.model.Stop;
import com.example.project_cbnew.model.Trip;
import com.example.project_cbnew.model.TripDay;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class MyAdepter extends RecyclerView.Adapter<MyAdepter.MyViewHolder> {

    Context context;
    private List<Trip> tripList; // ✅ แก้จาก ArrayList เป็น List
    private OnItemClickListener onItemClickListener;

    // ✅ Constructor รับ List<Trip> ตรง ๆ ไม่ต้องแปลงเป็น ArrayList
    public MyAdepter(Context context, List<Trip> tripList, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.tripList = tripList;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Trip trip = tripList.get(position);
        holder.description.setText(trip.getDescription());
        holder.tvTripTitle.setText(trip.getTitle());

        // ✅ STEP 1: หาก trip มี photoBitmap → แสดงก่อน
        if (trip.getPhotoBitmap() != null) {
            holder.titleImage.setImageBitmap(trip.getPhotoBitmap());

            // ✅ STEP 2: หาก trip มี imageUrl → ใช้ Glide โหลด
        } else if (trip.getImageUrl() != null && !trip.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(trip.getImageUrl())
                    .placeholder(R.drawable.loading_image)
                    .error(R.drawable.error)
                    .into(holder.titleImage);

            // ✅ STEP 3: หากมี imageResource → แสดงจาก resource ID
        } else if (trip.getImageResourceID() != 0) {
            holder.titleImage.setImageResource(trip.getImageResourceID());

            // ✅ STEP 4: fallback → หา stop ที่มีรูป
        } else if (trip.getTripDays() != null && !trip.getTripDays().isEmpty()) {
            Stop fallbackStop = null;
            for (TripDay day : trip.getTripDays()) {
                for (Stop stop : day.getStops()) {
                    if (stop.getPhotoBitmap() != null || stop.getImageResource() != 0 ||
                            (stop.getImageUrl() != null && !stop.getImageUrl().isEmpty())) {
                        fallbackStop = stop;
                        break;
                    }
                }
                if (fallbackStop != null) break;
            }

            if (fallbackStop != null) {
                if (fallbackStop.getPhotoBitmap() != null) {
                    holder.titleImage.setImageBitmap(fallbackStop.getPhotoBitmap());
                } else if (fallbackStop.getImageUrl() != null && !fallbackStop.getImageUrl().isEmpty()) {
                    Glide.with(context)
                            .load(fallbackStop.getImageUrl())
                            .placeholder(R.drawable.loading_image)
                            .error(R.drawable.error)
                            .into(holder.titleImage);
                } else if (fallbackStop.getImageResource() != 0) {
                    holder.titleImage.setImageResource(fallbackStop.getImageResource());
                } else {
                    holder.titleImage.setImageResource(R.drawable.t1);
                }
            } else {
                holder.titleImage.setImageResource(R.drawable.t1); // fallback สุดท้าย
            }

        } else {
            holder.titleImage.setImageResource(R.drawable.t1); // fallback สุดท้าย
        }

        // ✅ คลิกเพื่อเปิด TripDetails
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(trip);
            }
        });
    }



    @Override
    public int getItemCount() {
        return tripList.size();
    }

    // ✅ อัปเดต List และรีเฟรช RecyclerView
    public void updateList(List<Trip> newList) {
        this.tripList = newList;
        notifyDataSetChanged();
    }

    // ✅ Interface สำหรับจัดการคลิก
    public interface OnItemClickListener {
        void onItemClick(Trip trip);
    }

    // ✅ ViewHolder
    public static class MyViewHolder extends RecyclerView.ViewHolder {

        ShapeableImageView titleImage;
        TextView description;
        TextView tvTripTitle; // ← เพิ่ม


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            titleImage = itemView.findViewById(R.id.title_image);
            description = itemView.findViewById(R.id.description);
            tvTripTitle = itemView.findViewById(R.id.tvTripTitle);

        }
    }
}
