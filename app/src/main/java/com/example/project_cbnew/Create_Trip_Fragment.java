package com.example.project_cbnew;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.project_cbnew.adapter.CreateTripPagerAdapter;
import com.example.project_cbnew.model.Trip;
import com.example.project_cbnew.model.TripDay;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.List;

public class Create_Trip_Fragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private CreateTripViewModel viewModel;

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private CreateTripPagerAdapter adapter;

    public Create_Trip_Fragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(CreateTripViewModel.class);
        if (viewModel.getTripDayList().isEmpty()) {
            viewModel.addEmptyDay();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_create_trip, container, false);

        // ✅ ตรวจสอบว่ามี Trip ที่ส่งมาสำหรับการแก้ไขหรือไม่
        if (getArguments() != null && getArguments().containsKey("trip_to_edit")) {
            Trip tripToEdit = getArguments().getParcelable("trip_to_edit");
            if (tripToEdit != null) {
                viewModel.setTripId(null);
                viewModel.setTripTitle(tripToEdit.getTitle());
                viewModel.setTripDescription(tripToEdit.getDescription());
                viewModel.setTripId(tripToEdit.getTripId());


                viewModel.clearTripDays(); // ล้างของเดิมออกก่อน

                // ✅ โหลด TripDays ที่แยกวันไว้แล้ว
                if (tripToEdit.getTripDays() != null && !tripToEdit.getTripDays().isEmpty()) {
                    for (TripDay day : tripToEdit.getTripDays()) {
                        viewModel.addTripDay(day);
                    }
                } else {
                    // ⛑ fallback: ถ้า tripDays ยังไม่มี ให้โยนลงวันแรก
                    TripDay fallbackDay = new TripDay("วัน 1");
                    fallbackDay.setStops(tripToEdit.getStops());
                    fallbackDay.setTotalDistanceKm(tripToEdit.getDistance());
                    viewModel.addTripDay(fallbackDay);
                }
            }

        }

        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);
        Button btnRemoveDay = view.findViewById(R.id.btnRemoveDay);

        adapter = new CreateTripPagerAdapter(this);
        adapter.addPage("ภาพรวม");

        List<TripDay> dayList = viewModel.getTripDayList();
        for (int i = 0; i < dayList.size(); i++) {
            String dayTitle = "วัน " + (i + 1);
            if (!adapter.getPageTitles().contains(dayTitle)) {
                adapter.addPage(dayTitle);
            }
        }

        if (!adapter.getPageTitles().contains("➕")) {
            adapter.addPlusPage();
        }

        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(adapter.getItemCount());

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(adapter.getPageTitle(position))
        ).attach();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();

                Log.d("CreateTrip", "Selected tab: " + tab.getText() + ", position: " + position);
                Log.d("CreateTrip", "Current tab title: " + adapter.getPageTitle(position));

                if ("➕".equals(tab.getText())) {
                    int newDayNumber = adapter.getDayTabCount() + 1;
                    viewModel.addEmptyDay();
                    adapter.removePage("➕");
                    adapter.addPage("วัน " + newDayNumber);
                    adapter.addPlusPage();
                    adapter.notifyDataSetChanged();

                    Log.d("CreateTrip", "✅ เพิ่มวันใหม่: วัน " + newDayNumber);

                    viewPager.setCurrentItem(adapter.getItemCount() - 2, true);
                } else {
                    updateMapForCurrentPage();
                }
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {
                updateMapForCurrentPage();
            }
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateMapForCurrentPage();
            }
        });

        btnRemoveDay.setOnClickListener(v -> removeLastDayTab());

        setupGoogleMap();
        return view;
    }


    private void removeLastDayTab() {
        int dayCount = viewModel.getTripDayList().size();
        if (dayCount > 1) {
            adapter.removeLastPage();
            viewModel.removeLastDay();
            String dayTitleToRemove = "วัน " + dayCount;
            adapter.removePage(dayTitleToRemove);
            adapter.addPlusPage();
            adapter.notifyDataSetChanged();
            viewPager.setCurrentItem(adapter.getItemCount() - 2, true);
        } else {
            Toast.makeText(getContext(), "ไม่สามารถลบวันสุดท้ายได้", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupGoogleMap() {
        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        getChildFragmentManager().beginTransaction()
                .replace(R.id.mapContainer, mapFragment)
                .commitNowAllowingStateLoss();
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap gMap) {
        googleMap = gMap;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(true);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(13.7563, 100.5018), 10f));
        updateMapForCurrentPage();
    }

    private void updateMapForCurrentPage() {
        if (googleMap == null) return;
        int position = viewPager.getCurrentItem();

        Fragment fragment = adapter.getFragmentAt(position);
        Log.d("CreateTrip", "Updating map for position: " + position + ", fragment: " + fragment);

        if (fragment instanceof Trip_Day_Fragment) {
            ((Trip_Day_Fragment) fragment).setSharedMap(googleMap);
            Log.d("CreateTrip", "✅ Set shared map for day fragment");
        } else if (fragment instanceof Trip_Overview_Fragment) {
            ((Trip_Overview_Fragment) fragment).setSharedMap(googleMap);
            Log.d("CreateTrip", "✅ Set shared map for overview");
        } else {
            Log.w("CreateTrip", "❌ Fragment not ready");
        }
    }
    public static Create_Trip_Fragment newInstance(Trip tripToEdit) {
        Create_Trip_Fragment fragment = new Create_Trip_Fragment();
        Bundle args = new Bundle();
        args.putParcelable("trip_to_edit", tripToEdit);
        fragment.setArguments(args);
        return fragment;
    }

}
