package com.example.eventflow.org_event;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.eventflow.R;

/**
 * US 02.01.03 — Activity wrapper for InviteEntrantsFragment.
 * Launched from OrgEventActivity when organizer taps "Invite Entrants".
 */
public class InviteEntrantsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_entrants);

        String eventId = getIntent().getStringExtra("EVENT_ID");

        InviteEntrantsFragment fragment = InviteEntrantsFragment.newInstance(eventId);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.invite_fragment_container, fragment);
        transaction.commit();
    }
}