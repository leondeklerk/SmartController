package com.leondeklerk.smartcontroller;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.fragment.app.FragmentActivity;
import com.leondeklerk.smartcontroller.databinding.ActivityDeviceColorBinding;
import com.leondeklerk.smartcontroller.devices.RGBLedController;
import com.leondeklerk.smartcontroller.devices.SmartDevice;
import com.leondeklerk.smartcontroller.utils.DeviceStorageUtils;
import java.util.ArrayList;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Activity that is uses to view and update the color of a RGBLedController. Uses the shared MQTT
 * client to execute its actions and is also responsive to network changes. Makes use of the
 * corresponding layout.
 */
public class DeviceColorActivity extends FragmentActivity
    implements View.OnClickListener, ConnectionsHandler {

  public static final String EXTRA_SELECTED_DEV = "com.leondeklerk.smartcontroller.SELECTED_DEV";
  private ActivityDeviceColorBinding binding;
  private RGBLedController device;
  private MqttClient client;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    NetworkHandler handler = NetworkHandler.getHandler();
    handler.setCurrentHandler(this);

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

    // Setup the MqttCient and register the correct receiver.
    client = MqttClient.getInstance(getApplicationContext());
    client.registerHandler("DeviceColorActivity", this);
    client.setHandler("DeviceColorActivity");

    SharedPreferences preferences =
        this.getSharedPreferences(getString(R.string.dev_prefs), Context.MODE_PRIVATE);
    DeviceStorageUtils deviceStorageUtils = new DeviceStorageUtils(preferences, this);

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
  protected void onDestroy() {
    super.onDestroy();
    client.setHandler("MainActivity");
  }

  @Override
  public void onMqttMessage(String topic, MqttMessage message) {
    parseResponse(message);
  }

  @Override
  public void onMqttSubscribe() {}

  @Override
  public void onMqttConnected(boolean connected) {}

  @Override
  public void onNetworkChange() {
    client = MqttClient.reconnect(this);
  }

  /**
   * Parse the response from a received MQTT message and update the layout accordingly.
   *
   * @param message the mesage to parse.
   */
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
}
