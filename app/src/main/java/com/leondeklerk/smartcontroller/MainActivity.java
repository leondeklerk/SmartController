package com.leondeklerk.smartcontroller;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.leondeklerk.smartcontroller.data.Response;
import com.leondeklerk.smartcontroller.widget.ColorDotView;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements NetworkCallback {
  SwitchMaterial ledToggle;
  Context context;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    context = this;
//    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    ColorDotView colorDotView = findViewById(R.id.statusLed);
    setVisibility(colorDotView, false);
    TextView statusView = findViewById(R.id.deviceStatus);
    statusView.setText(getString(R.string.device_status, getString(R.string.status_unknown)));
    TextView ip = findViewById(R.id.deviceIp);
    ip.setText(getString(R.string.device_ip, "192.168.1.217"));
    NetworkTask status = new NetworkTask((NetworkCallback) context);
    status.execute("http://192.168.1.217/cm?&user=admin&password=LDK.Tasmota2020&cmnd=Power");
    ((TextView) findViewById(R.id.deviceName)).setText(getString(R.string.device_name, "Led Controllers"));
    ledToggle = (findViewById(R.id.deviceCard)).findViewById(R.id.devicePower);
    ledToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        NetworkTask task = new NetworkTask((NetworkCallback) context);
        task.execute("http://192.168.1.217/cm?&user=admin&password=LDK.Tasmota2020&cmnd=Power%20TOGGLE");
      }
    });
  }

  @Override
  public void onFinish(Response response) {
    ColorDotView colorDotView = findViewById(R.id.statusLed);
    TextView status = findViewById(R.id.deviceStatus);
    if (response.getException() != null) {
      Log.d("Network error", response.getException().toString());
      status.setText(getString(R.string.device_status, getString(R.string.status_unknown)));
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
        colorDotView.setFillColor(getColor(R.color.status_on));
      } else {
        colorDotView.setFillColor(getColor(R.color.status_off));
      }
      status.setText(getString(R.string.device_status, statusString));
      Toast.makeText(this, response.getResponse(), Toast.LENGTH_LONG).show();
    }
  }

  public void setVisibility(View view, boolean on){
    if(on) {
      view.setVisibility(View.VISIBLE);
    } else {
      view.setVisibility(View.INVISIBLE);
    }
  }
}
