package com.leondeklerk.smartcontroller;

import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.leondeklerk.smartcontroller.data.DeviceData;
import com.leondeklerk.smartcontroller.data.Response;
import com.leondeklerk.smartcontroller.devices.SmartDevice;
import com.leondeklerk.smartcontroller.widget.ColorDotView;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements NetworkCallback, CompoundButton.OnCheckedChangeListener{
  SwitchMaterial ledToggle;
  Context context;
  SmartDevice device;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    context = this;
//    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    DeviceData data = new DeviceData(0, "192.168.1.217", "LED Socket", "LDK.Tasmota2020", "admin");
    device = new SmartDevice(data);

    ColorDotView colorDotView = findViewById(R.id.statusLed);
    setVisibility(colorDotView, false);
    TextView statusView = findViewById(R.id.deviceStatus);
    statusView.setText(getString(R.string.device_status, getString(R.string.status_unknown)));
    TextView ip = findViewById(R.id.deviceIp);
    ip.setText(getString(R.string.device_ip, data.getIp()));
    NetworkTask status = new NetworkTask((NetworkCallback) context);
    status.execute(device.getCommand(device.getPowerStatus()));
    ((TextView) findViewById(R.id.deviceName)).setText(getString(R.string.device_name, data.getName()));
    ledToggle = (findViewById(R.id.deviceCard)).findViewById(R.id.devicePower);
    ledToggle.setOnCheckedChangeListener(this);
    FloatingActionButton fab = findViewById(R.id.fab);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        new MaterialAlertDialogBuilder(v.getContext())
            .setTitle("Add new Device")
            .setView(R.layout.device_dialog)
            .setPositiveButton(
                "add", null)
            .setNegativeButton("Cancel", null).create().show();
      }
    });
  }

  @Override
  public void onFinish(Response response) {
    ledToggle.setOnCheckedChangeListener(null);
    ColorDotView colorDotView = findViewById(R.id.statusLed);
    TextView status = findViewById(R.id.deviceStatus);
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

  public void setVisibility(View view, boolean on) {
    if (on) {
      view.setVisibility(View.VISIBLE);
    } else {
      view.setVisibility(View.INVISIBLE);
    }
  }

  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
      Log.d("Switch", "Clicked");
      NetworkTask task = new NetworkTask((NetworkCallback) context);
      task.execute(device.getCommand(device.turnOn(isChecked)));
  }
}
