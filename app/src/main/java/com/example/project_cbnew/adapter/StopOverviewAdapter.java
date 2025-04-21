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

public class StopOverviewAdapter extends RecyclerView.Adapter<StopOverviewAdapter.StopViewHolder> {

    private final List<Stop> stops;

    public StopOverviewAdapter(List<Stop> stops) {
        this.stops = stops;
    }

    @NonNull
    @Override
    public StopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stop_overview, parent, false);
        return new StopViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StopViewHolder holder, int position) {
        Stop stop = stops.get(position);
        holder.tvStopName.setText("จุดที่ " + (position + 1) + ": " + stop.getName());
        holder.tvStopDesc.setText(stop.getDescription());
    }

    @Override
    public int getItemCount() {
        return stops.size();
    }

    static class StopViewHolder extends RecyclerView.ViewHolder {
        TextView tvStopName, tvStopDesc;

        public StopViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStopName = itemView.findViewById(R.id.tvStopNameOverview);
            tvStopDesc = itemView.findViewById(R.id.tvStopDescOverview);
        }
    }
}
