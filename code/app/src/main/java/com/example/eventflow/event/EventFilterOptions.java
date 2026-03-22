package com.example.eventflow.event;

import java.util.ArrayList;
import java.util.List;

/**
 * Data class to hold filtering criteria for events.
 */
public class EventFilterOptions {
    private List<String> interests = new ArrayList<>();
    private List<String> daysOfWeek = new ArrayList<>();
    private String timeOfDay = ""; // "Morning", "Afternoon", "Evening", or empty for any

    public List<String> getInterests() { return interests; }
    public void setInterests(List<String> interests) { this.interests = interests; }
    public List<String> getDaysOfWeek() { return daysOfWeek; }
    public void setDaysOfWeek(List<String> daysOfWeek) { this.daysOfWeek = daysOfWeek; }
    public String getTimeOfDay() { return timeOfDay; }
    public void setTimeOfDay(String timeOfDay) { this.timeOfDay = timeOfDay; }

    public void clear() {
        interests.clear();
        daysOfWeek.clear();
        timeOfDay = "";
    }

    public boolean isEmpty() {
        return interests.isEmpty() && daysOfWeek.isEmpty() && (timeOfDay == null || timeOfDay.isEmpty());
    }
}
