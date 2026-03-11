package com.example.eventflow.view.profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.eventflow.R;
import com.example.eventflow.model.entities.Profile;

public class ProfileViewFragment extends Fragment {

    private static final String ARG_FIRST = "first";
    private static final String ARG_LAST = "last";
    private static final String ARG_EMAIL = "email";
    private static final String ARG_PHONE = "phone";

    public ProfileViewFragment() {}

    public static ProfileViewFragment newInstance(Profile profile) {

        ProfileViewFragment fragment = new ProfileViewFragment();

        Bundle args = new Bundle();
        args.putString(ARG_FIRST, profile.getFirstName());
        args.putString(ARG_LAST, profile.getLastName());
        args.putString(ARG_EMAIL, profile.getEmail());
        args.putString(ARG_PHONE, profile.getPhoneNumber());

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_profile_view, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        TextView name = view.findViewById(R.id.profile_name);
        TextView email = view.findViewById(R.id.profile_email);
        TextView phone = view.findViewById(R.id.profile_phone);

        Bundle args = getArguments();

        if (args != null) {

            String first = args.getString(ARG_FIRST);
            String last = args.getString(ARG_LAST);
            String em = args.getString(ARG_EMAIL);
            String ph = args.getString(ARG_PHONE);

            name.setText(first + " " + last);
            email.setText(em);

            if (TextUtils.isEmpty(ph)) {
                phone.setText("No phone number");
            } else {
                phone.setText(ph);
            }
        }
    }
}