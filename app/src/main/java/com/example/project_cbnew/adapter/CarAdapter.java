package com.example.project_cbnew.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.project_cbnew.R;
import com.example.project_cbnew.model.Car;
import com.google.firebase.storage.FirebaseStorage;


import java.util.List;

public class CarAdapter extends RecyclerView.Adapter<CarAdapter.CarViewHolder> {

    private List<Car> carList;
    private final OnCarSelectedListener listener;
    private int selectedPosition = RecyclerView.NO_POSITION;

    public interface OnCarSelectedListener {
        void onCarSelected(Car car);
    }

    public CarAdapter(List<Car> carList, OnCarSelectedListener listener) {
        this.carList = carList;
        this.listener = listener;
    }

    public void setFilteredList(List<Car> filteredCars) {
        this.carList = filteredCars;
        selectedPosition = RecyclerView.NO_POSITION;
        notifyDataSetChanged();
    }

    public Car getSelectedCar() {
        if (selectedPosition != RecyclerView.NO_POSITION && selectedPosition < carList.size()) {
            return carList.get(selectedPosition);
        }
        return null;
    }

    @NonNull
    @Override
    public CarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_car, parent, false);
        return new CarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarViewHolder holder, int position) {
        Car car = carList.get(position);
        holder.tvName.setText(car.getName());
        holder.tvType.setText("ประเภทรถ: " + car.getType());
        holder.tvCapacity.setText("ความจุ: " + car.getCapacity() + " ที่นั่ง");
        holder.tvPrice.setText("ราคา: " + car.getPrice() + " บาท/วัน");

        // ✅ ใช้ FirebaseStorage แปลง gs:// เป็น download URL
        if (car.getImageUrl() != null && car.getImageUrl().startsWith("gs://")) {
            FirebaseStorage.getInstance().getReferenceFromUrl(car.getImageUrl())
                    .getDownloadUrl()
                    .addOnSuccessListener(uri -> {
                        Glide.with(holder.imageView.getContext())
                                .load(uri.toString())
                                .placeholder(R.drawable.t1)
                                .into(holder.imageView);
                    })
                    .addOnFailureListener(e -> {
                        holder.imageView.setImageResource(R.drawable.t1);
                    });
        } else {
            Glide.with(holder.imageView.getContext())
                    .load(car.getImageUrl()) // fallback เผื่อเป็น URL อยู่แล้ว
                    .placeholder(R.drawable.t1)
                    .into(holder.imageView);
        }

        holder.radioButton.setChecked(position == selectedPosition);

        View.OnClickListener selectListener = v -> {
            int previousPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);
            listener.onCarSelected(car);
        };

        holder.itemView.setOnClickListener(selectListener);
        holder.radioButton.setOnClickListener(selectListener);
    }


    @Override
    public int getItemCount() {
        return carList != null ? carList.size() : 0;
    }

    static class CarViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvType, tvCapacity, tvPrice;
        ImageView imageView;
        RadioButton radioButton;

        public CarViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCarName);
            tvType = itemView.findViewById(R.id.tvCarType);
            tvCapacity = itemView.findViewById(R.id.tvCarCapacity);
            tvPrice = itemView.findViewById(R.id.tvCarPrice);
            imageView = itemView.findViewById(R.id.ivCarImage);
            radioButton = itemView.findViewById(R.id.rbSelectCar);
        }
    }
}
