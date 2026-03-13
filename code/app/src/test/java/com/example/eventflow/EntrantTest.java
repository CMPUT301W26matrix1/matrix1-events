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
}

