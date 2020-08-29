package com.leondeklerk.smartcontroller;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import com.leondeklerk.smartcontroller.databinding.ActivityDeviceColorBinding;
import com.leondeklerk.smartcontroller.databinding.SettingsActivityBinding;

/**
 * The activity that contains all the applications settings. Mainly uses for the MQTT server
 * settings.
 */
public class SettingsActivity extends AppCompatActivity
    implements OnSharedPreferenceChangeListener {

  private Intent result;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Inflate the view binding.
    SettingsActivityBinding binding = SettingsActivityBinding.inflate(getLayoutInflater());
    View view = binding.getRoot();
    setContentView(view);

    getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.settings, new SettingsFragment())
        .commit();

    // setup the toolbar
    binding.toolbar.setTitle(getString(R.string.title_activity_settings));
    binding.toolbar.setNavigationOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            onBackPressed();
          }
        });

    PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
        .registerOnSharedPreferenceChangeListener(this);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (result == null) {
      result = new Intent();
      result.putExtra(MainActivity.EXTRA_PREFS_CHANGED, false);
      setResult(Activity.RESULT_OK, result);
    }
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    // If a preference changed, notify the calling Activity.
    result = new Intent();
    result.putExtra(MainActivity.EXTRA_PREFS_CHANGED, true);
    setResult(Activity.RESULT_OK, result);
  }

  public static class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
      setPreferencesFromResource(R.xml.root_preferences, rootKey);
    }
  }
}
