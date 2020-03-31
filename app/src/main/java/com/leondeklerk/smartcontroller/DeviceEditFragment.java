package com.leondeklerk.smartcontroller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.leondeklerk.smartcontroller.devices.SmartDevice;
import java.lang.reflect.Type;
import java.util.ArrayList;
import org.jetbrains.annotations.NotNull;

public class DeviceEditFragment extends Fragment implements View.OnClickListener {

  static final String ARG_FRAG_NUM =
      "com.leondeklerk.smartcontroller.FRAG_NUM"; // TODO Change to device name
  private Activity context;
  private int devNum;
  private ArrayList<SmartDevice> devices;
  private SharedPreferences preferences;

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    context = getActivity();
    return inflater.inflate(R.layout.fragment_device_edit, container, false);
  }

  @Override
  public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
    Bundle args = getArguments();
    if (args != null) {
      devNum = args.getInt(ARG_FRAG_NUM);
    } else {
      devNum = 0;
    }
    preferences = context.getSharedPreferences(getString(R.string.dev_prefs), Context.MODE_PRIVATE);
    getDevices();

    MaterialButton cancelButton = view.findViewById(R.id.edit_cancel);
    cancelButton.setOnClickListener(this);

    MaterialButton deleteButton = view.findViewById(R.id.edit_delete);
    deleteButton.setOnClickListener(this);
  }

  @Override
  public void onResume() {
    super.onResume();
    MaterialToolbar toolbar = context.findViewById(R.id.toolbar);
    toolbar.setTitle("Dev num: " + devNum);
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.edit_cancel:
        context.onBackPressed();
        return;
      case R.id.edit_delete:
        devices.remove(devNum);
        storeDevices();
        Intent resultIntent = new Intent();
        resultIntent.putExtra(MainActivity.EXTRA_DEV_CHANGED, true);
        context.setResult(Activity.RESULT_OK, resultIntent);
        context.onBackPressed();
        return;
      default:
        Toast.makeText(context, "Unknown device clicked", Toast.LENGTH_SHORT).show();
    }
  }

  public void getDevices() {
    String json = preferences.getString("deviceList", null);
    if (json != null) {
      Gson gson = new Gson();
      Type type = new TypeToken<ArrayList<SmartDevice>>() {
      }.getType();
      devices = gson.fromJson(json, type);
    } else {
      devices = new ArrayList<>();
    }
  }

  public void storeDevices() {
    Editor prefsEditor = preferences.edit();
    Gson gson = new Gson();
    String json = gson.toJson(devices);
    prefsEditor.putString("deviceList", json);
    prefsEditor.apply();
  }
}
