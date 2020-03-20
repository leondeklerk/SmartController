package com.leondeklerk.smartcontroller;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.DiffUtil.DiffResult;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.leondeklerk.smartcontroller.DeviceAdapter.CardViewHolder;
import com.leondeklerk.smartcontroller.data.Response;
import com.leondeklerk.smartcontroller.devices.SmartDevice;
import com.leondeklerk.smartcontroller.utils.DiffUtilCallback;
import com.leondeklerk.smartcontroller.utils.IpInputFilter;
import com.leondeklerk.smartcontroller.utils.TextInputLayoutUtils;
import com.leondeklerk.smartcontroller.widget.ColorDotView;
import java.lang.reflect.Type;
import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity
    implements NetworkCallback, View.OnClickListener {

  private RecyclerView recyclerView;
  DeviceAdapter deviceAdapter;
  Context context;
  ArrayList<SmartDevice> devices;
  TextInputLayoutUtils layoutUtils;
  AlertDialog addDeviceDialog;
  SharedPreferences preferences;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    context = this;
    preferences = this.getPreferences(Context.MODE_PRIVATE);
    getDevices();

    recyclerView = findViewById(R.id.deviceList);
    recyclerView.setHasFixedSize(true);

    // use a linear layout manager
    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
    recyclerView.setLayoutManager(layoutManager);

    // set the adapter
    deviceAdapter = new DeviceAdapter(devices, context);
    recyclerView.setAdapter(deviceAdapter);

    // The Floating action button to launch a dialog where new device can be created
    FloatingActionButton fab = findViewById(R.id.fab);
    fab.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            addDeviceDialog = createDeviceDialog(v);
            addDeviceDialog.show();
            Button button = addDeviceDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            button.setOnClickListener((View.OnClickListener) context);
          }
        });
  }

  @Override
  public void onFinish(Response response, int deviceNum) {
    DeviceAdapter.CardViewHolder holder =
        (CardViewHolder) recyclerView.getChildViewHolder(recyclerView.getChildAt(deviceNum));
    MaterialCardView card = holder.cardView;

    ColorDotView deviceLed = card.findViewById(R.id.deviceLed);
    MaterialTextView deviceStatus = card.findViewById(R.id.deviceStatus);
    SwitchMaterial devicePower = holder.cardView.findViewById(R.id.devicePower);

    devicePower.setEnabled(false);

    if (response.getException() != null) {
      Log.d("Network error", response.getException().toString());
      deviceStatus.setText(getString(R.string.device_status, getString(R.string.status_unknown)));
      devicePower.setChecked(false);
      deviceLed.setVisibility(View.INVISIBLE);
    } else {
      String statusString;
      try {
        JSONObject obj = new JSONObject(response.getResponse());
        statusString = obj.getString("POWER");
      } catch (JSONException e) {
        e.printStackTrace();
        Log.d("JSON Response", response.getResponse());
        return;
      }
      if (statusString.equals("ON")) {
        if (!devicePower.isChecked()) {
          devicePower.setChecked(true);
        }
        deviceLed.setFillColor(getColor(R.color.status_on));
      } else {
        if (devicePower.isChecked()) {
          devicePower.setChecked(false);
        }
        deviceLed.setFillColor(getColor(R.color.status_off));
      }
      devicePower.setEnabled(true);
      deviceStatus.setText(getString(R.string.device_status, statusString));
      deviceLed.setVisibility(View.VISIBLE);
      Log.d("Response", response.getResponse());
    }
  }

  /**
   * @param v
   * @return
   */
  public AlertDialog createDeviceDialog(View v) {
    FrameLayout layout = new FrameLayout(v.getContext());

    AlertDialog dialog =
        new MaterialAlertDialogBuilder(
                v.getContext(), R.style.MaterialAlertDialog_FilledButtonDialog)
            .setTitle(getString(R.string.add_device_title))
            .setView(layout)
            .setPositiveButton(getString(R.string.add_button_confirm), null)
            .setNegativeButton(getString(android.R.string.cancel), null)
            .create();

    final View dialogView = dialog.getLayoutInflater().inflate(R.layout.device_dialog, layout);

    TextInputLayout nameLayout = dialogView.findViewById(R.id.newName);
    final TextInputLayout ipLayout = dialogView.findViewById(R.id.newIp);
    ArrayList<TextInputLayout> layouts = new ArrayList<>();
    layouts.add(nameLayout);
    layouts.add(ipLayout);
    layoutUtils = new TextInputLayoutUtils(layouts, context);

    //noinspection ConstantConditions
    ipLayout.getEditText().setFilters(new InputFilter[] {new IpInputFilter()});

    // Add a listener to the switch to enable / disable
    SwitchMaterial credentials = dialogView.findViewById(R.id.switchCredentials);
    credentials.setOnCheckedChangeListener(
        new CompoundButton.OnCheckedChangeListener() {
          @Override
          public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            setCredentialsAvailability(dialogView, isChecked);
          }
        });
    layoutUtils.setErrorListeners();
    return dialog;
  }

  /**
   * @param dialogView
   * @param isChecked
   */
  public void setCredentialsAvailability(View dialogView, boolean isChecked) {
    TextInputLayout usernameLayout = dialogView.findViewById(R.id.newUsername);
    usernameLayout.setEnabled(isChecked);
    TextInputLayout passwordLayout = dialogView.findViewById(R.id.newPassword);
    passwordLayout.setEnabled(isChecked);

    if (isChecked) {
      layoutUtils.addLayout(usernameLayout);
      layoutUtils.addLayout(passwordLayout);
    } else {
      layoutUtils.removeLayout(usernameLayout);
      layoutUtils.removeLayout(passwordLayout);
    }
    layoutUtils.setErrorListeners();
  }

  @Override
  public void onClick(View v) {
    if (!layoutUtils.hasErrors()) {
      addDeviceDialog.dismiss();
      SwitchMaterial switchMaterial = addDeviceDialog.findViewById(R.id.switchCredentials);
      boolean isProtected = switchMaterial.isChecked();
      SmartDevice device = layoutUtils.readDevice(isProtected, devices.size());
      ArrayList<SmartDevice> newList = new ArrayList<>(devices);
      newList.add(device);
      DiffUtilCallback diffUtilCallback = new DiffUtilCallback(devices, newList);
      DiffResult diff = DiffUtil.calculateDiff(diffUtilCallback);
      devices.clear();
      devices.addAll(newList);
      diff.dispatchUpdatesTo(deviceAdapter);
      storeDevices();
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
