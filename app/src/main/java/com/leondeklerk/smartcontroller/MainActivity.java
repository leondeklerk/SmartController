package com.leondeklerk.smartcontroller;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.DiffUtil.DiffResult;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.leondeklerk.smartcontroller.data.Entry;
import com.leondeklerk.smartcontroller.databinding.ActivityMainBinding;
import com.leondeklerk.smartcontroller.databinding.DeviceDialogBinding;
import com.leondeklerk.smartcontroller.devices.SmartDevice;
import com.leondeklerk.smartcontroller.utils.DeviceStorageUtils;
import com.leondeklerk.smartcontroller.utils.DiffUtilCallback;
import com.leondeklerk.smartcontroller.utils.TextInputUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Main Activity of the application. Contains the basis navigation for the settings and help pages.
 * Its main layout contains the RecyclerView with all the device cards in it. Providing an up to
 * date status of all devices, taking network and preference changes into account. Also contains a
 * FAB and logic to add new devices.
 */
public class MainActivity extends AppCompatActivity
    implements View.OnClickListener,
        SwipeRefreshLayout.OnRefreshListener,
        Toolbar.OnMenuItemClickListener,
        ConnectionsHandler {

  static final String EXTRA_DEV_REMOVED = "com.leondeklerk.smartcontroller.DEV_REMOVED";
  static final String EXTRA_DEV_CHANGED = "com.leondeklerk.smartcontroller.DEV_CHANGED";
  static final String EXTRA_PREFS_CHANGED = "com.leondeklerk.smartcontroller.PREFS_CHANGED";
  private DeviceDialogBinding dialogBinding;
  private DeviceStorageUtils deviceStorageUtils;
  private ArrayList<TextInputLayout> layouts;
  private Map<String, Entry> deviceMap;
  @Getter private MqttClient mqttClient;
  private NetworkHandler networkHandler;
  private boolean connected;
  DeviceAdapter deviceAdapter;
  Context context;
  ArrayList<SmartDevice> devices;
  AlertDialog addDeviceDialog;
  SharedPreferences preferences;
  SwipeRefreshLayout refreshLayout;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Register the Network change handler
    networkHandler = NetworkHandler.getHandler();
    networkHandler.register(this);
    networkHandler.setCurrentHandler(this);

    // Bind the MainActivity layout file
    com.leondeklerk.smartcontroller.databinding.ActivityMainBinding binding =
        ActivityMainBinding.inflate(getLayoutInflater());
    View view = binding.getRoot();
    setContentView(view);

    context = this;
    preferences = this.getSharedPreferences(getString(R.string.dev_prefs), Context.MODE_PRIVATE);

    // Get the MQTT client.
    mqttClient = MqttClient.getInstance(this);

    deviceStorageUtils = new DeviceStorageUtils(preferences, context);
    deviceMap = new HashMap<>();

    devices = deviceStorageUtils.getDevices();

    buildDeviceMap();

    // Create a RecyclerView for the deviceCards
    RecyclerView recyclerView = binding.deviceList;
    recyclerView.setHasFixedSize(true);

    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
    recyclerView.setLayoutManager(layoutManager);

    deviceAdapter = new DeviceAdapter(devices, this);
    recyclerView.setAdapter(deviceAdapter);

    // Set the refresh layout
    refreshLayout = binding.deviceListRefresh;
    refreshLayout.setOnRefreshListener(this);

    binding.toolbar.setOnMenuItemClickListener(this);

    // Set the FAB listener for device creation
    binding.fab.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            addDeviceDialog = createDeviceDialog();
            addDeviceDialog.show();
            Button button = addDeviceDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            button.setOnClickListener((View.OnClickListener) context);
          }
        });
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    // Unregister the handler
    if (networkHandler != null) {
      Log.d("MainActivity@onDestroy#handler", "unregistered");
      networkHandler.unregister(this);
    }

    // Delete the MQTT client
    if (mqttClient != null) {
      Log.d("MainActivity@onDestroy#client", "unregistered");
      mqttClient.destroy();
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    networkHandler.setCurrentHandler(this);
    if (mqttClient.getCurrentHandler() != this) {
      Log.d("MainActivity@onResume#notThis", "not the current handler");
      mqttClient.setHandler("MainActivity");
    }

    if(!mqttClient.isConnected()) {
      connected = false;
      mqttClient = MqttClient.reconnect(this);
    }

    pingStatus(-1);
  }

  @Override
  public void onClick(View v) {
    // Check if any input field has errors
    if (!TextInputUtils.hasErrors(layouts)) {
      // Cancel all tasks and dismiss the dialog
      addDeviceDialog.dismiss();

      // Get the type of SmartDevice
      int typeId = dialogBinding.newType.getCheckedButtonId();
      String type = TextInputUtils.DEV_TYPE_DEF;
      if (typeId == dialogBinding.typeController.getId()) {
        type = TextInputUtils.DEV_TYPE_RGB;
      }

      // Create the new device and add it
      SmartDevice device = TextInputUtils.readDevice(context, type, layouts, devices.size());
      ArrayList<SmartDevice> newList = new ArrayList<>(devices);
      newList.add(device);

      updateAdapter(devices, newList);

      // (Re)Build the device map
      buildDeviceMap();

      // Store the new list of devices
      deviceStorageUtils.storeDevices(devices);

      // Ping the status of the new device
      pingStatus(devices.size() - 1);
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == 0) {
      // If the activity closed normally
      if (resultCode == RESULT_OK) {
        int removed = data.getIntExtra(EXTRA_DEV_REMOVED, -1);
        if (removed >= 0) {
          updateAdapter(devices, deviceStorageUtils.getDevices());
          pingStatus(-1);
        }

        int changed = data.getIntExtra(EXTRA_DEV_CHANGED, -1);
        if (changed >= 0) {
          updateAdapter(devices, deviceStorageUtils.getDevices());
          pingStatus(changed);
        }
      }
    } else if (requestCode == 1) {
      // If the PreferenceActivity closed normally.
      if (resultCode == RESULT_OK) {
        if (data.getBooleanExtra(EXTRA_PREFS_CHANGED, false)) {
          // If the preferences changed the MqttClient needs to reconnect to the server.
          connected = false;
          mqttClient = MqttClient.reconnect(this);
        }
      }
    }
  }

  @Override
  public boolean onMenuItemClick(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.settings:
        // Open the settings screen.
        Intent intent = new Intent(context, SettingsActivity.class);
        startActivityForResult(intent, 1);
        return true;
      case R.id.help:
        Log.d("MainActivity@onMenuItemClick#help", "Reached help");
        return true;
      default:
        return false;
    }
  }

  @Override
  public void onRefresh() {
    Log.d("MainActivity@onRefresh", "refreshed");
    // Ping all devices
    pingStatus(-1);
  }

  @Override
  public void onMqttMessage(String topic, MqttMessage message) {
    Log.d("MainActivity@onMqttMessage", "Messaged arrived: " + message.toString());
    Pair<String, Boolean> parsedTopic = getTopic(topic);
    if (parsedTopic.second) {
      Entry entry = deviceMap.get(parsedTopic.first);
      if (entry != null) {
        parseResponse(message, entry);
      }
    }
  }

  @Override
  public void onMqttSubscribe() {
    Log.d("MainActivity@onMqttSubscribe", "subscribed");

    // Set connected to true and register the handlers.
    connected = true;
    mqttClient.registerHandler("MainActivity", this);
    mqttClient.setHandler("MainActivity");

    // Ping all devices for their status
    pingStatus(-1);
  }

  @Override
  public void onMqttConnected(boolean connected) {
    Log.d("MainActivity@onMqttConnected", String.valueOf(connected));
    // If no connection could be made, notify the users.
    if (!connected) {
      Toast.makeText(
              context,
              "No connection to the MQTT server (change your preferences?)",
              Toast.LENGTH_SHORT)
          .show();
      pingStatus(-1);
    }
  }

  @Override
  public void onNetworkChange() {
    Log.d("MainActivity@onNetworkChange", "changed");
    // If the network changed. Change all device statuses and try to reconnect the MqttClient.
    resetStatus();
    connected = false;
    mqttClient = MqttClient.reconnect(this);
  }

  /**
   * Create a dialog which asks the user for input, also registers relevant listeners for the dialog
   * UI.
   *
   * @return A AlertDialog to be used for creating new devices.
   */
  public AlertDialog createDeviceDialog() {
    // Create a binding based on the device_dialog layout
    dialogBinding = DeviceDialogBinding.inflate(LayoutInflater.from(context));

    // create a dialog
    AlertDialog dialog =
        new MaterialAlertDialogBuilder(context, R.style.MaterialAlertDialog_FilledButtonDialog)
            .setTitle(getString(R.string.add_device_title))
            .setView(dialogBinding.getRoot())
            .setPositiveButton(getString(R.string.add_button_confirm), null)
            .setNegativeButton(getString(android.R.string.cancel), null)
            .create();

    // Add all TextInputLayouts to a the list for error checking
    layouts = new ArrayList<>();
    layouts.add(dialogBinding.newName);
    layouts.add(dialogBinding.newTopic);

    // Register error listeners
    TextInputUtils.setListener(dialogBinding.newName, TextInputUtils.DEFAULT_TYPE);
    TextInputUtils.setListener(dialogBinding.newTopic, TextInputUtils.DEFAULT_TYPE);

    return dialog;
  }

  /**
   * Ping a device for its status, will ping all devices if -1 was supplied.
   *
   * @param id the id of the device to ping.
   */
  public void pingStatus(int id) {
    if (connected) {
      Log.d("MainActivity@pingStatus#if", "connected");
      if (id >= 0) {
        devices.get(id).getData().setStatus(getString(R.string.status_unknown));
        mqttClient.publish(devices.get(id).getPowerStatus());
      } else {
        // Ping all devices
        for (int i = 0; i < devices.size(); i++) {
          devices.get(i).getData().setStatus(getString(R.string.status_unknown));
          mqttClient.publish(devices.get(i).getPowerStatus());
        }
      }
    } else {
      Log.d("MainActivity@pingStatus#else", "not connected");
      resetStatus();
      refreshLayout.setRefreshing(false);
    }
  }

  /**
   * Calculate the difference between two lists of devices and dispatch this to the DeviceAdapter to
   * update the content of the RecyclerView.
   *
   * @param oldList the current list of the RecyclerView.
   * @param newList the new list to calculate the difference with.
   */
  public void updateAdapter(ArrayList<SmartDevice> oldList, ArrayList<SmartDevice> newList) {
    // Calculate the difference
    DiffUtilCallback diffUtilCallback = new DiffUtilCallback(oldList, newList);
    DiffResult diff = DiffUtil.calculateDiff(diffUtilCallback);

    // Set the list of devices to be up to date
    devices.clear();
    devices.addAll(newList);

    buildDeviceMap();

    diff.dispatchUpdatesTo(deviceAdapter);
  }

  /** Built a device map from the list of devices available, with the topic as a key. */
  private void buildDeviceMap() {
    // Reset the current map
    deviceMap.clear();

    // Fill it with all the entries
    for (int i = 0; i < devices.size(); i++) {
      deviceMap.put(devices.get(i).getData().getTopic(), new Entry(i, devices.get(i)));
    }
  }

  /**
   * Extract the device topic from the topic of the arrived message. The extracted topic is used to
   * identify the id and device associated with this message.
   *
   * @param input the message topic
   * @return A pair with the device topic and a boolean indicating if the topic was valid or not
   */
  private Pair<String, Boolean> getTopic(String input) {
    String[] split = input.split("/");
    if (split.length > 2) {
      int start = split[0].length() + 1;
      int end = input.length() - split[split.length - 1].length() - 1;
      return new Pair<>(input.substring(start, end), true);
    } else {
      return new Pair<>(null, false);
    }
  }

  /**
   * Parse the message and handle the outcome.
   *
   * @param message The message ot parse.
   * @param entry The entry to change based on the message.
   */
  private void parseResponse(MqttMessage message, Entry entry) {
    String statusString;
    try {
      JSONObject obj = new JSONObject(message.toString());
      statusString = obj.getString("POWER");
    } catch (JSONException e) {
      Log.d("MainActivity@parseErsponse#catch", "not parsable", e);
      entry.getDevice().getData().setStatus(getString(R.string.status_unknown));
      e.printStackTrace();
      return;
    }
    // Set the values according to the response
    if (statusString.equals("ON")) {
      entry.getDevice().getData().setStatus(getString(R.string.status_on));
    } else {
      entry.getDevice().getData().setStatus(getString(R.string.status_off));
    }

    refreshLayout.setRefreshing(false);
    // Update the RecyclerView
    deviceAdapter.notifyItemChanged(entry.getId());
  }

  /** Reset the status of all devices. */
  private void resetStatus() {
    for (SmartDevice device : devices) {
      device.getData().setStatus(getString(R.string.status_unknown));
    }
  }
}
