package com.example.eventflow.org_event;

import android.widget.CheckBox;
import android.widget.EditText;

/**
 * Helper class to manage the attendance limit feature.
 * Handles both UI interaction and validation logic.
 */
public class AttendanceLimit {

    /**
     * Links the checkbox to the EditText so the organizer can optionally limit attendees.
     * This method handles the UI behavior (enabling/disabling views).
     *
     * @param cbLimit The CheckBox that toggles the limit.
     * @param etLimit The EditText where the number is entered.
     */
    public static void setupLimitToggle(CheckBox cbLimit, EditText etLimit) {
        cbLimit.setOnCheckedChangeListener((buttonView, isChecked) -> {
            etLimit.setEnabled(isChecked);

            if (isChecked) {
                etLimit.setAlpha(1.0f);
            } else {
                etLimit.setAlpha(0.5f);
                etLimit.setText("");
                etLimit.setError(null); // Clear error messages when disabled
            }
        });
    }

    /**
     * Logic for validating the attendee limit input.
     * This method is "Pure Logic," meaning it doesn't require Android UI components,
     * making it perfect for JUnit Unit Testing.
     *
     * @param isChecked Whether the limit toggle is currently active.
     * @param input The text string retrieved from the EditText.
     * @return True if the configuration is valid; False otherwise.
     */
    public static boolean isValidLimit(boolean isChecked, String input) {
        if (!isChecked) {
            return true;
        }

        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        try {
            int limit = Integer.parseInt(input.trim());
            return limit > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}