package com.example.project_cbnew.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_cbnew.R;
import com.example.project_cbnew.model.TripDay;

import java.util.List;

public class DayAdapter extends RecyclerView.Adapter<DayAdapter.DayViewHolder> {

    private final List<TripDay> dayList;
    private final OnDayClickListener listener;
    private int selectedIndex = 0;

    public interface OnDayClickListener {
        void onDayClick(int position);
    }

    public DayAdapter(List<TripDay> dayList, OnDayClickListener listener) {
        this.dayList = dayList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_day_chip, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        TripDay tripDay = dayList.get(position);
        holder.bind(tripDay.getTitle(), position == selectedIndex);

        holder.itemView.setOnClickListener(v -> {
            int oldIndex = selectedIndex;
            selectedIndex = position;
            notifyItemChanged(oldIndex);
            notifyItemChanged(selectedIndex);
            listener.onDayClick(position);
        });
    }

    @Override
    public int getItemCount() {
        return dayList.size();
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {
        TextView tvDay;

        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tvDayChip);
        }

        void bind(String dayTitle, boolean isSelected) {
            tvDay.setText(dayTitle);
            Context context = tvDay.getContext();
            if (isSelected) {
                tvDay.setBackgroundResource(R.drawable.bg_chip_selected);
                tvDay.setTextColor(Color.WHITE);
            } else {
                tvDay.setBackgroundResource(R.drawable.bg_chip_unselected);
                tvDay.setTextColor(ContextCompat.getColor(context, R.color.black));
            }
        }
    }
}
