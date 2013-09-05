package com.bnsoft.cafe;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	@SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        getPreferenceScreen().getSharedPreferences()
        .registerOnSharedPreferenceChangeListener(this);
        bindPreferenceSummaryToValue(findPreference(getString(R.string.edit_ip_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.edit_port_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.edit_currency_key)));
    }
	
	private static void bindPreferenceSummaryToValue(Preference preference) {
		// Set the listener to watch for value changes.
		preference
				.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

		// Trigger the listener immediately with the preference's
		// current value.
		sBindPreferenceSummaryToValueListener.onPreferenceChange(
				preference,
				PreferenceManager.getDefaultSharedPreferences(
						preference.getContext()).getString(preference.getKey(),
						""));
	}
	
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
	        String key) {
		
		int MAX_VALUE = 65535;
		int MIN_VALUE = 1;

	  if (getString(R.string.edit_port_key).equals(key))	{
		  
	    String valueString = sharedPreferences.getString(key, getString(R.string.edit_port_defaultValue));
	    int value = Integer.parseInt(valueString);
	    
	    if (value > MAX_VALUE)	{
	    	@SuppressWarnings("deprecation")
			EditTextPreference edit_port = (EditTextPreference) findPreference(key);
	    	edit_port.setText(""+MAX_VALUE);
	    	Toast t = Toast.makeText(this,"Maximum port number is "+MAX_VALUE, Toast.LENGTH_SHORT);
	    	t.show();
	    } else if (value < MIN_VALUE)	{
	    	@SuppressWarnings("deprecation")
			EditTextPreference p = (EditTextPreference) findPreference(key);
	    	p.setText(""+MIN_VALUE);
	    	Toast t = Toast.makeText(this,"Minimum port number is "+MIN_VALUE, Toast.LENGTH_SHORT);
	    	t.show();
	    } else {
	    	Global.PORT = Integer.parseInt(valueString);
	    }
	  }
	  if (getString(R.string.edit_currency_key).equals(key)) {
	    	Log.d("BNSOFT_DBG", "Changing currency identifier...");
	    	Global.currencyIdent = sharedPreferences.getString(key, getString(R.string.edit_currency_defaultValue));
	  }
	  if (getString(R.string.edit_ip_key).equals(key)) {
		  Global.HOST = sharedPreferences.getString(key, getString(R.string.edit_ip_defaultValue));
	  }
//	  if (getString(R.string.edit_fontSize_key).equals(key)) {
//		  Global.FONT_SIZE = Integer.parseInt(sharedPreferences.getString(key, getString(R.string.edit_fontSize_defaultValue)));
//	  }
	}
	
	private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object value) {
			String stringValue = value.toString();

			if (preference instanceof ListPreference) {
				// For list preferences, look up the correct display value in
				// the preference's 'entries' list.
				ListPreference listPreference = (ListPreference) preference;
				int index = listPreference.findIndexOfValue(stringValue);

				// Set the summary to reflect the new value.
				preference
						.setSummary(index >= 0 ? listPreference.getEntries()[index]
								: null);

			} else
				// For all other preferences, set the summary to the value's
				// simple string representation.
				preference.setSummary(stringValue);
			return true;
		}
	};
}
