package com.example.project_cbnew.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.example.project_cbnew.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.graphics.Bitmap;

public class Trip implements Parcelable {
    // ------------------ [FIELDS] ------------------
    private String tripId;
    private String title;
    private String description;
    private String imageUrl;
    private int imageResourceID; // สำหรับ hardcoded trip
    private int days;
    private int distance;
    private int price;
    private String startLocation;
    private String startDate;
    private String createdBy;
    private Bitmap photoBitmap; // ✅ เพิ่มรูปแบบ bitmap จาก stop (ใช้ใน adapter)

    private List<Stop> stops;
    private List<TripDay> tripDays;
    private Map<String, Object> extraFields = new HashMap<>();

    // ------------------ [CONSTRUCTORS] ------------------
    public Trip() {} // default

    public Trip(int imageResourceID, String title, String description, int days, String startLocation, int distance, int price, List<Stop> stops) {
        this.imageResourceID = imageResourceID;
        this.title = title;
        this.description = description;
        this.days = days;
        this.startLocation = startLocation;
        this.distance = distance;
        this.price = price;
        this.stops = stops;
    }

    protected Trip(Parcel in) {
        title = in.readString();
        description = in.readString();
        imageResourceID = in.readInt();
        days = in.readInt();
        startLocation = in.readString();
        distance = in.readInt();
        price = in.readInt();
        stops = in.createTypedArrayList(Stop.CREATOR);
        createdBy = in.readString();
        tripDays = in.createTypedArrayList(TripDay.CREATOR);
        startDate = in.readString();
        imageUrl = in.readString();
        tripId = in.readString();
    }

    public static final Creator<Trip> CREATOR = new Creator<Trip>() {
        @Override public Trip createFromParcel(Parcel in) { return new Trip(in); }
        @Override public Trip[] newArray(int size) { return new Trip[size]; }
    };

