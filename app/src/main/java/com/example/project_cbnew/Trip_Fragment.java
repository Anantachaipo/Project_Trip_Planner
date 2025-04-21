package com.example.project_cbnew;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_cbnew.adapter.MyAdepter;
import com.example.project_cbnew.hardcodetrip.HardcodedTripProvider;
import com.example.project_cbnew.model.Trip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Trip_Fragment extends Fragment {

    private RecyclerView recyclerView;
    private SearchView searchView;
    private Button btnAllTrips, btnRecommended, btnMyTrips;
    private MyAdepter adapter;

    private List<Trip> recommendedTrips = new ArrayList<>();
    private List<Trip> myTrips = new ArrayList<>();
    private List<Trip> currentDisplayList = new ArrayList<>();
    private String currentFilter = "all";

    public Trip_Fragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trip, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerViewTrip);
        searchView = view.findViewById(R.id.searchView);
        btnAllTrips = view.findViewById(R.id.btnAllTrips);
        btnRecommended = view.findViewById(R.id.btnRecommended);
        btnMyTrips = view.findViewById(R.id.btnMyTrips);


        // ✅ เปิดช่องพิมพ์เสมอ
        searchView.setIconified(false);
        searchView.clearFocus(); // กันคีย์บอร์ดเด้ง
        searchView.requestFocusFromTouch(); // บางรุ่นต้องบังคับ

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MyAdepter(getContext(), currentDisplayList, trip -> {
            TripDetails_Fragment detailsFragment = TripDetails_Fragment.newInstance(trip);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frameLayout, detailsFragment)
                    .addToBackStack(null)
                    .commit();
        });
        recyclerView.setAdapter(adapter);

        loadRecommendedTrips();
        loadUserTrips();

        view.findViewById(R.id.btnCreateTrip).setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frameLayout, new Create_Trip_Fragment())
                    .addToBackStack(null)
                    .commit();
        });

        btnAllTrips.setOnClickListener(v -> {
            currentFilter = "all";
            updateDisplayList();
        });

        btnRecommended.setOnClickListener(v -> {
            currentFilter = "recommended";
            updateDisplayList();
        });

        btnMyTrips.setOnClickListener(v -> {
            currentFilter = "my";
            updateDisplayList();
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }
            @Override
            public boolean onQueryTextChange(String newText) {
                filterSearchResults(newText);
                return true;
            }
        });
    }

    private void updateDisplayList() {
        currentDisplayList.clear();
        if (currentFilter.equals("recommended")) {
            currentDisplayList.addAll(recommendedTrips);
        } else if (currentFilter.equals("my")) {
            currentDisplayList.addAll(myTrips);
        } else {
            currentDisplayList.addAll(recommendedTrips);
            currentDisplayList.addAll(myTrips);
        }
        adapter.updateList(currentDisplayList);
    }

    private void filterSearchResults(String query) {
        if (TextUtils.isEmpty(query)) {
            updateDisplayList();
            return;
        }

        List<Trip> filtered = new ArrayList<>();
        for (Trip trip : currentDisplayList) {
            if (trip.getTitle().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(trip);
            }
        }
        adapter.updateList(filtered);
    }

    private void loadRecommendedTrips() {
        recommendedTrips = HardcodedTripProvider.getTrips(getContext());
        updateDisplayList();
    }

    private void loadUserTrips() {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "";

        FirebaseFirestore.getInstance().collection("trips")
                .whereEqualTo("createdBy", uid)
                .get()
                .addOnSuccessListener(query -> {
                    myTrips.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        Trip trip = Trip.fromFirestore(doc);
                        myTrips.add(trip);
                    }
                    updateDisplayList();
                });
    }
    private void replaceFragment(Fragment fragment) {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.frameLayout, fragment)
                .addToBackStack(null)
                .commit();
    }
}

