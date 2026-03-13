package com.example.eventflow;

import com.example.eventflow.model.entities.Profile;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ProfileTest {

    @Test
    public void constructorAndGetters_workCorrectly() {
        Profile profile = new Profile(
                "device123",
                "Taufeeq",
                "Raji",
                "taufeeq@example.com",
                "7801234567"
        );

        assertEquals("device123", profile.getDeviceId());
        assertEquals("Taufeeq", profile.getFirstName());
        assertEquals("Raji", profile.getLastName());
        assertEquals("taufeeq@example.com", profile.getEmail());
        assertEquals("7801234567", profile.getPhoneNumber());
    }

    @Test
    public void setters_updateFieldsCorrectly() {
        Profile profile = new Profile();

        profile.setDeviceId("device999");
        profile.setFirstName("John");
        profile.setLastName("Doe");
        profile.setEmail("john@example.com");
        profile.setPhoneNumber("1234567890");

        assertEquals("device999", profile.getDeviceId());
        assertEquals("John", profile.getFirstName());
        assertEquals("Doe", profile.getLastName());
        assertEquals("john@example.com", profile.getEmail());
        assertEquals("1234567890", profile.getPhoneNumber());
    }

    @Test
    public void getFullName_returnsConcatenatedName() {
        Profile profile = new Profile(
                "device123",
                "Taufeeq",
                "Raji",
                "taufeeq@example.com",
                "7801234567"
        );

        assertEquals("Taufeeq Raji", profile.getFullName());
    }
}