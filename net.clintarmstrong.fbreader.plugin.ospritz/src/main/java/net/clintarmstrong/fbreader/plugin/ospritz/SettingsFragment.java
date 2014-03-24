package net.clintarmstrong.fbreader.plugin.ospritz;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by carmstrong on 3/19/14.
 */
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }
}