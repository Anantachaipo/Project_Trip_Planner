package com.example.project_cbnew.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Car implements Parcelable {
    private String id; // ✅ New: Firestore Document ID
    private String name;
    private String type;
    private int capacity;
    private int price;
    private String imageUrl;
    private boolean isAvailable = true;

    public Car(String id, String name, String type, int capacity, int price, String imageUrl, boolean isAvailable) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.capacity = capacity;
        this.price = price;
        this.imageUrl = imageUrl;
        this.isAvailable = isAvailable;
    }

    protected Car(Parcel in) {
        id = in.readString();
        name = in.readString();
        type = in.readString();
        capacity = in.readInt();
        price = in.readInt();
        imageUrl = in.readString();
        isAvailable = in.readByte() != 0;
    }

    public static final Creator<Car> CREATOR = new Creator<Car>() {
        @Override
        public Car createFromParcel(Parcel in) {
            return new Car(in);
        }

        @Override
        public Car[] newArray(int size) {
            return new Car[size];
        }
    };

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public String getType() { return type; }
    public int getCapacity() { return capacity; }
    public int getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(id);
        parcel.writeString(name);
        parcel.writeString(type);
        parcel.writeInt(capacity);
        parcel.writeInt(price);
        parcel.writeString(imageUrl);
        parcel.writeByte((byte) (isAvailable ? 1 : 0));
    }
}

