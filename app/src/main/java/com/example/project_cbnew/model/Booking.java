package com.example.project_cbnew.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Booking implements Parcelable {

    private String bookingId;
    private String accountId;
    private String tripId;
    private String vanId;

    private String bookingDate;
    private String typeVehicle;
    private String journeyStart;
    private String journeyEnd;

    private String startDate;
    private String endDate;

    private int totalDistance;
    private String note;
    private String status;
    private String seats;

    private String tripTitle;
    private String driverId;
    private Double driverRating;       // ⭐ คะแนน (nullable)
    private String driverReviewNote;   // 💬 รีวิว
    private int stability;

    public Booking() {
        // ตั้งค่าเริ่มต้นให้ไม่ crash
        this.driverRating = 0.0;
        this.driverReviewNote = "";
    }

    protected Booking(Parcel in) {
        bookingId = in.readString();
        accountId = in.readString();
        tripId = in.readString();
        vanId = in.readString();
        bookingDate = in.readString();
        typeVehicle = in.readString();
        journeyStart = in.readString();
        journeyEnd = in.readString();
        startDate = in.readString();
        endDate = in.readString();
        totalDistance = in.readInt();
        note = in.readString();
        status = in.readString();
        seats = in.readString();
        tripTitle = in.readString();
        driverId = in.readString();
        driverRating = (Double) in.readValue(Double.class.getClassLoader());
        driverReviewNote = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(bookingId);
        dest.writeString(accountId);
        dest.writeString(tripId);
        dest.writeString(vanId);
        dest.writeString(bookingDate);
        dest.writeString(typeVehicle);
        dest.writeString(journeyStart);
        dest.writeString(journeyEnd);
        dest.writeString(startDate);
        dest.writeString(endDate);
        dest.writeInt(totalDistance);
        dest.writeString(note);
        dest.writeString(status);
        dest.writeString(seats);
        dest.writeString(tripTitle);
        dest.writeString(driverId);
        dest.writeValue(driverRating);
        dest.writeString(driverReviewNote);
    }

    public static final Creator<Booking> CREATOR = new Creator<Booking>() {
        @Override
        public Booking createFromParcel(Parcel in) {
            return new Booking(in);
        }

        @Override
        public Booking[] newArray(int size) {
            return new Booking[size];
        }
    };

    // ✅ Getter/Setter
    public String getTripTitle() { return tripTitle; }
    public void setTripTitle(String tripTitle) { this.tripTitle = tripTitle; }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }

    public String getTripId() { return tripId; }
    public void setTripId(String tripId) { this.tripId = tripId; }

    public String getVanId() { return vanId; }
    public void setVanId(String vanId) { this.vanId = vanId; }

    public String getBookingDate() { return bookingDate; }
    public void setBookingDate(String bookingDate) { this.bookingDate = bookingDate; }

    public String getTypeVehicle() { return typeVehicle; }
    public void setTypeVehicle(String typeVehicle) { this.typeVehicle = typeVehicle; }

    public String getJourneyStart() { return journeyStart; }
    public void setJourneyStart(String journeyStart) { this.journeyStart = journeyStart; }

    public String getJourneyEnd() { return journeyEnd; }
    public void setJourneyEnd(String journeyEnd) { this.journeyEnd = journeyEnd; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public int getTotalDistance() { return totalDistance; }
    public void setTotalDistance(int totalDistance) { this.totalDistance = totalDistance; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSeats() { return seats; }
    public void setSeats(String seats) { this.seats = seats; }

    public String getDriverId() { return driverId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }

    public Double getDriverRating() { return driverRating; }
    public void setDriverRating(Double driverRating) { this.driverRating = driverRating; }

    public String getDriverReviewNote() { return driverReviewNote; }
    public void setDriverReviewNote(String driverReviewNote) { this.driverReviewNote = driverReviewNote; }

    public int getStability() { return stability; }
    public void setStability(int stability) { this.stability = stability; }

    @Override public int describeContents() { return 0; }
}
