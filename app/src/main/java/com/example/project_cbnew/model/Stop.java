package com.example.project_cbnew.model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Map;

public class Stop implements Parcelable {

    private String name;
    private String description;
    private int imageResource;
    private Double latitude;
    private Double longitude;

    private int durationAtStop = 0;                // หน่วย: นาที
    private int travelTimeToNext = 0;              // เวลาเดินทางไปจุดถัดไป
    private int travelTimeFromPrevious = 0;        // เวลาเดินทางจากจุดก่อนหน้า

    private int durationFromPreviousStopMinutes = 0; // สำหรับคำนวณรวม
    private int durationAtStopMinutes = 0;

    private String imageUrl;                       // รูปจาก Firebase Storage
    private Bitmap photoBitmap;                    // รูปจาก Glide หรือ Bitmap memory

    private Map<String, Object> extraFields;       // สำหรับเก็บ field เพิ่มเติมจาก Firebase

    // --------------------- Constructors ---------------------
    public Stop() {}

    public Stop(String name, String description, int imageResource) {
        this(name, description, imageResource, 0.0, 0.0);
    }

    public Stop(String name, String description, int imageResource, double latitude, double longitude) {
        this.name = name;
        this.description = description;
        this.imageResource = imageResource;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Stop(String name, String description, double latitude, double longitude) {
        this(name, description, 0, latitude, longitude);
    }

    // --------------------- Firebase-compatible ---------------------
    public static Stop fromMap(Map<String, Object> map) {
        if (map == null) return null;

        // ✅ รองรับชื่อจาก Firestore ("TP_PlaceName") หรือ fallback เป็น "name"
        String name = null;
        if (map.containsKey("TP_PlaceName")) {
            name = (String) map.get("TP_PlaceName");
        } else if (map.containsKey("name")) {
            name = (String) map.get("name");
        }
        if (name == null || name.isEmpty()) name = "ไม่ทราบชื่อ";

        String description = null;
        if (map.containsKey("TP_Address")) {
            description = (String) map.get("TP_Address");
        } else if (map.containsKey("description")) {
            description = (String) map.get("description");
        }
        if (description == null) description = "";

        double lat = 0, lng = 0;
        if (map.get("TP_Latitude") instanceof Number) {
            lat = ((Number) map.get("TP_Latitude")).doubleValue();
        } else if (map.get("latitude") instanceof Number) {
            lat = ((Number) map.get("latitude")).doubleValue();
        }

        if (map.get("TP_Longitude") instanceof Number) {
            lng = ((Number) map.get("TP_Longitude")).doubleValue();
        } else if (map.get("longitude") instanceof Number) {
            lng = ((Number) map.get("longitude")).doubleValue();
        }

        Stop stop = new Stop(name, description, lat, lng);
        stop.setExtraFields(map);

        if (map.containsKey("TP_PhotoUrl")) {
            stop.setImageUrl((String) map.get("TP_PhotoUrl"));
        } else if (map.containsKey("imageUrl")) {
            stop.setImageUrl((String) map.get("imageUrl"));
        }

        return stop;
    }


    // --------------------- Parcelable ---------------------
    protected Stop(Parcel in) {
        name = in.readString();
        description = in.readString();
        imageResource = in.readInt();
        latitude = in.readDouble();
        longitude = in.readDouble();
        durationAtStop = in.readInt();
        travelTimeToNext = in.readInt();
        travelTimeFromPrevious = in.readInt();
        durationFromPreviousStopMinutes = in.readInt();
        durationAtStopMinutes = in.readInt();
        imageUrl = in.readString();
        photoBitmap = in.readParcelable(Bitmap.class.getClassLoader());
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(description);
        dest.writeInt(imageResource);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeInt(durationAtStop);
        dest.writeInt(travelTimeToNext);
        dest.writeInt(travelTimeFromPrevious);
        dest.writeInt(durationFromPreviousStopMinutes);
        dest.writeInt(durationAtStopMinutes);
        dest.writeString(imageUrl);
        dest.writeParcelable(photoBitmap, flags);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Stop> CREATOR = new Creator<Stop>() {
        @Override
        public Stop createFromParcel(Parcel in) {
            return new Stop(in);
        }

        @Override
        public Stop[] newArray(int size) {
            return new Stop[size];
        }
    };

    // --------------------- Getters/Setters ---------------------
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getImageResource() { return imageResource; }
    public void setImageResource(int imageResource) { this.imageResource = imageResource; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public int getDurationAtStop() { return durationAtStop; }
    public void setDurationAtStop(int durationAtStop) { this.durationAtStop = durationAtStop; }

    public int getTravelTimeToNext() { return travelTimeToNext; }
    public void setTravelTimeToNext(int travelTimeToNext) { this.travelTimeToNext = travelTimeToNext; }

    public int getTravelTimeFromPrevious() { return travelTimeFromPrevious; }
    public void setTravelTimeFromPrevious(int travelTimeFromPrevious) { this.travelTimeFromPrevious = travelTimeFromPrevious; }

    public int getDurationFromPreviousStopMinutes() { return durationFromPreviousStopMinutes; }
    public void setDurationFromPreviousStopMinutes(int durationFromPreviousStopMinutes) { this.durationFromPreviousStopMinutes = durationFromPreviousStopMinutes; }

    public int getDurationAtStopMinutes() { return durationAtStopMinutes; }
    public void setDurationAtStopMinutes(int durationAtStopMinutes) { this.durationAtStopMinutes = durationAtStopMinutes; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Bitmap getPhotoBitmap() { return photoBitmap; }
    public void setPhotoBitmap(Bitmap photoBitmap) { this.photoBitmap = photoBitmap; }

    public Map<String, Object> getExtraFields() { return extraFields; }
    public void setExtraFields(Map<String, Object> extraFields) { this.extraFields = extraFields; }

}
