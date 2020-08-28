package com.leondeklerk.smartcontroller;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.leondeklerk.smartcontroller.databinding.FragmentDeviceEditBinding;
import com.leondeklerk.smartcontroller.databinding.PasswordDialogBinding;
import com.leondeklerk.smartcontroller.devices.SmartDevice;
import com.leondeklerk.smartcontroller.utils.DeviceStorageUtils;
import com.leondeklerk.smartcontroller.utils.TextInputUtils;
import java.util.ArrayList;
import org.jetbrains.annotations.NotNull;

public class DeviceEditFragment extends Fragment implements View.OnClickListener {

  static final String ARG_FRAG_NUM = "com.leondeklerk.smartcontroller.FRAG_NUM";
  private Activity context;
  private int devNum;
  private ArrayList<SmartDevice> devices;
  private DeviceStorageUtils deviceStorageUtils;
  private FragmentDeviceEditBinding binding;
  private SmartDevice device;
  private SmartDevice initial;
  private PasswordDialogBinding pwdBinding;
  private ArrayList<TextInputLayout> fragList, pwdList;
  private AlertDialog dialog;
  private String referencePwd;
  private boolean pwdErrors = true;

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
    referencePwd = device.getData().getPassword();

    // Bind the data class
    binding.setDevice(device);
    binding.executePendingBindings();

    // Set button listeners
    binding.editDelete.setOnClickListener(this);
    binding.editUpdate.setOnClickListener(this);
    binding.editPassword.setOnClickListener(this);

    setUpUtilsFrag(true);

    // Change the listeners based whether or not the switch is enabled
    binding.switchCredentials.setOnCheckedChangeListener(
        new OnCheckedChangeListener() {
          @Override
          public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (buttonView.isPressed()) {
              device.getData().setProtected(isChecked);
              setUpUtilsFrag(isChecked);
            }
          }
        });
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
      case R.id.edit_password:
        // Create a binding for the password dialog
        pwdBinding = PasswordDialogBinding.inflate(LayoutInflater.from(context));

        // Create a new dialog and set it up
        dialog = createPasswordDialog();
        dialog.show();
        setUpPwdDialog();
        break;
      case android.R.id.button1:
        // Check if the two new passwords match
        TextInputUtils.checkPwd(pwdBinding.changePwdOld, referencePwd);
        TextInputUtils.checkEqual(pwdBinding.changePwdNew, pwdBinding.changePwdNew2);

        if (TextInputUtils.hasErrors(pwdList)) return;

        device.getData().setPassword(TextInputUtils.getText(pwdBinding.changePwdNew));
        dialog.dismiss();
        pwdErrors = false;
        break;
      case android.R.id.button2:
        // Reset the pwdList since it is not used
        pwdList = null;
        dialog.dismiss();
        break;
      case R.id.edit_update:
        if (!TextInputUtils.hasErrors(fragList)) {
          // Check if all requirements regarding the passwords are met
          if (checkPwdRequirements()) {
            // Update the device and return
            updateDevice();
            context.onBackPressed();
          } else {
            // Upon up the password screen
            binding.editPassword.performClick();
          }
        } else {
          setResult(false);
        }
        break;
      default:
        Log.d("Unknown button", String.valueOf(v.getId()));
    }
  }

  /**
   * Create a dialog that is used to change the password of a device. This dialog is based on the
   * PasswordDialog layout.
   *
   * @return the dialog.
   */
  private AlertDialog createPasswordDialog() {
    return new MaterialAlertDialogBuilder(context, R.style.MaterialAlertDialog_FilledButtonDialog)
        .setTitle(R.string.update_pwd_title)
        .setView(pwdBinding.getRoot())
        .setPositiveButton(R.string.edit_update, null)
        .setNegativeButton(android.R.string.cancel, null)
        .create();
  }

  /** */
  private void setUpPwdDialog() {
    // Set the listeners of the buttons
    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(this);
    dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(this);

    // Add the layouts to the list
    pwdList = new ArrayList<>();
    pwdList.add(pwdBinding.changePwdOld);
    pwdList.add(pwdBinding.changePwdNew);
    pwdList.add(pwdBinding.changePwdNew2);

    // Set their error listeners
    TextInputUtils.setListener(pwdBinding.changePwdNew, TextInputUtils.DEFAULT_TYPE);
    TextInputUtils.setListener(pwdBinding.changePwdNew2, TextInputUtils.DEFAULT_TYPE);

    // If there already was a password
    if (referencePwd != null) {
      // Set up the field for the old password
      TextInputUtils.setListener(pwdBinding.changePwdOld, TextInputUtils.DEFAULT_TYPE);
      pwdBinding.changePwdOld.requestFocus();
    } else {
      // If there was no current password, disable it's input field
      pwdList.remove(pwdBinding.changePwdOld);
      pwdBinding.changePwdOld.setEnabled(false);
      pwdBinding.changePwdNew.requestFocus();
    }
  }

  /**
   * Set up the input fields in the fragment, adding their error listeners.
   *
   * @param hasCredentials whether or not credentials are enabled.
   */
  private void setUpUtilsFrag(boolean hasCredentials) {
    fragList = new ArrayList<>();

    // Add all input layouts to a list
    fragList.add(binding.editName);
    fragList.add(binding.editTopic);

    // Set the error listeners
    TextInputUtils.setListener(binding.editName, TextInputUtils.DEFAULT_TYPE);
    TextInputUtils.setListener(binding.editTopic, TextInputUtils.DEFAULT_TYPE);

    // If credentials are enabled, add them
    if (hasCredentials) {
      TextInputUtils.setListener(binding.editUsername, TextInputUtils.DEFAULT_TYPE);
      fragList.add(binding.editUsername);
    } else {
      device.getData().setPassword(null).setUsername(null);
    }
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
        .setTopic(TextInputUtils.getText(binding.editTopic))
        .setProtected(binding.switchCredentials.isChecked())
        .setUsername(TextInputUtils.getText(binding.editUsername));
    setResult(false);

    // Store the new device data
    deviceStorageUtils.storeDevices(devices);
  }

  /**
   * Checks if the requirements for credentials are met. There are three different cases relevant:
   * 1. There are no credentials enabled. 2. Only the username might have been updated, the password
   * not. 3. The password was updated.
   *
   * @return true if the requirements for each type of case are met correctly, false if not.
   */
  private boolean checkPwdRequirements() {
    // No credentials
    if (!binding.switchCredentials.isChecked()) {
      return true;
    } else if (pwdList == null && referencePwd != null) {
      // Only the username was updated
      return true;
    } else return !pwdErrors;
    // The password was updated, return if it had any errors or not.
  }
}
