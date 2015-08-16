package com.aigbe.adclient;

/**
 * Created by Austin Aigbe on 8/9/2015.
 */
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class Settings extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private SharedPreferences.OnSharedPreferenceChangeListener listener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);

        // Register the EditTextPreference
        final EditTextPreference ipAddress = (EditTextPreference) findPreference("adServerIp");
        ipAddress.setSummary(ipAddress.getText());

        final EditTextPreference port = (EditTextPreference) findPreference("adServerPort");
        port.setSummary(port.getText());

        final ListPreference loc = (ListPreference) findPreference("location");
        loc.setSummary(loc.getEntry());

        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                String value = sharedPreferences.getString(key, "NULL");
                // IP Address
                if (key.equals("adServerIp")){
                    if (value.matches(MainActivity.REGEX_IP_ADDRESS)) {
                        ipAddress.setSummary(value);
                        ipAddress.setText(value);
                    } else {
                        Toast.makeText(getApplicationContext(), "Invalid IP address, default IP (127.0.0.1) will be used", Toast.LENGTH_SHORT).show();
                        ipAddress.setSummary(MainActivity.DEFAULT_IP);
                        ipAddress.setText(MainActivity.DEFAULT_IP);
                    }
                //Port
                }else if (key.equals("adServerPort")) {
                    if (value == "NULL") {
                        Toast.makeText(getApplicationContext(), "Invalid port number, default value (8080) will be used", Toast.LENGTH_SHORT).show();
                        port.setText(MainActivity.DEFAULT_PORT);
                        port.setSummary(MainActivity.DEFAULT_PORT);
                    }
                // location
                }else if (key.equals("location")) {
                    loc.setSummary(value);
                }


            }
        };
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {

            Preference k = findPreference(key);
            k.setSummary(sharedPreferences.getString(key, "NULL"));
    }
}
