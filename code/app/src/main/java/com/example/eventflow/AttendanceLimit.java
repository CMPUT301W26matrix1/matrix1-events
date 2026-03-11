package com.example.eventflow;

import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

public class AttendanceLimit {

    /**
     * Sets up the relationship between the checkbox and the input field.
     */
    public static void setupLimitToggle(CheckBox limitCheckBox, EditText limitEditText) {
        limitCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Enable the input and make it look active
                limitEditText.setEnabled(true);
                limitEditText.setAlpha(1.0f);
            } else {
                // Disable and fade it out
                limitEditText.setEnabled(false);
                limitEditText.setAlpha(0.5f);
                limitEditText.setText("");
            }
        });
    }
}