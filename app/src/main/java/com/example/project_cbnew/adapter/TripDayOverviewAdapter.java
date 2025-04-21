package com.example.project_cbnew.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_cbnew.R;
import com.example.project_cbnew.model.Stop;
import com.example.project_cbnew.model.TripDay;

import java.util.List;
import java.util.Locale;

public class TripDayOverviewAdapter extends RecyclerView.Adapter<TripDayOverviewAdapter.DayViewHolder> {

    private List<TripDay> tripDays;

    public TripDayOverviewAdapter(List<TripDay> tripDays) {
        this.tripDays = tripDays;
    }
    private OnDayClickListener onDayClickListener;

    public void setOnDayClickListener(OnDayClickListener listener) {
        this.onDayClickListener = listener;
    }
    public interface OnDayClickListener {
        void onDayClick(int position, TripDay tripDay);
    }


    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_day_overview, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        TripDay tripDay = tripDays.get(position);

        // ✅ ป้องกัน null
        String title = tripDay.getTitle();
        holder.tvDayTitle.setText(title != null ? title : "ไม่ระบุวัน");

        StringBuilder stopsText = new StringBuilder();
        List<Stop> stops = tripDay.getStops();
        for (int i = 0; i < stops.size(); i++) {
            Stop stop = stops.get(i);
            stopsText.append("• จุดที่ ").append(i + 1).append(": ")
                    .append(stop.getName() != null ? stop.getName() : "ไม่ทราบชื่อ").append("\n");

            // ✅ เช็ก description
            if (stop.getDescription() != null && !stop.getDescription().isEmpty()) {
                stopsText.append("   ").append(stop.getDescription()).append("\n");
            }
        }

        String summary = String.format(Locale.getDefault(),
                "\n\n📏 ระยะทาง: %d กม.\n🚌 เวลาเดินทาง: %s\n⏱ เวลาพัก: %s",
                tripDay.getTotalDistanceKm(),
                formatMinutes(tripDay.getTotalTravelMinutes()),
                formatMinutes(tripDay.getTotalRestMinutes()));

        holder.tvStops.setText(stopsText.toString().trim() + summary);

        holder.itemView.setOnClickListener(v -> {
            if (onDayClickListener != null) {
                onDayClickListener.onDayClick(position, tripDays.get(position));
            }
        });
    }




    // 🕓 Helper
    private String formatMinutes(int minutes) {
        int h = minutes / 60;
        int m = minutes % 60;
        if (h > 0 && m > 0) return h + " ชม. " + m + " นาที";
        else if (h > 0) return h + " ชม.";
        else return m + " นาที";
    }





    @Override
    public int getItemCount() {
        return tripDays.size();
    }

    public static class DayViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayTitle, tvStops;

        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayTitle = itemView.findViewById(R.id.tvDayTitle);
            tvStops = itemView.findViewById(R.id.tvStops);
        }
    }
    public void updateData(List<TripDay> newData) {
        tripDays.clear();
        tripDays.addAll(newData);
        notifyDataSetChanged();
    }



}
