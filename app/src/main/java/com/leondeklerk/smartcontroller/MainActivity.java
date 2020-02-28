package com.leondeklerk.smartcontroller;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.leondeklerk.smartcontroller.data.DeviceData;
import com.leondeklerk.smartcontroller.data.Response;
import com.leondeklerk.smartcontroller.devices.SmartDevice;
import com.leondeklerk.smartcontroller.utils.IpInputFilter;
import com.leondeklerk.smartcontroller.utils.TextInputLayoutUtils;
import com.leondeklerk.smartcontroller.widget.ColorDotView;
import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity
    implements NetworkCallback, CompoundButton.OnCheckedChangeListener, View.OnClickListener {

  private RecyclerView recyclerView;
  private RecyclerView.Adapter mAdapter;
  private RecyclerView.LayoutManager layoutManager;
  SwitchMaterial ledToggle;
  Context context;
  SmartDevice device;
  ArrayList<SmartDevice> devices;
  TextInputLayoutUtils layoutUtils;
  AlertDialog addDeviceDialog;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    context = this;
    //    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    DeviceData data =
        new DeviceData(0, "LED Socket", "192.168.1.217", true)
            .setUsername("admin")
            .setPassword("LDK.Tasmota2020");
    device = new SmartDevice(data);

    recyclerView = findViewById(R.id.deviceList);

    // use this setting to improve performance if you know that changes
    // in content do not change the layout size of the RecyclerView
    recyclerView.setHasFixedSize(true);

    // use a linear layout manager
    layoutManager = new LinearLayoutManager(this);
    recyclerView.setLayoutManager(layoutManager);

    // specify an adapter (see also next example)
    devices = new ArrayList<>();
    devices.add(device);
    Adapter mAdapter = new DeviceAdapter(devices, context);
    recyclerView.setAdapter(mAdapter);

    ColorDotView colorDotView = findViewById(R.id.statusLed);
    setVisibility(colorDotView, false);
    MaterialTextView statusView = findViewById(R.id.deviceStatus);
    statusView.setText(getString(R.string.device_status, getString(R.string.status_unknown)));
    MaterialTextView ip = findViewById(R.id.deviceIp);
    ip.setText(getString(R.string.device_ip, data.getIp()));
    NetworkTask status = new NetworkTask((NetworkCallback) context);
    status.execute(device.getCommand(device.getPowerStatus()));
    ((MaterialTextView) findViewById(R.id.deviceName))
        .setText(getString(R.string.device_name, data.getName()));
    ledToggle = (findViewById(R.id.deviceCard)).findViewById(R.id.devicePower);
    ledToggle.setOnCheckedChangeListener(this);

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
  public void onFinish(Response response) {
//    DeviceAdapter.CardViewHolder holder = (CardViewHolder) recyclerView.getChildViewHolder(recyclerView.getChildAt(0));
//    ((MaterialTextView) holder.cardView.findViewById(R.id.deviceName)).setText("123");
    ledToggle.setOnCheckedChangeListener(null);
    ColorDotView colorDotView = findViewById(R.id.statusLed);
    MaterialTextView status = findViewById(R.id.deviceStatus);
    if (response.getException() != null) {
      Log.d("Network error", response.getException().toString());
      status.setText(getString(R.string.device_status, getString(R.string.status_unknown)));
      ledToggle.setChecked(false);
      setVisibility(colorDotView, false);
    } else {
      setVisibility(colorDotView, true);
      String statusString = null;
      try {
        JSONObject obj = new JSONObject(response.getResponse());
        statusString = obj.getString("POWER");
      } catch (JSONException e) {
        e.printStackTrace();
      }
      if (statusString.equals("ON")) {
        if (!ledToggle.isChecked()) {
          ledToggle.setChecked(true);
        }
        colorDotView.setFillColor(getColor(R.color.status_on));
      } else {
        if (ledToggle.isChecked()) {
          ledToggle.setChecked(false);
        }
        colorDotView.setFillColor(getColor(R.color.status_off));
      }
      status.setText(getString(R.string.device_status, statusString));
      Log.d("Response", response.getResponse());
    }
    ledToggle.setOnCheckedChangeListener(this);
  }

  /**
   * @param buttonView
   * @param isChecked
   */
  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    Log.d("Switch", "Clicked");
    NetworkTask task = new NetworkTask((NetworkCallback) context);
    task.execute(device.getCommand(device.turnOn(isChecked)));
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
    ipLayout.getEditText().setFilters(new InputFilter[]{new IpInputFilter()});

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

  /**
   * @param view
   * @param on
   */
  public void setVisibility(View view, boolean on) {
    if (on) {
      view.setVisibility(View.VISIBLE);
    } else {
      view.setVisibility(View.INVISIBLE);
    }
  }

  @Override
  public void onClick(View v) {
    if (!layoutUtils.hasErrors()) {
      addDeviceDialog.dismiss();
      SwitchMaterial switchMaterial = addDeviceDialog.findViewById(R.id.switchCredentials);
      boolean isProtected = switchMaterial.isChecked();

      SmartDevice device = layoutUtils.readDevice(isProtected, devices.size());
      devices.add(device);

      Adapter mAdapter = new DeviceAdapter(devices, context);
      recyclerView.setAdapter(mAdapter);
    }
  }
}
