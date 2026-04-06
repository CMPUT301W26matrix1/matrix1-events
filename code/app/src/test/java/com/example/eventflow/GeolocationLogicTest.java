package com.example.eventflow;

import static org.junit.Assert.*;

import com.example.eventflow.model.entities.Event;

import org.junit.Before;
import org.junit.Test;

public class GeolocationLogicTest {

    private Event eventWithGeo;
    private Event eventWithoutGeo;

    @Before
    public void setUp() {
        eventWithGeo = new Event();
        eventWithGeo.setGeolocationRequired(true);
        eventWithGeo.setLocationLatitude(53.5461); // Edmonton latitude
        eventWithGeo.setLocationLongitude(-113.4938); // Edmonton longitude
        eventWithGeo.setLocationRadius(1000); // 1km radius

        eventWithoutGeo = new Event();
        eventWithoutGeo.setGeolocationRequired(false);
    }

    @Test
    public void testIsGeolocationRequired() {
        assertTrue(eventWithGeo.isGeolocationRequired());
        assertFalse(eventWithoutGeo.isGeolocationRequired());
    }

    @Test
    public void testDistanceCalculation_InsideRadius() {
        // A point roughly 500m away from the center
        double userLat = 53.5461;
        double userLon = -113.5010; 
        
        double distance = calculateDistance(
            eventWithGeo.getLocationLatitude(), eventWithGeo.getLocationLongitude(),
            userLat, userLon
        );
        
        assertTrue("User should be within radius", distance <= eventWithGeo.getLocationRadius());
    }

    @Test
    public void testDistanceCalculation_OutsideRadius() {
        // A point roughly 2km away from the center
        double userLat = 53.5600;
        double userLon = -113.4938;
        
        double distance = calculateDistance(
            eventWithGeo.getLocationLatitude(), eventWithGeo.getLocationLongitude(),
            userLat, userLon
        );
        
        assertFalse("User should be outside radius", distance <= eventWithGeo.getLocationRadius());
    }

    /**
     * Simple Haversine formula for distance in meters.
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // Radius of the earth in meters
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
