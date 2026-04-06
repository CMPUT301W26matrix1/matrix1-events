package com.example.eventflow;

import static org.junit.Assert.*;

import com.example.eventflow.model.entities.Entrant;

import org.junit.Test;

public class EntrantTest {

    @Test
    public void entrantId_setterAndGetter_workCorrectly() {
        Entrant entrant = new Entrant();
        entrant.setEntrantid("1234");

        assertEquals("1234", entrant.getEntrantid());
    }

    @Test
    public void userId_setterAndGetter_workCorrectly() {
        Entrant entrant = new Entrant();
        entrant.setUserId("user_1");

        assertEquals("user_1", entrant.getUserId());
    }

    @Test
    public void defaultConstructor_createsObject() {
        Entrant entrant = new Entrant();

        assertNotNull(entrant);
    }

    @Test
    public void fullConstructor_setsFieldsCorrectly() {
        Entrant entrant = new Entrant("John Doe", "john@example.com", "1234567890", "2023-11-01", "Waiting");
        
        assertEquals("John Doe", entrant.getName());
        assertEquals("john@example.com", entrant.getEmail());
        assertEquals("1234567890", entrant.getPhoneNumber());
        assertEquals("2023-11-01", entrant.getInviteDate());
        assertEquals("Waiting", entrant.getStatus());
    }

    @Test
    public void settersAndGetters_workCorrectly() {
        Entrant entrant = new Entrant();
        
        entrant.setName("Jane Doe");
        entrant.setEmail("jane@example.com");
        entrant.setPhoneNumber("0987654321");
        entrant.setStatus("Invited");
        entrant.setInviteDate("2023-11-02");

        assertEquals("Jane Doe", entrant.getName());
        assertEquals("jane@example.com", entrant.getEmail());
        assertEquals("0987654321", entrant.getPhoneNumber());
        assertEquals("Invited", entrant.getStatus());
        assertEquals("2023-11-02", entrant.getInviteDate());
    }
}
