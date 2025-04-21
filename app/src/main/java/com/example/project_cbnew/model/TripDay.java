package com.example.project_cbnew.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class TripDay implements Parcelable {

    private String title;
    private List<Stop> stops = new ArrayList<>();
    private List<Stop> originalStops = new ArrayList<>();

    private int totalDistanceKm = 0;
    private int totalRestMinutes = 0;
    private int totalTravelMinutes = 0;

    // ------------------- Constructors -------------------
    public TripDay() {} // default constructor (Firebase needs this)

    public TripDay(String title) {
        this.title = title;
    }

    public TripDay(String title, List<Stop> stops) {
        this.title = title;
        this.stops = stops;
    }

    // ------------------- Parcelable Constructor -------------------
    protected TripDay(Parcel in) {
        title = in.readString();
        stops = in.createTypedArrayList(Stop.CREATOR);
        originalStops = in.createTypedArrayList(Stop.CREATOR);
        totalDistanceKm = in.readInt();
        totalRestMinutes = in.readInt();
        totalTravelMinutes = in.readInt();
    }

    public static final Creator<TripDay> CREATOR = new Creator<TripDay>() {
        @Override public TripDay createFromParcel(Parcel in) {
            return new TripDay(in);
        }

        @Override public TripDay[] newArray(int size) {
            return new TripDay[size];
        }
    };

    // ------------------- Parcelable Methods -------------------
    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeTypedList(stops);
        dest.writeTypedList(originalStops);
        dest.writeInt(totalDistanceKm);
        dest.writeInt(totalRestMinutes);
        dest.writeInt(totalTravelMinutes);
    }

    @Override public int describeContents() {
        return 0;
    }

    // ------------------- Getters/Setters -------------------
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public List<Stop> getStops() { return stops; }
    public void setStops(List<Stop> stops) { this.stops = stops; }

    public void addStop(Stop stop) {
        if (this.stops == null) this.stops = new ArrayList<>();
        this.stops.add(stop);
    }

    public void removeStop(int index) {
        if (stops != null && index >= 0 && index < stops.size()) {
            this.stops.remove(index);
        }
    }

    public List<Stop> getOriginalStops() { return originalStops; }
    public void setOriginalStops(List<Stop> originalStops) { this.originalStops = originalStops; }

    public int getTotalDistanceKm() { return totalDistanceKm; }
    public void setTotalDistanceKm(int totalDistanceKm) { this.totalDistanceKm = totalDistanceKm; }

    public int getTotalRestMinutes() { return totalRestMinutes; }
    public void setTotalRestMinutes(int totalRestMinutes) { this.totalRestMinutes = totalRestMinutes; }

    public int getTotalTravelMinutes() { return totalTravelMinutes; }
    public void setTotalTravelMinutes(int totalTravelMinutes) { this.totalTravelMinutes = totalTravelMinutes; }
}
