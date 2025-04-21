package com.example.project_cbnew.adapter;

import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.example.project_cbnew.R;
import com.example.project_cbnew.model.Stop;

import android.content.Context;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class StopPreviewAdapter extends RecyclerView.Adapter<StopPreviewAdapter.StopViewHolder> {

    private final List<Stop> stopList;

    public StopPreviewAdapter(List<Stop> stopList) {
        this.stopList = stopList;
    }

    @NonNull
    @Override
    public StopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stop_preview, parent, false);
        return new StopViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StopViewHolder holder, int position) {
        Stop stop = stopList.get(position);
        holder.stopName.setText(stop.getName());
        holder.stopDesc.setText(stop.getDescription());

        Context context = holder.itemView.getContext();
        int imageSize = 120;

        if (stop.getImageUrl() != null && !stop.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(stop.getImageUrl())
                    .override(imageSize, imageSize)
                    .centerCrop()
                    .placeholder(R.drawable.t1)
                    .into(holder.stopImage);

        } else if (stop.getPhotoBitmap() != null) {
            // 🔥 ปลอดภัย 100%: แปลงเป็นไฟล์ก่อนใช้ Glide โหลด
            try {
                Bitmap original = stop.getPhotoBitmap();
                int width = original.getWidth();
                int height = original.getHeight();
                int maxSize = 512;

                float ratio = Math.min((float) maxSize / width, (float) maxSize / height);
                int resizedWidth = Math.round(width * ratio);
                int resizedHeight = Math.round(height * ratio);
                Bitmap resized = Bitmap.createScaledBitmap(original, resizedWidth, resizedHeight, true);

                // ✅ บันทึก bitmap เป็นไฟล์ temp
                File tempFile = File.createTempFile("stop_", ".jpg", context.getCacheDir());
                FileOutputStream fos = new FileOutputStream(tempFile);
                resized.compress(Bitmap.CompressFormat.JPEG, 80, fos);
                fos.close();

                // ✅ โหลดไฟล์นี้เข้า Glide
                Glide.with(context)
                        .load(Uri.fromFile(tempFile))
                        .override(imageSize, imageSize)
                        .centerCrop()
                        .placeholder(R.drawable.t1)
                        .into(holder.stopImage);

            } catch (Exception e) {
                e.printStackTrace();
                holder.stopImage.setImageResource(R.drawable.t1);
            }

        } else if (stop.getImageResource() != 0) {
            Glide.with(context)
                    .load(stop.getImageResource())
                    .override(imageSize, imageSize)
                    .centerCrop()
                    .placeholder(R.drawable.t1)
                    .into(holder.stopImage);

        } else {
            Glide.with(context)
                    .load(R.drawable.t1)
                    .override(imageSize, imageSize)
                    .centerCrop()
                    .into(holder.stopImage);
        }
    }











    @Override
    public int getItemCount() {
        return stopList.size();
    }

    static class StopViewHolder extends RecyclerView.ViewHolder {
        TextView stopName, stopDesc;
        ImageView stopImage;

        public StopViewHolder(@NonNull View itemView) {
            super(itemView);
            stopName = itemView.findViewById(R.id.stopName);
            stopDesc = itemView.findViewById(R.id.stopDesc);
            stopImage = itemView.findViewById(R.id.stopImage);
        }
    }
}
