package com.n1kdo.lotwlook;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

public class PreferencesSettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }
}
