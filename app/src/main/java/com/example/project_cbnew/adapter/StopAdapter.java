package com.example.project_cbnew.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.project_cbnew.R;
import com.example.project_cbnew.model.Stop;

import java.util.Collections;
import java.util.List;

public class StopAdapter extends RecyclerView.Adapter<StopAdapter.StopViewHolder> {

    private final List<Stop> stopList;
    private final OnStopClickListener listener;

    public interface OnStopClickListener {
        void onStopClick(Stop stop);
        void onStopLongClick(Stop stop, int position);
    }

    public StopAdapter(List<Stop> stopList, OnStopClickListener listener) {
        this.stopList = stopList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public StopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stop, parent, false);
        return new StopViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StopViewHolder holder, int position) {
        Stop stop = stopList.get(position);

        holder.tvStopName.setText(stop.getName());
        holder.tvStopDesc.setText(stop.getDescription());

        // 📍 เวลาเดินทางจากจุดก่อนหน้า (ไม่แสดงสำหรับจุดแรก)
        if (position == 0) {
            holder.tvStopDuration.setText("จุดเริ่มต้น");
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("• เวลาเดินทาง: ").append(stop.getTravelTimeFromPrevious()).append(" นาที");

            if (position != stopList.size() - 1) {
                sb.append("\n• เวลาพัก: ").append(stop.getDurationAtStop()).append(" นาที");
            }

            holder.tvStopDuration.setText(sb.toString());
        }

        // ✅ รองรับรูป 3 แบบ: photoBitmap > imageUrl > imageResource
        if (stop.getPhotoBitmap() != null) {
            holder.ivStopImage.setImageBitmap(stop.getPhotoBitmap());

        } else if (stop.getImageUrl() != null && !stop.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(stop.getImageUrl())  // รองรับทั้ง URL และ resource URI
                    .placeholder(R.drawable.loading_image)
                    .error(R.drawable.error)
                    .into(holder.ivStopImage);

        } else if (stop.getImageResource() != 0) {
            Glide.with(holder.itemView.getContext())
                    .load(stop.getImageResource())
                    .placeholder(R.drawable.loading_image)
                    .error(R.drawable.error)
                    .into(holder.ivStopImage);

        } else {
            holder.ivStopImage.setImageResource(R.drawable.t1); // fallback
        }

        holder.itemView.setOnClickListener(v -> listener.onStopClick(stop));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onStopLongClick(stop, position);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return stopList.size();
    }

    public static class StopViewHolder extends RecyclerView.ViewHolder {
        TextView tvStopName, tvStopDesc, tvStopDuration;
        ImageView ivStopImage;

        public StopViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStopName = itemView.findViewById(R.id.tvStopName);
            tvStopDesc = itemView.findViewById(R.id.tvStopDesc);
            tvStopDuration = itemView.findViewById(R.id.tvStopDuration);
            ivStopImage = itemView.findViewById(R.id.ivStopImage);
        }
    }

    public void swapItems(int fromPosition, int toPosition) {
        Collections.swap(stopList, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    public void updateStops(List<Stop> newStops) {
        stopList.clear();
        stopList.addAll(newStops);
        notifyDataSetChanged();
    }
}
