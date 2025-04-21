package com.example.project_cbnew.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_cbnew.R;
import com.example.project_cbnew.model.Stop;

import java.util.List;

public class StopAdapter2 extends RecyclerView.Adapter<StopAdapter2.StopViewHolder> {

    private List<Stop> stopList;

    public StopAdapter2(List<Stop> stopList) {
        this.stopList = stopList;
    }

    @NonNull
    @Override
    public StopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stop_simple, parent, false);
        return new StopViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StopViewHolder holder, int position) {
        Stop stop = stopList.get(position);
        holder.tvStopName.setText(stop.getName());
        holder.tvStopDesc.setText(stop.getDescription());
    }

    @Override
    public int getItemCount() {
        return stopList.size();
    }

    public void updateStops(List<Stop> newStops) {
        this.stopList = newStops;
        notifyDataSetChanged();
    }

    static class StopViewHolder extends RecyclerView.ViewHolder {
        TextView tvStopName, tvStopDesc;

        StopViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStopName = itemView.findViewById(R.id.tvStopName);
            tvStopDesc = itemView.findViewById(R.id.tvStopDesc);
        }
    }
}
