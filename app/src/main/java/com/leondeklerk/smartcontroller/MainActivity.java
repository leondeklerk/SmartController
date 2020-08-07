package com.leondeklerk.smartcontroller;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.DiffUtil.DiffResult;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.leondeklerk.smartcontroller.data.DeviceData;
import com.leondeklerk.smartcontroller.data.Response;
import com.leondeklerk.smartcontroller.databinding.ActivityMainBinding;
import com.leondeklerk.smartcontroller.databinding.DeviceDialogBinding;
import com.leondeklerk.smartcontroller.devices.SmartDevice;
import com.leondeklerk.smartcontroller.utils.DeviceStorageUtils;
import com.leondeklerk.smartcontroller.utils.DiffUtilCallback;
import com.leondeklerk.smartcontroller.utils.TextInputUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity
    implements NetworkCallback, View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

  public static final String EXTRA_DEV_REMOVED = "com.leondeklerk.smartcontroller.DEV_REMOVED";
  public static final String EXTRA_DEV_CHANGED = "com.leondeklerk.smartcontroller.DEV_CHANGED";
  private DeviceDialogBinding dialogBinding;
  private Map<Integer, ResponseTask> taskMap;
  private DeviceStorageUtils deviceStorageUtils;
  private ArrayList<TextInputLayout> layouts;
  DeviceAdapter deviceAdapter;
  Context context;
  ArrayList<SmartDevice> devices;
  AlertDialog addDeviceDialog;
  SharedPreferences preferences;
  SwipeRefreshLayout refreshLayout;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Bind the MainActivity layout file
    com.leondeklerk.smartcontroller.databinding.ActivityMainBinding binding =
        ActivityMainBinding.inflate(getLayoutInflater());
    View view = binding.getRoot();
    setContentView(view);

    context = this;
    preferences = this.getSharedPreferences(getString(R.string.dev_prefs), Context.MODE_PRIVATE);

    deviceStorageUtils = new DeviceStorageUtils(preferences);
    taskMap = new HashMap<>();

    devices = deviceStorageUtils.getDevices();

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

    // Ping the devices for their status
    pingStatus(-1);

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
  public void onPreExecute(ResponseTask task) {
    int taskIndex = task.getDeviceNum();
    ResponseTask curTask = taskMap.get(taskIndex);
    if (curTask != null) {
      curTask.cancel(true);
    }
    taskMap.put(taskIndex, task);
  }

  @Override
  public void onFinish(ResponseTask task, Response response, int deviceNum) {
    // Remove the task from the list of tasks
    taskMap.remove(deviceNum);

    DeviceData device = devices.get(deviceNum).getData();

    if (response.getException() != null) {
      // If there was an error, log it and set status to UNKNOWN
      Log.d("Network error", response.getException().toString());
      device.setStatus("UNKNOWN");
    } else {
      // If there was a response, retrieve the response
      String statusString;
      try {
        JSONObject obj = new JSONObject(response.getResponse());
        statusString = obj.getString("POWER");
      } catch (JSONException e) {
        device.setStatus("UNKNOWN");
        e.printStackTrace();
        Log.d("JSON Response", response.getResponse());
        return;
      }
      // Set the values according to the response
      if (statusString.equals("ON")) {
        device.setStatus("ON");
      } else {
        device.setStatus("OFF");
      }
      Log.d("Response", response.getResponse());
    }

    // If this was the last task, turn the refreshing animation off
    if(taskMap.isEmpty()) {
      refreshLayout.setRefreshing(false);
    }

    // Update the RecyclerView
    deviceAdapter.notifyItemChanged(deviceNum);
  }

  @Override
  public void onCancel(ResponseTask task) {
    taskMap.remove(task.getDeviceNum());
  }

  @Override
  public void onClick(View v) {
    // Check if the ip is in the correct format
    TextInputUtils.checkIp(dialogBinding.newIp);

    // Check if any input field has errors
    if (!TextInputUtils.hasErrors(layouts)) {
      // Cancel all tasks and dismiss the dialog
      addDeviceDialog.dismiss();

      // Check if the credentials part is enabled
      boolean isProtected = dialogBinding.switchCredentials.isChecked();

      // Get the type of SmartDevice
      int typeId = dialogBinding.newType.getCheckedButtonId();
      String type = TextInputUtils.DEV_TYPE_DEF;
      if (typeId == dialogBinding.typeController.getId()) {
        type = TextInputUtils.DEV_TYPE_RGB;
      }

      // Create the new device and add it
      SmartDevice device = TextInputUtils.readDevice(type, layouts, isProtected, devices.size());
      ArrayList<SmartDevice> newList = new ArrayList<>(devices);
      newList.add(device);

      updateAdapter(devices, newList);

      // Store the new list of devices
      deviceStorageUtils.storeDevices(devices);

      // Ping the status of the new device
      pingStatus(devices.size() - 1);
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    // If the activity closed normally
    if (resultCode == RESULT_OK) {
      int removed = data.getIntExtra(EXTRA_DEV_REMOVED, -1);
      if (removed >= 0) {
        ResponseTask task = taskMap.get(removed);
        if (task != null) task.cancel(true);
        updateAdapter(devices, deviceStorageUtils.getDevices());
      }

      int changed = data.getIntExtra(EXTRA_DEV_CHANGED, -1);
      if (changed >= 0) {
        ResponseTask task = taskMap.get(changed);
        if (task != null) task.cancel(true);
        updateAdapter(devices, deviceStorageUtils.getDevices());
        pingStatus(changed);
      }
    }
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
    layouts.add(dialogBinding.newIp);

    // Register error listeners
    TextInputUtils.setListener(dialogBinding.newName, TextInputUtils.DEFAULT_TYPE);
    TextInputUtils.setListener(dialogBinding.newIp, TextInputUtils.IP_TYPE);

    // Add a listener to the switch to enable / disable credentials
    dialogBinding.switchCredentials.setOnCheckedChangeListener(
        new CompoundButton.OnCheckedChangeListener() {
          @Override
          public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            setCredentialsAvailability(dialogBinding, isChecked);
          }
        });
    return dialog;
  }

  /**
   * Enable or disable the credential input fields on a device creation Dialog. This also registers
   * the correct error listeners.
   *
   * @param dialogBinding the view binding containing the views of the dialog.
   * @param enable boolean whether the fields should be enabled or not
   */
  public void setCredentialsAvailability(DeviceDialogBinding dialogBinding, boolean enable) {
    dialogBinding.newUsername.setEnabled(enable);
    dialogBinding.newPassword.setEnabled(enable);

    // Add the layouts and there error listeners  if they are enabled or remove them if not
    if (enable) {
      layouts.add(dialogBinding.newUsername);
      TextInputUtils.setListener(dialogBinding.newUsername, TextInputUtils.DEFAULT_TYPE);

      layouts.add(dialogBinding.newPassword);
      TextInputUtils.setListener(dialogBinding.newPassword, TextInputUtils.DEFAULT_TYPE);
    } else {
      layouts.remove(dialogBinding.newUsername);
      layouts.remove(dialogBinding.newPassword);
    }
  }

  /**
   * Ping a device for its status, will ping all devices if -1 was supplied.
   *
   * @param id the id of the device to ping.
   */
  public void pingStatus(int id) {
    if (id >= 0) {
      createStatusTask(id);
    } else {
      // Ping all devices
      for (int i = 0; i < devices.size(); i++) {
        createStatusTask(i);
      }
    }
  }

  /**
   * Create a network task to ping a device for its status.
   *
   * @param id the id of the device.
   */
  public void createStatusTask(int id) {
    refreshLayout.setRefreshing(true);

    ResponseTask task = new ResponseTask((NetworkCallback) context, id);
    SmartDevice device = devices.get(id);
    task.executeOnExecutor(
        AsyncTask.THREAD_POOL_EXECUTOR, device.getCommand(device.getPowerStatus()));
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

    diff.dispatchUpdatesTo(deviceAdapter);
  }

  @Override
  public void onRefresh() {
    // Ping all devices
    pingStatus(-1);
  }
}
