package com.example.eventflow;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputLayout;

public class AttendanceLimit extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_attendance_limit, container, false);
        CheckBox limitCheckBox = view.findViewById(R.id.checkbox_limit_entrants);
        TextInputLayout limitInputLayout = view.findViewById(R.id.layout_entrant_limit);
        EditText limitEditText = view.findViewById(R.id.edit_entrant_limit);

        limitCheckBox.setOnCheckedChangeListener((buttonView, isChecked)->{
            if (isChecked){
                limitInputLayout.setVisibility(View.VISIBLE);
                limitEditText.requestFocus();
            }else{
                limitInputLayout.setVisibility(View.GONE);
                limitEditText.setText("");
            }
        });
        return view;
    }
}
