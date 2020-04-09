package com.leondeklerk.smartcontroller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.leondeklerk.smartcontroller.databinding.FragmentDeviceEditBinding;
import com.leondeklerk.smartcontroller.devices.SmartDevice;
import com.leondeklerk.smartcontroller.utils.DeviceStorageUtils;
import java.util.ArrayList;
import org.jetbrains.annotations.NotNull;

public class DeviceEditFragment extends Fragment implements View.OnClickListener {

  static final String ARG_FRAG_NUM = "com.leondeklerk.smartcontroller.FRAG_NUM";
  private Activity context;
  private int devNum;
  private ArrayList<SmartDevice> devices;
  private DeviceStorageUtils deviceStorageUtils;
  private FragmentDeviceEditBinding binding;

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

    binding.editDelete.setOnClickListener(this);
    binding.editUpdate.setOnClickListener(this);
  }

  @Override
  public void onResume() {
    super.onResume();
    DeviceEditActivity.binding.toolbar.setTitle(devices.get(devNum).getData().getName());
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }

  @Override
  public void onClick(View v) {
    if (v.getId() == R.id.edit_delete) {
      devices.remove(devNum);
      deviceStorageUtils.storeDevices(devices);
      Intent resultIntent = new Intent();
      resultIntent.putExtra(MainActivity.EXTRA_DEV_CHANGED, true);
      context.setResult(Activity.RESULT_OK, resultIntent);
      context.onBackPressed();
    } else {
      Toast.makeText(context, "Unknown device clicked", Toast.LENGTH_SHORT).show();
    }
  }
}
