package com.leondeklerk.smartcontroller;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.EditTextPreference;
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
                .replace(R.id.settings, new SettingsFragment(this))
                .commit();

        // setup the toolbar
        binding.toolbar.setTitle(getString(R.string.title_activity_settings));
        binding.toolbar.setNavigationOnClickListener(
                view1 -> onBackPressed());

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

    /**
     * Fragment containing the actual preferences.
     */
    public static class SettingsFragment extends PreferenceFragmentCompat implements
            OnPreferenceClickListener {

        private SharedPreferences preferences;
        private final static int OPEN_FILE_PICKER = 1;
        private Preference filePickerPreference;
        private final Context context;

        /**
         * Default constructor, used to take in a context.
         *
         * @param context the context of the application
         */
        SettingsFragment(Context context) {
            this.context = context;
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            preferences = PreferenceManager.getDefaultSharedPreferences(context);
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            // Find the filepicker preference and set the summary and click listener.
            filePickerPreference = findPreference("filePicker");

            if (filePickerPreference != null) {
                filePickerPreference.setOnPreferenceClickListener(this);
                filePickerPreference.setSummary(preferences.getString("mqtt_file_picker_summary", ""));
            }


            final EditTextPreference preference = findPreference("mqtt_password");

            // Replace the values of a password field with asteriks for security.
            // Based on: https://stackoverflow.com/a/59072162/8298898
            if (preference != null) {
                preference.setSummaryProvider(preference12 -> {

                    // Check if there is a value
                    String getPassword = PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("mqtt_password", "Not set");

                    //return "not set" else return password with asterisks
                    if (getPassword.equals("not set")) {
                        return getPassword;
                    } else {
                        return (setAsterisks(getPassword.length()));
                    }
                });

                //set input type as password and set summary with asterisks the new password
                preference.setOnBindEditTextListener(
                        editText -> {
                            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            Drawable visibilityDrawable = ContextCompat.getDrawable(context, R.drawable.baseline_visibility_24);
                            editText.setCompoundDrawablesWithIntrinsicBounds(null, null, visibilityDrawable, null);

                            editText.setOnTouchListener((view, motionEvent) -> {
                                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                                    if (motionEvent.getRawX() >= (editText.getRight() - editText.getCompoundDrawables()[2].getBounds().width())) {
                                        if (editText.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                                            editText.setInputType(InputType.TYPE_CLASS_TEXT);
                                            Drawable draw = ContextCompat.getDrawable(context, R.drawable.baseline_visibility_off_24);
                                            editText.setCompoundDrawablesWithIntrinsicBounds(null, null, draw, null);
                                        } else {
                                            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                                            Drawable draw = ContextCompat.getDrawable(context, R.drawable.baseline_visibility_24);
                                            editText.setCompoundDrawablesWithIntrinsicBounds(null, null, draw, null);
                                        }
                                        return true;
                                    }
                                }
                                return false;
                            });

                            preference.setSummaryProvider(preference1 -> setAsterisks(editText.getText().toString().length()));
                        });
            }

        }

        /**
         * Create a string of just asterisks based on a length.
         *
         * @param length the length of the input string
         * @return the string of asterisks
         */
        private String setAsterisks(int length) {
            StringBuilder sb = new StringBuilder();
            for (int s = 0; s < length; s++) {
                sb.append("*");
            }
            return sb.toString();
        }

        @Override
        public boolean onPreferenceClick(@NonNull Preference preference) {
            if (!preferences.getString("mqtt_file_picker_summary", "").equals("")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
                builder.setTitle(R.string.mqtt_select_certificate_title).setMessage(R.string.mqtt_select_certificate);

                builder.setPositiveButton(R.string.button_select, (dialog, id) -> openFileChooser());

                builder.setNegativeButton(R.string.button_delete, (dialog, id) -> {
                    preferences.edit().putString("mqtt_file_picker_summary", "").apply();
                    preferences.edit().putString("mqtt_cert", null).apply();
                    filePickerPreference.setSummary("");
                });

                builder.setNeutralButton(R.string.color_cancel, (dialog, id) -> {

                });

                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                openFileChooser();
            }
            return true;
        }

        private void openFileChooser() {
            // Create an intent to open a filepicker
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            String[] mimetypes = {"application/x-pem-file", "application/x-x509-ca-cert", "application/pkix-cert"};
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);

            // Start the file picker
            //noinspection deprecation
            startActivityForResult(intent, OPEN_FILE_PICKER);
        }

        @SuppressWarnings("deprecation")
        @Override
        public void onActivityResult(int requestCode, int resultCode,
                                     Intent resultData) {
            // Upon completion of the filepicker
            if (requestCode == OPEN_FILE_PICKER && resultCode == Activity.RESULT_OK) {
                if (resultData != null) {
                    // The uri of the selected file
                    Uri uri = resultData.getData();

                    // Get the name of the file
                    assert uri != null;
                    Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null);
                    assert cursor != null;
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    cursor.moveToFirst();
                    String fileName = cursor.getString(nameIndex);
                    cursor.close();

                    // Set the name of the file as the summary and save this
                    preferences.edit().putString("mqtt_file_picker_summary", fileName).apply();
                    filePickerPreference.setSummary(fileName);

                    // Read the file based on the URI
                    try {
                        String text = readTextFromUri(uri);

                        // Rough certificate validation
                        if (text.startsWith("-----BEGIN CERTIFICATE-----") && text.endsWith("-----END CERTIFICATE-----\n")) {
                            preferences.edit().putString("mqtt_cert", text).apply();
                        } else {
                            Toast.makeText(getContext(), "Invalid file", Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        Log.d("SettingsActiviy@onActivityResult#catch", "Reading failed", e);
                    }
                }
            } else {
                Toast.makeText(getContext(), "No file selected", Toast.LENGTH_SHORT).show();
            }
        }

        /**
         * Read a file based on a URI
         *
         * @param uri the content uri to find the file
         * @return the text inside the file
         * @throws IOException error thrown upon file reading error.
         */
        private String readTextFromUri(Uri uri) throws IOException {
            StringBuilder stringBuilder = new StringBuilder();
            try (
                    // Open the URI
                    InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
                    // Create a reader from the input stream
                    BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(inputStream)))
            ) {
                int charInt;
                // While there are characters to read
                while ((charInt = reader.read()) != -1) {
                    char character = (char) charInt;
                    // Filter out potential harmful character (which should not occur in a certificate)
                    if (character == '(' || character == '{') {
                        continue;
                    }
                    stringBuilder.append((char) charInt);
                }
            }
            return stringBuilder.toString();
        }
    }
}
