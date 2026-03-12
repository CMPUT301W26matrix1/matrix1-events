package com.example.eventflow.view.profile;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventflow.R;

import java.util.ArrayList;

/**
 * Displays the list of entrants invited to the event.
 */
public class SelectedEntrantsActivity extends AppCompatActivity {

    private ArrayList<String> selectedEntrants;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_entrants);

        ListView listView = findViewById(R.id.selectedEntrantsList);

        selectedEntrants = new ArrayList<>();
        selectedEntrants.add("Alice - INVITED");
        selectedEntrants.add("Bob - INVITED");

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this,
                        android.R.layout.simple_list_item_1,
                        selectedEntrants);

        listView.setAdapter(adapter);
    }
}