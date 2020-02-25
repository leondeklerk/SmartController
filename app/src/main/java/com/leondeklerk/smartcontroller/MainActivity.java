package com.leondeklerk.smartcontroller;

import android.content.Context;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.leondeklerk.smartcontroller.data.DeviceData;
import com.leondeklerk.smartcontroller.data.Response;
import com.leondeklerk.smartcontroller.devices.SmartDevice;
import com.leondeklerk.smartcontroller.utils.IpInputFilter;
import com.leondeklerk.smartcontroller.widget.ColorDotView;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity
    implements NetworkCallback, CompoundButton.OnCheckedChangeListener {
  SwitchMaterial ledToggle;
  Context context;
  SmartDevice device;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    context = this;
    //    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    DeviceData data =
        new DeviceData(0, "192.168.1.217", "LED Socket", true)
            .setPassword("LDK.Tasmota2020")
            .setUsername("admin");
    device = new SmartDevice(data);

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
            AlertDialog addDeviceDialog = createDeviceDialog(v);
            addDeviceDialog.show();
          }
        });
  }

  @Override
  public void onFinish(Response response) {
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

  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    Log.d("Switch", "Clicked");
    NetworkTask task = new NetworkTask((NetworkCallback) context);
    task.execute(device.getCommand(device.turnOn(isChecked)));
  }

  public AlertDialog createDeviceDialog(View v) {
    FrameLayout layout = new FrameLayout(v.getContext());

    AlertDialog dialog =
        new MaterialAlertDialogBuilder(v.getContext())
            .setTitle("Add new Device")
            .setView(layout)
            .setPositiveButton("add", null)
            .setNegativeButton("Cancel", null)
            .create();

    final View dialogView = dialog.getLayoutInflater().inflate(R.layout.device_dialog, layout);

    // The InputLayout always has a EditText since this is supplied with the layout
    TextInputLayout ipText = dialogView.findViewById(R.id.newIp);
    //noinspection ConstantConditions
    ipText.getEditText().setFilters(new InputFilter[]{new IpInputFilter()});

    // Add a listener to the switch to enable / disable
    SwitchMaterial credentials = dialogView.findViewById(R.id.switchCredentials);
    credentials.setOnCheckedChangeListener(
        new CompoundButton.OnCheckedChangeListener() {
          @Override
          public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            dialogView.findViewById(R.id.newUsername).setEnabled(isChecked);
            dialogView.findViewById(R.id.newPassword).setEnabled(isChecked);
          }
        });
    return dialog;
  }

  public void setVisibility(View view, boolean on) {
    if (on) {
      view.setVisibility(View.VISIBLE);
    } else {
      view.setVisibility(View.INVISIBLE);
    }
  }
}
