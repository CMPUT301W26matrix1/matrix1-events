package com.example.eventflow;

import static org.junit.Assert.*;

import com.example.eventflow.org_event.AttendanceLimit;

import org.junit.Test;

/**
 * Tests the logic for attendee limits without requiring an Android device.
 */
public class AttendanceLimitTest {

    @Test
    public void testLimitWhenDisabled() {
        // When checkbox is OFF, it should always be valid regardless of input
        assertTrue("Disabled limit should be valid",
                AttendanceLimit.isValidLimit(false, ""));
    }

    @Test
    public void testValidLimitEnabled() {
        // When checkbox is ON and input is a positive number
        assertTrue("Positive number should be valid",
                AttendanceLimit.isValidLimit(true, "50"));
    }

    @Test
    public void testInvalidLimitEnabled() {
        // When checkbox is ON but input is empty or invalid
        assertFalse("Empty input should be invalid",
                AttendanceLimit.isValidLimit(true, ""));
        assertFalse("Negative number should be invalid",
                AttendanceLimit.isValidLimit(true, "-10"));
        assertFalse("Non-numeric input should be invalid",
                AttendanceLimit.isValidLimit(true, "abc"));
    }
}
