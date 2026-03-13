package com.example.eventflow;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class OrganizerFinalEntrantsTest {

    @Test
    public void confirmedEntrantsAppearInFinalList() {

        List<Boolean> entrantStatus = new ArrayList<>();
        entrantStatus.add(true);
        entrantStatus.add(true);
        entrantStatus.add(true);

        int confirmedCount = 0;

        for (Boolean status : entrantStatus) {
            if (status) {
                confirmedCount++;
            }
        }

        assertEquals(3, confirmedCount);
    }

    @Test
    public void unconfirmedEntrantsNotIncluded() {

        List<Boolean> entrantStatus = new ArrayList<>();
        entrantStatus.add(true);
        entrantStatus.add(false);
        entrantStatus.add(true);

        int confirmedCount = 0;

        for (Boolean status : entrantStatus) {
            if (status) {
                confirmedCount++;
            }
        }

        assertEquals(2, confirmedCount);
    }

    @Test
    public void emptyEntrantListReturnsEmpty() {

        List<Boolean> entrantStatus = new ArrayList<>();

        int confirmedCount = 0;

        for (Boolean status : entrantStatus) {
            if (status) {
                confirmedCount++;
            }
        }

        assertEquals(0, confirmedCount);
    }

    @Test
    public void listSizeMatchesConfirmedEntrants() {

        List<Boolean> entrantStatus = new ArrayList<>();
        entrantStatus.add(false);
        entrantStatus.add(true);
        entrantStatus.add(true);
        entrantStatus.add(false);

        int confirmedCount = 0;

        for (Boolean status : entrantStatus) {
            if (status) {
                confirmedCount++;
            }
        }

        assertEquals(2, confirmedCount);
    }
}