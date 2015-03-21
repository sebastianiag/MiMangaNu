package ar.rulosoft.mimanganu;

import java.io.File;
import java.io.IOException;

import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;

public class OpcionesActivity extends PreferenceActivity {
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.fragment_preferences);
		CheckBoxPreference cBoxPreference = (CheckBoxPreference) getPreferenceManager().findPreference("mostrar_en_galeria");

		/*
		 * Esconder de galeria
		 */

		cBoxPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				boolean valor = (Boolean) newValue;
				File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MiMangaNu/", ".nomedia");
				if (!valor) {
					if (f.exists())
						f.delete();
				} else {
					if (!f.exists())
						try {
							f.createNewFile();
						} catch (IOException e) {
							e.printStackTrace();
						}
				}
				return true;
			}
		});
		
		
	}
}