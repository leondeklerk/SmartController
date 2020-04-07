package com.leondeklerk.smartcontroller;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputLayout;
import com.leondeklerk.smartcontroller.data.DeviceData;
import com.leondeklerk.smartcontroller.data.Response;
import com.leondeklerk.smartcontroller.databinding.ActivityMainBinding;
import com.leondeklerk.smartcontroller.devices.SmartDevice;
import com.leondeklerk.smartcontroller.utils.DeviceStorageUtils;
import com.leondeklerk.smartcontroller.utils.DiffUtilCallback;
import com.leondeklerk.smartcontroller.utils.IpInputFilter;
import com.leondeklerk.smartcontroller.utils.TextInputLayoutUtils;
import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity
    implements NetworkCallback, View.OnClickListener {

  public static final String EXTRA_DEV_CHANGED = "com.leondeklerk.smartcontroller.DEV_CHANGED";
  private ActivityMainBinding binding;
  private RecyclerView recyclerView;
  private ArrayList<NetworkTask> tasks;
  private DeviceStorageUtils deviceStorageUtils;
  DeviceAdapter deviceAdapter;
  Context context;
  ArrayList<SmartDevice> devices;
  TextInputLayoutUtils layoutUtils;
  AlertDialog addDeviceDialog;
  SharedPreferences preferences;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    binding = ActivityMainBinding.inflate(getLayoutInflater());
    View view = binding.getRoot();
    setContentView(view);

    context = this;
    preferences = this.getSharedPreferences(getString(R.string.dev_prefs), Context.MODE_PRIVATE);

    deviceStorageUtils = new DeviceStorageUtils(preferences);
    tasks = new ArrayList<>();

    devices = deviceStorageUtils.getDevices();

    recyclerView = binding.deviceList;
    recyclerView.setHasFixedSize(true);

    // use a linear layout manager
    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
    recyclerView.setLayoutManager(layoutManager);

    // set the adapter
    deviceAdapter = new DeviceAdapter(devices, this);
    recyclerView.setAdapter(deviceAdapter);

    pingStatus(false);

    // The Floating action button to launch a dialog where new device can be created
    binding.fab.setOnClickListener(
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
  public void onPreExecute(NetworkTask task) {
    tasks.add(task);
  }

  @Override
  public void onFinish(NetworkTask task, Response response, int deviceNum) {
    tasks.remove(task);
    DeviceData device = devices.get(deviceNum).getData();

    if (response.getException() != null) {
      Log.d("Network error", response.getException().toString());
      device.setStatus("UNKNOWN");
    } else {
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
      if (statusString.equals("ON")) {
        device.setStatus("ON");
      } else {
        device.setStatus("OFF");
      }
      Log.d("Response", response.getResponse());
    }
    deviceAdapter.notifyItemChanged(deviceNum);
  }

  @Override
  public void onCancel(NetworkTask task) {
    tasks.remove(task);
  }

  @Override
  public void onClick(View v) {
    if (!layoutUtils.hasErrors()) {
      cancelTasks();
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
      deviceStorageUtils.storeDevices(devices);
      pingStatus(true);
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == RESULT_OK) {
      if (data.getBooleanExtra(EXTRA_DEV_CHANGED, false)) {
        cancelTasks();
        ArrayList<SmartDevice> oldList = new ArrayList<>(devices);
        ArrayList<SmartDevice> newList = deviceStorageUtils.getDevices();
        DiffUtilCallback diffUtilCallback = new DiffUtilCallback(oldList, newList);
        DiffResult diff = DiffUtil.calculateDiff(diffUtilCallback);
        devices.clear();
        devices.addAll(newList);
        diff.dispatchUpdatesTo(deviceAdapter);
        pingStatus(false);
      }
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

  /**
   * Cancel all outstanding tasks, ending them if they are already running or stopping them before
   * they even begin.
   */
  public void cancelTasks() {
    for (NetworkTask task : tasks) {
      task.cancel(true);
    }
  }

  /**
   * Ping devices for their status, when using the last boolean only the last added device will be
   * pinged. This is to provide the functionality for newly added devices.
   *
   * @param last whether to only ping the last (new) device or not.
   */
  public void pingStatus(boolean last) {
    int start = 0;
    if (last) {
      start = devices.size() - 1;
    }
    for (int i = start; i < devices.size(); i++) {
      NetworkTask task = new NetworkTask((NetworkCallback) context, i);
      SmartDevice device = devices.get(i);
      task.execute(device.getCommand(device.getPowerStatus()));
    }
  }
}
