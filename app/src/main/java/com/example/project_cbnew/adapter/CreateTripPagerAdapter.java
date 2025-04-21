package com.example.project_cbnew.adapter;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.project_cbnew.Trip_Day_Fragment;
import com.example.project_cbnew.Trip_Overview_Fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateTripPagerAdapter extends FragmentStateAdapter {

    private final List<String> pageTitles = new ArrayList<>();
    private final Map<Integer, Fragment> fragmentMap = new HashMap<>();

    public CreateTripPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    public void addPage(String title) {
        if (!pageTitles.contains(title)) {
            pageTitles.add(title);
        }
    }

    public void addPlusPage() {
        if (!pageTitles.contains("➕")) {
            pageTitles.add("➕");
        }
    }

    public void removeLastPage() {
        if (!pageTitles.isEmpty()) {
            pageTitles.remove(pageTitles.size() - 1);
        }
    }

    public void removePage(String title) {
        pageTitles.remove(title);
    }

    public int getDayTabCount() {
        int count = 0;
        for (String title : pageTitles) {
            if (title.startsWith("วัน")) count++;
        }
        return count;
    }

    public String getPageTitle(int position) {
        return pageTitles.get(position);
    }

    public List<String> getPageTitles() {
        return pageTitles;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        String title = pageTitles.get(position);
        Log.d("Adapter", "createFragment for: " + title);

        Fragment fragment;
        if ("ภาพรวม".equals(title)) {
            fragment = new Trip_Overview_Fragment();
        } else if ("➕".equals(title)) {
            fragment = new Fragment(); // dummy
        } else if (title.startsWith("วัน")) {
            int dayNumber = extractDayNumber(title);
            fragment = Trip_Day_Fragment.newInstance(dayNumber);
        } else {
            fragment = new Fragment();
        }

        fragmentMap.put(position, fragment);
        return fragment;
    }

    public Fragment getFragmentAt(int position) {
        return fragmentMap.get(position);
    }

    private int extractDayNumber(String title) {
        try {
            return Integer.parseInt(title.replace("วัน", "").trim());
        } catch (NumberFormatException e) {
            return 1; // fallback
        }
    }

    @Override
    public int getItemCount() {
        return pageTitles.size();
    }

    @Override
    public long getItemId(int position) {
        return pageTitles.get(position).hashCode();
    }

    @Override
    public boolean containsItem(long itemId) {
        for (String title : pageTitles) {
            if (title.hashCode() == itemId) return true;
        }
        return false;
    }
}
