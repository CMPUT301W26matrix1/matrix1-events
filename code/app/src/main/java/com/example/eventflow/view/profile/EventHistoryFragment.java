/**
 * Fragment that displays the event history for the current device.
 * Retrieves history items based on the Android device ID and displays them in a RecyclerView.
 */
package com.example.eventflow.view.profile;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventflow.R;
import com.example.eventflow.model.entities.EventHistoryItem;
import com.example.eventflow.model.repositories.EventHistoryRepository;

import java.util.List;

public class EventHistoryFragment extends Fragment {

    private RecyclerView rvEventHistory;
    private TextView tvEmptyHistory;

    private EventHistoryRepository eventHistoryRepository;

    public EventHistoryFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvEventHistory = view.findViewById(R.id.rvEventHistory);
        tvEmptyHistory = view.findViewById(R.id.tvEmptyHistory);

        rvEventHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        eventHistoryRepository = new EventHistoryRepository();

        loadEventHistory();
    }

    @SuppressLint("HardwareIds")
    private void loadEventHistory() {
        String deviceId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        eventHistoryRepository.getEventHistoryByDeviceId(deviceId,
                new EventHistoryRepository.LoadEventHistoryCallback() {
                    @Override
                    public void onSuccess(@NonNull List<EventHistoryItem> historyItems) {
                        if (historyItems.isEmpty()) {
                            tvEmptyHistory.setVisibility(View.VISIBLE);
                            rvEventHistory.setVisibility(View.GONE);
                        } else {
                            tvEmptyHistory.setVisibility(View.GONE);
                            rvEventHistory.setVisibility(View.VISIBLE);
                            rvEventHistory.setAdapter(new EventHistoryAdapter(historyItems));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(requireContext(),
                                "Failed to load event history: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
