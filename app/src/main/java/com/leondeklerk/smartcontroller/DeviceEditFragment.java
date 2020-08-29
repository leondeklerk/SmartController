package com.leondeklerk.smartcontroller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.textfield.TextInputLayout;
import com.leondeklerk.smartcontroller.databinding.FragmentDeviceEditBinding;
import com.leondeklerk.smartcontroller.devices.SmartDevice;
import com.leondeklerk.smartcontroller.utils.DeviceStorageUtils;
import com.leondeklerk.smartcontroller.utils.TextInputUtils;
import java.util.ArrayList;
import org.jetbrains.annotations.NotNull;

/**
 * A fragment that represents the actual device edit screen withing the carousel of edit screens.
 * Contains all the data of a device and the options to alter this data.
 */
public class DeviceEditFragment extends Fragment implements View.OnClickListener {

  static final String ARG_FRAG_NUM = "com.leondeklerk.smartcontroller.FRAG_NUM";
  private Activity context;
  private int devNum;
  private ArrayList<SmartDevice> devices;
  private DeviceStorageUtils deviceStorageUtils;
  private FragmentDeviceEditBinding binding;
  private SmartDevice device;
  private SmartDevice initial;
  private ArrayList<TextInputLayout> fragList;

  @Override
  public View onCreateView(
      @NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding = FragmentDeviceEditBinding.inflate(inflater, container, false);
    context = getActivity();
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
    Bundle args = getArguments();
    if (args != null) {
      devNum = args.getInt(ARG_FRAG_NUM);
    } else {
      context.finish();
    }
    SharedPreferences preferences =
        context.getSharedPreferences(getString(R.string.dev_prefs), Context.MODE_PRIVATE);
    deviceStorageUtils = new DeviceStorageUtils(preferences, context);

    devices = deviceStorageUtils.getDevices();
    device = devices.get(devNum);

    // Set up current device references to refer to
    initial = SmartDevice.clone(device);

    // Bind the data class
    binding.setDevice(device);
    binding.executePendingBindings();

    // Set button listeners
    binding.editDelete.setOnClickListener(this);
    binding.editUpdate.setOnClickListener(this);

    setUpUtilsFrag();
  }

  @Override
  public void onResume() {
    super.onResume();
    // Change the title of the Activity
    DeviceEditActivity.binding.toolbar.setTitle(device.getData().getName());
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.edit_delete:
        // Remove the device and store this
        devices.remove(devNum);
        deviceStorageUtils.storeDevices(devices);

        setResult(true);

        // Return
        context.onBackPressed();
        break;
      case R.id.edit_update:
        if (!TextInputUtils.hasErrors(fragList)) {

          // Update the device and return
          updateDevice();
          context.onBackPressed();

        } else {
          setResult(false);
        }
        break;
      default:
        Log.d("Unknown button", String.valueOf(v.getId()));
    }
  }

  /** Set up the input fields in the fragment, adding their error listeners. */
  private void setUpUtilsFrag() {
    fragList = new ArrayList<>();

    // Add all input layouts to a list
    fragList.add(binding.editName);
    fragList.add(binding.editTopic);

    // Set the error listeners
    TextInputUtils.setListener(binding.editName, TextInputUtils.DEFAULT_TYPE);
    TextInputUtils.setListener(binding.editTopic, TextInputUtils.DEFAULT_TYPE);
  }

  /**
   * Set the result intent of the parent activity, this will be checked upon reentry of the
   * MainActivity.
   *
   * @param removed true if the device was removed, false if not
   */
  private void setResult(boolean removed) {
    // Create a new intent
    Intent resultIntent = new Intent();
    if (removed) {
      // If the device was removed, flag this
      resultIntent.putExtra(MainActivity.EXTRA_DEV_REMOVED, devNum);
    } else {
      if (!initial.equals(device)) {
        // If the device was edited, flag this
        resultIntent.putExtra(MainActivity.EXTRA_DEV_CHANGED, devNum);
      }
    }
    context.setResult(Activity.RESULT_OK, resultIntent);
  }

  /** Update and store the current device. */
  private void updateDevice() {
    // Update the device data
    device
        .getData()
        .setName(TextInputUtils.getText(binding.editName))
        .setTopic(TextInputUtils.getText(binding.editTopic));
    setResult(false);

    // Store the new device data
    deviceStorageUtils.storeDevices(devices);
  }
}
