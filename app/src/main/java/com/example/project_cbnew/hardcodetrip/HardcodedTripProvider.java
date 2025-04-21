package com.example.project_cbnew.hardcodetrip;

import android.content.Context;

import com.example.project_cbnew.R;
import com.example.project_cbnew.model.Stop;
import com.example.project_cbnew.model.Trip;
import com.example.project_cbnew.model.TripDay;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HardcodedTripProvider {

    public static List<Trip> getTrips(Context context) {
        List<Trip> tripList = new ArrayList<>();
        Random random = new Random();

        String[] tripTitles = {
                context.getString(R.string.trip_1),
                context.getString(R.string.trip_2),
                context.getString(R.string.trip_3),
                context.getString(R.string.trip_4),
                context.getString(R.string.trip_5),
                context.getString(R.string.trip_6)
        };

        String[] tripDescriptions = {
                context.getString(R.string.description_1),
                context.getString(R.string.description_2),
                context.getString(R.string.description_3),
                context.getString(R.string.description_4),
                context.getString(R.string.description_5),
                context.getString(R.string.description_6)
        };

        int[] tripDays = { 3, 4, 2, 2, 2, 2 };

        int[] imageResourceIDs = {
                R.drawable.t1, R.drawable.t2, R.drawable.t3,
                R.drawable.t4, R.drawable.t5, R.drawable.t6
        };

        String[] startLocations = {
                "กรุงเทพฯ", "เชียงใหม่", "พัทยา", "กระบี่", "สุโขทัย", "ภูเก็ต"
        };

        int[] distances = { 250, 300, 200, 400, 350, 280 };
        int[] prices    = { 3500, 4000, 3200, 5000, 4500, 4200 };

        int[] stopNameArrays = {
                R.array.trip_1_stops, R.array.trip_2_stops, R.array.trip_3_stops,
                R.array.trip_4_stops, R.array.trip_5_stops, R.array.trip_6_stops
        };

        int[] stopDescArrays = {
                R.array.trip_1_stop_descriptions, R.array.trip_2_stop_descriptions,
                R.array.trip_3_stop_descriptions, R.array.trip_4_stop_descriptions,
                R.array.trip_5_stop_descriptions, R.array.trip_6_stop_descriptions
        };

        int[][] stopImageResources = {
                {
                        R.drawable.place_11, R.drawable.place_12, R.drawable.place_13,
                        R.drawable.place_14, R.drawable.place_15, R.drawable.place_16,
                        R.drawable.place_17, R.drawable.place_18, R.drawable.place_19,
                        R.drawable.place_20
                },
                {
                        R.drawable.place_21, R.drawable.place_22, R.drawable.place_23,
                        R.drawable.place_24, R.drawable.place_25, R.drawable.place_26,
                        R.drawable.place_27, R.drawable.place_28, R.drawable.place_29,
                        R.drawable.place_30
                },
                {
                        R.drawable.place_31, R.drawable.place_32, R.drawable.place_33,
                        R.drawable.place_34, R.drawable.place_35
                },
                {
                        R.drawable.place_36, R.drawable.place_37, R.drawable.place_38
                },
                {
                        R.drawable.place_39, R.drawable.place_40, R.drawable.place_41
                },
                {
                        R.drawable.place_42, R.drawable.place_43, R.drawable.place_44
                }
        };

        for (int i = 0; i < tripTitles.length; i++) {
            // build days & stops
            String[] names = context.getResources().getStringArray(stopNameArrays[i]);
            String[] descs = context.getResources().getStringArray(stopDescArrays[i]);
            int[] images    = stopImageResources[i];
            int  stopCount  = Math.min(names.length, images.length);

            int days        = tripDays[i];
            int stopsPerDay = (int) Math.ceil((double) stopCount / days);

            List<TripDay> tripDayList = new ArrayList<>();
            int stopIndex = 0;

            for (int d = 0; d < days; d++) {
                List<Stop> stopsForDay = new ArrayList<>();
                int totalDistance = 0;
                int totalRest     = 0;

                for (int s = 0; s < stopsPerDay && stopIndex < stopCount; s++, stopIndex++) {
                    int imgResId = images[stopIndex];
                    // Use constructor to set imageResource directly
                    Stop stop = new Stop(names[stopIndex], descs[stopIndex], imgResId);

                    // set coordinates
                    stop.setLatitude(getLat(i, stopIndex));
                    stop.setLongitude(getLng(i, stopIndex));

                    // durations
                    if (s == 0) {
                        stop.setDurationFromPreviousStopMinutes(0);
                    } else {
                        int travelMin = 10 + random.nextInt(21);
                        stop.setDurationFromPreviousStopMinutes(travelMin);
                        totalDistance += travelMin;
                    }
                    int restMin = 20 + random.nextInt(41);
                    stop.setDurationAtStopMinutes(restMin);
                    totalRest += restMin;

                    stopsForDay.add(stop);
                }

                TripDay tripDay = new TripDay("วัน " + (d + 1), stopsForDay);
                tripDay.setTotalDistanceKm(totalDistance);
                tripDay.setTotalRestMinutes(totalRest);
                tripDayList.add(tripDay);
            }

            // assemble Trip
            Trip trip = new Trip(
                    imageResourceIDs[i],
                    tripTitles[i],
                    tripDescriptions[i],
                    days,
                    startLocations[i],
                    distances[i],
                    prices[i],
                    new ArrayList<>()
            );
            trip.setTripDays(tripDayList);
            trip.setStops(flattenAllStops(tripDayList));

            tripList.add(trip);
        }

        return tripList;
    }

    private static double getLat(int tripIndex, int stopIndex) {
        double[][] latData = {
                {13.7514, 13.7500, 13.7596, 13.7420, 13.5189, 13.7390, 13.7436, 13.7516, 13.7613, 13.8150},
                {18.8072, 18.7414, 18.7877, 18.7962, 18.7888, 18.7866, 18.9337, 18.8658, 18.8055, 18.5883},
                {12.9276, 12.9310, 12.8666, 12.9443, 12.9205, 12.9806},
                {7.7406, 8.1066, 7.9884},
                {17.0159, 17.0146, 17.0172},
                {7.7600, 7.9025, 7.8466}
        };
        if (tripIndex < latData.length && stopIndex < latData[tripIndex].length) {
            return latData[tripIndex][stopIndex];
        }
        return 0.0;
    }

    private static double getLng(int tripIndex, int stopIndex) {
        double[][] lngData = {
                {100.4928, 100.4913, 100.4970, 100.5070, 99.9531, 100.5108, 100.4889, 100.4919, 100.4911, 100.5531},
                {98.9215, 98.9265, 99.0000, 98.9675, 98.9818, 99.0016, 98.8240, 99.3633, 98.9546, 98.4816},
                {100.8771, 100.7727, 100.9020, 100.8880, 100.8691, 100.8735},
                {98.7784, 98.7165, 98.8214},
                {99.8215, 99.8209, 99.8132},
                {98.3032, 98.2955, 98.3382}
        };
        if (tripIndex < lngData.length && stopIndex < lngData[tripIndex].length) {
            return lngData[tripIndex][stopIndex];
        }
        return 0.0;
    }

    private static List<Stop> flattenAllStops(List<TripDay> tripDays) {
        List<Stop> allStops = new ArrayList<>();
        for (TripDay day : tripDays) {
            if (day.getStops() != null) {
                allStops.addAll(day.getStops());
            }
        }
        return allStops;
    }
}
