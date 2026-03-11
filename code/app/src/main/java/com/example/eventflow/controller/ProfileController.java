package com.example.eventflow.controller;

import android.text.TextUtils;
import android.util.Patterns;

import com.example.eventflow.model.entities.Profile;

public class ProfileController {

    public String validateProfileInput(String firstName, String lastName, String email) {
        if (TextUtils.isEmpty(firstName)) {
            return "First name is required";
        }

        if (TextUtils.isEmpty(lastName)) {
            return "Last name is required";
        }

        if (TextUtils.isEmpty(email)) {
            return "Email is required";
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return "Enter a valid email address";
        }

        return null;
    }

    public Profile createProfile(String deviceId, String firstName, String lastName, String email, String phoneNumber) {
        return new Profile(deviceId, firstName, lastName, email, phoneNumber);
    }
}