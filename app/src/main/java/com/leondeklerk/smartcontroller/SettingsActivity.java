package com.leondeklerk.smartcontroller;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceClickListener;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import com.leondeklerk.smartcontroller.databinding.SettingsActivityBinding;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

/**
 * The activity that contains all the applications settings. Mainly uses for the MQTT server
 * settings.
 */
public class SettingsActivity extends AppCompatActivity implements
    OnSharedPreferenceChangeListener {

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

  public static class SettingsFragment extends PreferenceFragmentCompat implements
      OnPreferenceClickListener {

    private final static int OPEN_FILE_PICKER = 1;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
      setPreferencesFromResource(R.xml.root_preferences, rootKey);
      Preference preference = findPreference("filePicker");
      preference.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
      Log.d("SettingsActivity@onPreferenceClick", preference.getKey());
      Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
      intent.addCategory(Intent.CATEGORY_OPENABLE);
      intent.setType("*/*");
      String[] mimetypes = {"application/x-pem-file","application/x-x509-ca-cert","application/pkix-cert"};
      intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);

      startActivityForResult(intent, OPEN_FILE_PICKER);
      return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
        Intent resultData) {
      if (requestCode == OPEN_FILE_PICKER && resultCode == Activity.RESULT_OK) {
        // The result data contains a URI for the document or directory that
        // the user selected.
        Uri uri = null;
        if (resultData != null) {
          uri = resultData.getData();
          Log.d("SettingsActivity@onActivityResult#uri", uri.toString());
          // Perform operations on the document using its URI.
          try {
            Log.d("SettingsActivity@onActivityResult#try", readTextFromUri(uri));
          } catch (IOException e) {
            Log.d("SettingsActiviy@onActivityResult#catch", e.getMessage());
          }
        }
      } else {
        Toast.makeText(getContext(), "No file selected", Toast.LENGTH_SHORT).show();
      }
    }

    private String readTextFromUri(Uri uri) throws IOException {
      StringBuilder stringBuilder = new StringBuilder();
      try (InputStream inputStream =
          getContext().getContentResolver().openInputStream(uri);
          BufferedReader reader = new BufferedReader(
              new InputStreamReader(Objects.requireNonNull(inputStream)))) {
        String line;
        while ((line = reader.readLine()) != null) {
          stringBuilder.append(line);
        }
      }
      return stringBuilder.toString();
    }
  }
}
