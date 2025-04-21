package com.example.project_cbnew;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class Setting_Fragment extends Fragment {

    public Setting_Fragment() {
        // Required empty public constructor
    }

    public static Setting_Fragment newInstance(String param1, String param2) {
        Setting_Fragment fragment = new Setting_Fragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        Spinner spinnerLanguage = view.findViewById(R.id.spinnerLanguage);

        String[] languages = {"ไทย", "English"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, languages);
        spinnerLanguage.setAdapter(adapter);

        // ในอนาคตสามารถเซฟค่าที่เลือกลง SharedPreferences แล้วเปลี่ยนภาษาทันทีได้
        return view;
    }
}
