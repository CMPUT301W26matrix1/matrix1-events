package com.example.eventflow.org_event;

import android.content.Context;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import java.util.UUID;

public class EventFormManager {

    /**
     * Reads all inputs, checks for mistakes, and returns a clean Event object.
     */
    public static Event validateAndCreateEvent(
            Context context,
            EditText etName,
            EditText etLocation,
            EditText etDate,
            EditText etDescription,
            CheckBox cbLimit,
            EditText etLimit,
            CheckBox cbPrivate,
            EditText etRegStart,
            EditText etRegEnd,
            String posterUrl) {

        String name = etName.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String regStart = etRegStart != null ? etRegStart.getText().toString().trim() : "";
        String regEnd = etRegEnd != null ? etRegEnd.getText().toString().trim() : "";

        if (!isDataValid(name, location, date)) {
            Toast.makeText(context, "Please fill in the Name, Location, and Date.", Toast.LENGTH_SHORT).show();
            return null;
        }

        Integer limitValue = null;
        if (cbLimit.isChecked()) {
            String limitStr = etLimit.getText().toString().trim();
            if (limitStr.isEmpty()) {
                etLimit.setError("Please type a number");
                etLimit.requestFocus();
                return null;
            }
            try {
                limitValue = Integer.parseInt(limitStr);
            } catch (NumberFormatException e) {
                etLimit.setError("Invalid number");
                return null;
            }
            if (limitValue <= 0) {
                etLimit.setError("Limit must be at least 1");
                return null;
            }
        }

        boolean isPrivate = cbPrivate != null && cbPrivate.isChecked();
        String newEventId = UUID.randomUUID().toString();

        return new Event(newEventId, name, location, date, description, limitValue, isPrivate, regStart, regEnd, posterUrl);
    }

    /**
     * Helper to check if basic event data is valid.
     */
    public static boolean isDataValid(String name, String location, String date) {
        return name != null && !name.trim().isEmpty() &&
                location != null && !location.trim().isEmpty() &&
                date != null && !date.trim().isEmpty();
    }

    /**
     * Overload for 8 arguments (with private checkbox)
     */
    public static Event validateAndCreateEvent(
            Context context,
            EditText etName,
            EditText etLocation,
            EditText etDate,
            EditText etDescription,
            CheckBox cbLimit,
            EditText etLimit,
            CheckBox cbPrivate) {
        return validateAndCreateEvent(context, etName, etLocation, etDate, etDescription, cbLimit, etLimit, cbPrivate, null, null, null);
    }

    // Overload for backward compatibility (7 arguments)
    public static Event validateAndCreateEvent(
            Context context,
            EditText etName,
            EditText etLocation,
            EditText etDate,
            EditText etDescription,
            CheckBox cbLimit,
            EditText etLimit) {
        return validateAndCreateEvent(context, etName, etLocation, etDate, etDescription, cbLimit, etLimit, null, null, null, null);
    }
}