package net.clintarmstrong.fbreader.plugin.ospritz;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by carmstrong on 3/19/14.
 */
public class SettingsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
