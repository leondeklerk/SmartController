package com.leondeklerk.smartcontroller;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import androidx.fragment.app.FragmentActivity;
import com.google.gson.Gson;
import com.leondeklerk.smartcontroller.data.Command;
import com.leondeklerk.smartcontroller.data.Entry;
import com.leondeklerk.smartcontroller.databinding.ActivityDeviceColorBinding;
import com.leondeklerk.smartcontroller.devices.RGBLedController;
import com.leondeklerk.smartcontroller.devices.SmartDevice;
import com.leondeklerk.smartcontroller.utils.DeviceStorageUtils;
import java.util.ArrayList;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

public class DeviceColorActivity extends FragmentActivity implements View.OnClickListener,
    MqttCallback {

  public static final String EXTRA_SELECTED_DEV = "com.leondeklerk.smartcontroller.SELECTED_DEV";
  private ActivityDeviceColorBinding binding;
  private RGBLedController device;
  private MqttClient client;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    binding = ActivityDeviceColorBinding.inflate(getLayoutInflater());
    View view = binding.getRoot();
    setContentView(view);

    binding.toolbar.setNavigationOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            onBackPressed();
          }
        });

    Intent intent = getIntent();
    int deviceNum = intent.getIntExtra(EXTRA_SELECTED_DEV, 0);
    client = MqttClient.getInstance(getApplicationContext());
    client.registerCallback("DeviceColorActivity", this);
    client.setCallback("DeviceColorActivity");

    Context context = this;

    SharedPreferences preferences =
        this.getSharedPreferences(getString(R.string.dev_prefs), Context.MODE_PRIVATE);
    DeviceStorageUtils deviceStorageUtils = new DeviceStorageUtils(preferences, context);

    ArrayList<SmartDevice> devices = deviceStorageUtils.getDevices();
    device = new RGBLedController(devices.get(deviceNum).getData());

    client.publish(device.getColor());

    binding.colorInfo.setText(device.getData().getName());

    binding.colorCancel.setOnClickListener(this);
    binding.colorSet.setOnClickListener(this);
  }

  @Override
  public void onClick(View v) {
    int id = v.getId();
    switch (id) {
      case R.id.color_cancel:
        this.onBackPressed();
        break;
      case R.id.color_set:
        int red = (int) binding.sliderRed.getValue();
        int green = (int) binding.sliderGreen.getValue();
        int blue = (int) binding.sliderBlue.getValue();

        client.publish(device.setColor(red, green, blue));
        break;
      default:
        Log.d("Clicked", "Non-existent button clicked (color)");
    }
  }

  @Override
  public void connectionLost(Throwable cause) {

  }

  @Override
  public void messageArrived(String topic, MqttMessage message) {
    parseResponse(message);
  }

  @Override
  public void deliveryComplete(IMqttDeliveryToken token) {

  }

  private void parseResponse(MqttMessage message) {
    String colorString = "";
    try {
      JSONObject obj = new JSONObject(message.toString());
      colorString = obj.getString("Color");
    } catch (JSONException e) {
      e.printStackTrace();
    }
    String[] colors = colorString.split(",");
    binding.sliderRed.setValue(Float.parseFloat(colors[0]));
    binding.sliderGreen.setValue(Float.parseFloat(colors[1]));
    binding.sliderBlue.setValue(Float.parseFloat(colors[2]));

  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    client.setCallback("MainActivity");
  }
}
