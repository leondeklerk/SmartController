package com.leondeklerk.smartcontroller;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputFilter;
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
import com.leondeklerk.smartcontroller.utils.IpInputFilter;
import com.leondeklerk.smartcontroller.utils.TextInputLayoutUtils;
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
  private TextInputLayoutUtils fragUtils;
  private TextInputLayoutUtils pwdUtils;
  private AlertDialog dialog;

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
    deviceStorageUtils = new DeviceStorageUtils(preferences);

    devices = deviceStorageUtils.getDevices();
    device = devices.get(devNum);
    initial = SmartDevice.clone(device);

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
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(this);
        setUpPwdDialog();
        break;
      case android.R.id.button1:
        if (pwdUtils.hasErrors()) return;

        pwdBinding.changePwdOld.setErrorEnabled(false);

        if (pwdUtils.checkEqual(pwdBinding.changePwdNew, pwdBinding.changePwdNew2)) {
          device.getData().setPassword(TextInputLayoutUtils.getText(pwdBinding.changePwdNew));
          dialog.dismiss();
        }
        break;
      case R.id.edit_update:
        if (!fragUtils.hasErrors()) {
          // TODO: Passwords need more work only changing name should be possible.
          if (!binding.switchCredentials.isChecked()
              || (pwdUtils != null && !pwdUtils.hasErrors())) {
            updateDevice();
            context.onBackPressed();
          } else {
            binding.editPassword.performClick();
            //            Snackbar.make(
            //                binding.getRoot(),
            //                getString(R.string.edit_password_required),
            //                Snackbar.LENGTH_LONG)
            //                .show();
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

  /**
   * Setup the different properties for the password dialog. This mainly consists of setting up the
   * correct error messages for the layouts.
   */
  private void setUpPwdDialog() {
    pwdBinding.changePwdOld.requestFocus();

    ArrayList<TextInputLayout> layouts = new ArrayList<>();
    layouts.add(pwdBinding.changePwdNew);
    layouts.add(pwdBinding.changePwdNew2);

    pwdUtils = new TextInputLayoutUtils(layouts, context);

    if (device.getData().getPassword() != null) {
      pwdUtils.addLayout(pwdBinding.changePwdOld);
      pwdUtils.setMatchPwd(device.getData().getPassword());
    } else {
      pwdBinding.changePwdOld.setEnabled(false);
    }

    pwdUtils.setErrorListeners();
  }

  /** Set up the TextInputLayouts in the fragment, adding their error listeners. */
  private void setUpUtilsFrag(boolean checked) {
    ArrayList<TextInputLayout> layouts = new ArrayList<>();
    // Add all input layouts to a list
    layouts.add(binding.editName);
    layouts.add(binding.editIp);

    // Create new utils to check for errors
    fragUtils = new TextInputLayoutUtils(layouts, context);

    //noinspection ConstantConditions
    binding.editIp.getEditText().setFilters(new InputFilter[] {new IpInputFilter()});

    // If credentials are enabled, add them
    if (checked) {
      fragUtils.addLayout(binding.editUsername);
    } else {
      device.getData().setPassword(null).setUsername(null);
    }
    fragUtils.setErrorListeners();
  }

  /**
   * Set the result intent of the parent activity, this will be checked upon reentry of the
   * MainActivity.
   *
   * @param removed if the device was removed
   */
  private void setResult(boolean removed) {
    Intent resultIntent = new Intent();
    if (removed) {
      resultIntent.putExtra(MainActivity.EXTRA_DEV_REMOVED, devNum);
    } else {
      if (!initial.equals(device)) {
        resultIntent.putExtra(MainActivity.EXTRA_DEV_CHANGED, devNum);
      }
    }
    context.setResult(Activity.RESULT_OK, resultIntent);
  }

  /** Update the values of the current device. */
  private void updateDevice() {
    // TODO change back to two-way bindings after changing error listeners
    device
        .getData()
        .setName(TextInputLayoutUtils.getText(binding.editName))
        .setIp(TextInputLayoutUtils.getText(binding.editIp))
        .setProtected(binding.switchCredentials.isChecked())
        .setUsername(TextInputLayoutUtils.getText(binding.editUsername));
    setResult(false);
    deviceStorageUtils.storeDevices(devices);
  }
}