    // ------------------ [GETTERS & SETTERS] ------------------
    public String getTripId() { return tripId; }
    public void setTripId(String tripId) { this.tripId = tripId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getImageResourceID() { return imageResourceID; }
    public void setImageResourceID(int imageResourceID) { this.imageResourceID = imageResourceID; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getDays() { return days; }
    public void setDays(int days) { this.days = days; }

    public int getDistance() { return distance; }
    public void setDistance(int distance) { this.distance = distance; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    public String getStartLocation() { return startLocation; }
    public void setStartLocation(String startLocation) { this.startLocation = startLocation; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public Bitmap getPhotoBitmap() {
        return photoBitmap;
    }

    public void setPhotoBitmap(Bitmap photoBitmap) {
        this.photoBitmap = photoBitmap;
    }

    public List<Stop> getStops() {
        if (stops != null && !stops.isEmpty()) return stops;
        if (tripDays != null && !tripDays.isEmpty()) {
            List<Stop> merged = new ArrayList<>();
            for (TripDay day : tripDays) merged.addAll(day.getStops());
            return merged;
        }
        return new ArrayList<>();
    }

    public void setStops(List<Stop> stops) { this.stops = stops; }

    public List<TripDay> getTripDays() { return tripDays; }
    public void setTripDays(List<TripDay> tripDays) { this.tripDays = tripDays; }

    public Map<String, Object> getExtraFields() { return extraFields; }
    public void setExtraFields(Map<String, Object> extraFields) { this.extraFields = extraFields; }

    // ------------------ [PARCELABLE] ------------------
    @Override public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(description);
        dest.writeInt(imageResourceID);
        dest.writeInt(days);
        dest.writeString(startLocation);
        dest.writeInt(distance);
        dest.writeInt(price);
        dest.writeTypedList(stops);
        dest.writeString(createdBy);
        dest.writeTypedList(tripDays);
        dest.writeString(startDate);
        dest.writeString(imageUrl);
        dest.writeString(tripId);
    }




    //
    public static Trip fromFirestore(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return null;

        Trip trip = new Trip();
        trip.setTitle(doc.getString("title"));
        trip.setDescription(doc.getString("description"));
        trip.setStartLocation(doc.getString("startLocation"));
        trip.setImageUrl(doc.getString("imageUrl"));
        trip.setTripDays((List<TripDay>) doc.get("tripDays")); // ต้องแน่ใจว่า TripDay implements Parcelable ด้วย

        return trip;
    }
    @SuppressWarnings("unchecked")
    public static Trip fromFirestore(QueryDocumentSnapshot doc) {
        Trip trip = new Trip();
        trip.title = doc.getString("T_Name");
        trip.description = doc.getString("T_Description");
        trip.createdBy = doc.getString("createdBy");
        trip.days = doc.contains("T_Days") ? doc.getLong("T_Days").intValue() : 1;
        trip.distance = doc.contains("T_Distance") ? doc.getLong("T_Distance").intValue() : 0;
        trip.price = 0;
        trip.startLocation = doc.contains("T_StartLocation") ? doc.getString("T_StartLocation") : "";
        trip.setTripId(doc.getId());
        trip.setStartDate(doc.getString("T_StartDate"));
        trip.setExtraFields(doc.getData());


        List<Stop> stops = new ArrayList<>();
        if (doc.contains("Trip_Places")) {
            List<?> stopList = (List<?>) doc.get("Trip_Places");
            if (stopList != null) {
                for (Object obj : stopList) {
                    if (obj instanceof Map) {
                        Map<String, Object> stopMap = (Map<String, Object>) obj;

                        String name = (String) stopMap.get("TP_PlaceName");
                        String desc = (String) stopMap.get("TP_Address");

                        double lat = 0, lng = 0;
                        Object latObj = stopMap.get("TP_Latitude");
                        Object lngObj = stopMap.get("TP_Longitude");

                        if (latObj instanceof Number) lat = ((Number) latObj).doubleValue();
                        if (lngObj instanceof Number) lng = ((Number) lngObj).doubleValue();

                        Stop stop = new Stop(name, desc, lat, lng);
                        stop.setExtraFields(stopMap);

                        String imageUrl = null;
                        if (stopMap.containsKey("TP_PhotoUrl")) {
                            imageUrl = (String) stopMap.get("TP_PhotoUrl");
                        } else if (stopMap.containsKey("TP_ImageUrl")) {
                            imageUrl = (String) stopMap.get("TP_ImageUrl");
                        }

                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            stop.setImageUrl(imageUrl);
                            Log.d("TripFromFirestore", "✅ Stop: " + name + " | imageUrl = " + imageUrl);
                        } else {
                            Log.d("TripFromFirestore", "❌ Stop: " + name + " ไม่มีรูป");
                        }

                        stop.setImageResource(R.drawable.t1);
                        stops.add(stop);
                    }
                }
            }
        }

        trip.stops = stops;

        // ✅ แยก Stop เป็นหลายวัน
        Map<Integer, List<Stop>> stopsByDay = new HashMap<>();
        for (Stop stop : stops) {
            int dayIndex = 0;
            if (stop.getExtraFields() != null && stop.getExtraFields().containsKey("TP_Day")) {
                Object dayObj = stop.getExtraFields().get("TP_Day");
                if (dayObj instanceof Number) {
                    dayIndex = ((Number) dayObj).intValue() - 1;
                }
            }
            if (!stopsByDay.containsKey(dayIndex)) {
                stopsByDay.put(dayIndex, new ArrayList<>());
            }
            stopsByDay.get(dayIndex).add(stop);

        }

        // ✅ โหลดระยะทางและเวลาพัก/เดินทาง รายวันจาก Firestore
        List<Long> distanceList = (List<Long>) doc.get("T_DayDistances");
        List<Long> restList = (List<Long>) doc.get("T_DayRestMinutes");
        List<Long> durationList = (List<Long>) doc.get("T_DayDurations");  // <== ใหม่!

        trip.tripDays = new ArrayList<>();
        List<Integer> sortedKeys = new ArrayList<>(stopsByDay.keySet());
        Collections.sort(sortedKeys);
        for (int i = 0; i < sortedKeys.size(); i++) {
            int dayIndex = sortedKeys.get(i);
            List<Stop> dayStops = stopsByDay.get(dayIndex);
            TripDay day = new TripDay("วัน " + (dayIndex + 1), dayStops);

            if (distanceList != null && dayIndex < distanceList.size()) {
                day.setTotalDistanceKm(distanceList.get(dayIndex).intValue());
            }
            if (restList != null && dayIndex < restList.size()) {
                day.setTotalRestMinutes(restList.get(dayIndex).intValue());
            }
            if (durationList != null && dayIndex < durationList.size()) {
                day.setTotalTravelMinutes(durationList.get(dayIndex).intValue()); // ✅ ใส่ field ใหม่ตรงนี้
            }

            trip.tripDays.add(day);
        }

        if ((trip.startLocation == null || trip.startLocation.isEmpty()) && !stops.isEmpty()) {
            trip.startLocation = stops.get(0).getName();
        }

        return trip;
    }

    public static Trip fromBookingTripDoc(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return null;

        Trip trip = new Trip();
        trip.setTitle(doc.getString("T_Name"));
        trip.setDescription(doc.getString("T_Description"));
        trip.setStartLocation(doc.getString("T_StartLocation"));
        trip.setImageUrl(doc.getString("imageUrl")); // optional

        List<Map<String, Object>> rawStops = (List<Map<String, Object>>) doc.get("Trip_Places");
        List<Stop> stops = new ArrayList<>();
        if (rawStops != null) {
            for (Map<String, Object> stopMap : rawStops) {
                Stop stop = Stop.fromMap(stopMap);
                stops.add(stop);
            }
        }
        trip.setStops(stops);

        // แยก Stop เป็น TripDay
        Map<Integer, List<Stop>> stopsByDay = new HashMap<>();
        for (Stop stop : stops) {
            int dayIndex = 0;
            if (stop.getExtraFields().containsKey("TP_Day")) {
                Object dayObj = stop.getExtraFields().get("TP_Day");
                if (dayObj instanceof Number) {
                    dayIndex = ((Number) dayObj).intValue() - 1;
                }
            }
            if (!stopsByDay.containsKey(dayIndex)) {
                stopsByDay.put(dayIndex, new ArrayList<>());
            }
            stopsByDay.get(dayIndex).add(stop);

        }

        List<Long> distanceList = (List<Long>) doc.get("T_DayDistances");
        List<Long> durationList = (List<Long>) doc.get("T_DayDurations");
        List<Long> restList = (List<Long>) doc.get("T_DayRestMinutes");

        List<TripDay> tripDays = new ArrayList<>();
        List<Integer> sortedKeys = new ArrayList<>(stopsByDay.keySet());
        Collections.sort(sortedKeys);

        for (int i = 0; i < sortedKeys.size(); i++) {
            int dayIndex = sortedKeys.get(i);
            TripDay day = new TripDay("วัน " + (dayIndex + 1), stopsByDay.get(dayIndex));

            if (distanceList != null && dayIndex < distanceList.size())
                day.setTotalDistanceKm(distanceList.get(dayIndex).intValue());
            if (durationList != null && dayIndex < durationList.size())
                day.setTotalTravelMinutes(durationList.get(dayIndex).intValue());
            if (restList != null && dayIndex < restList.size())
                day.setTotalRestMinutes(restList.get(dayIndex).intValue());

            tripDays.add(day);
        }

        trip.setTripDays(tripDays);
        return trip;
    }



}
