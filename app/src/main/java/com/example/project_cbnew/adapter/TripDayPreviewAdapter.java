package com.example.project_cbnew.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_cbnew.R;
import com.example.project_cbnew.model.TripDay;

import java.util.List;

public class TripDayPreviewAdapter extends RecyclerView.Adapter<TripDayPreviewAdapter.DayViewHolder> {

    private final List<TripDay> dayList;

    public TripDayPreviewAdapter(List<TripDay> dayList) {
        this.dayList = dayList;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trip_day_preview, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        TripDay day = dayList.get(position);
        holder.tvDayTitle.setText(day.getTitle());

        StopPreviewAdapter stopAdapter = new StopPreviewAdapter(day.getStops());
        holder.recyclerStops.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.recyclerStops.setNestedScrollingEnabled(false); // ✅ เพิ่มสำหรับ RecyclerView ซ้อน ScrollView
        holder.recyclerStops.setAdapter(stopAdapter);
        holder.recyclerStops.addItemDecoration(new DividerItemDecoration(
                holder.recyclerStops.getContext(), LinearLayoutManager.VERTICAL));

    }

    @Override
    public int getItemCount() {
        return dayList.size();
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayTitle;
        RecyclerView recyclerStops;

        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayTitle = itemView.findViewById(R.id.tvDayTitle);
            recyclerStops = itemView.findViewById(R.id.recyclerStopsPreview);
        }
    }


}
