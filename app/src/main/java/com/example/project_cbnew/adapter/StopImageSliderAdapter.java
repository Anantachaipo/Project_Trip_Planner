package com.example.project_cbnew.adapter;

import com.bumptech.glide.Glide;
import com.example.project_cbnew.R;
import com.example.project_cbnew.model.Stop;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StopImageSliderAdapter extends RecyclerView.Adapter<StopImageSliderAdapter.ImageViewHolder> {

    private final List<Stop> stopList;

    public StopImageSliderAdapter(List<Stop> stopList) {
        this.stopList = stopList;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stop_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Stop stop = stopList.get(position);
        holder.stopName.setText(stop.getName());

        // 🔍 Debug log
        Log.d("SLIDER_IMG", "Index=" + position
                + " | Name=" + stop.getName()
                + " | URL=" + stop.getImageUrl()
                + " | resId=" + stop.getImageResource());

        if (stop.getPhotoBitmap() != null) {
            holder.imageView.setImageBitmap(stop.getPhotoBitmap());
        }
        else if (stop.getImageUrl() != null && !stop.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(stop.getImageUrl())
                    .placeholder(R.drawable.t1)
                    .into(holder.imageView);
        }
        else if (stop.getImageResource() != 0) {
            holder.imageView.setImageResource(stop.getImageResource());
        }
        else {
            Log.w("SLIDER_IMG", "❌ ไม่มีรูปของ stop: " + stop.getName());
            holder.imageView.setImageResource(R.drawable.t1);
        }
    }


    @Override
    public int getItemCount() {
        return stopList.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView stopName;
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.stopImageSlider);
            stopName = itemView.findViewById(R.id.stopNameSlider);
        }
    }
}
