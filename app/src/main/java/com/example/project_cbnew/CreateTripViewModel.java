package com.example.project_cbnew;

import java.util.Calendar;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.project_cbnew.model.TripDay;

import java.util.ArrayList;
import java.util.List;

public class CreateTripViewModel extends ViewModel {
    private final MutableLiveData<String> tripTitle = new MutableLiveData<>();
    private final MutableLiveData<String> tripDescription = new MutableLiveData<>();
    private final MutableLiveData<List<TripDay>> tripDays = new MutableLiveData<>(new ArrayList<>());

    private final MutableLiveData<String> tripId = new MutableLiveData<>();
    private MutableLiveData<Calendar> startDate = new MutableLiveData<>(Calendar.getInstance());

    public MutableLiveData<Calendar> getStartDate() {
        return startDate;
    }

    public void setStartDate(Calendar date) {
        startDate.setValue(date);
    }

    public LiveData<String> getTripTitle() {
        return tripTitle;
    }

    public void setTripTitle(String title) {
        tripTitle.setValue(title);
    }

    public LiveData<String> getTripDescription() {
        return tripDescription;
    }

    public void setTripDescription(String description) {
        tripDescription.setValue(description);
    }

    public LiveData<List<TripDay>> getTripDays() {
        return tripDays;
    }

    public void setTripDays(List<TripDay> days) {
        tripDays.setValue(new ArrayList<>(days));
    }


    public List<TripDay> getTripDayList() {
        List<TripDay> value = tripDays.getValue();
        return value != null ? new ArrayList<>(value) : new ArrayList<>();
    }

    public void updateDay(int dayIndex, TripDay updatedDay) {
        List<TripDay> currentDays = tripDays.getValue();
        if (currentDays != null && dayIndex >= 0 && dayIndex < currentDays.size()) {
            currentDays.set(dayIndex, updatedDay);
            tripDays.setValue(new ArrayList<>(currentDays)); // ✅ สำคัญมาก ต้อง set ใหม่เสมอ
        }
    }

    public void addEmptyDays(int count) {
        List<TripDay> current = tripDays.getValue();
        if (current == null) current = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            current.add(new TripDay("วัน " + (current.size() + 1)));
        }
        tripDays.setValue(new ArrayList<>(current));
    }

    public void addEmptyDay() {
        addEmptyDays(1);
    }

    public TripDay getTripDay(int index) {
        List<TripDay> currentDays = tripDays.getValue();
        if (currentDays != null && index >= 0 && index < currentDays.size()) {
            return currentDays.get(index);
        }
        return new TripDay("วัน " + (index + 1));
    }

    public void removeLastDay() {
        List<TripDay> list = tripDays.getValue();
        if (list != null && !list.isEmpty()) {
            list.remove(list.size() - 1);
            tripDays.setValue(new ArrayList<>(list));
        }
    }

    public void refreshTripDays() {
        List<TripDay> current = tripDays.getValue();
        if (current != null) {
            tripDays.setValue(new ArrayList<>(current)); // ✅ สำคัญมาก ต้องเป็น List ใหม่เสมอ
        }
    }

    public void clearTripDays() {
        tripDays.setValue(new ArrayList<>());
    }

    public void addTripDay(TripDay tripDay) {
        List<TripDay> currentDays = tripDays.getValue();
        if (currentDays == null) {
            currentDays = new ArrayList<>();
        }
        currentDays.add(tripDay);
        tripDays.setValue(new ArrayList<>(currentDays)); // อัปเดต LiveData ใหม่
    }

    public MutableLiveData<String> getTripId() {
        return tripId;
    }

    public void setTripId(String id) {
        tripId.setValue(id);
    }

    public void clear() {
        tripTitle.setValue(null);
        tripDescription.setValue(null);
        tripDays.setValue(new ArrayList<>());
        startDate.setValue(null);
        tripId.setValue(null);
    }




}