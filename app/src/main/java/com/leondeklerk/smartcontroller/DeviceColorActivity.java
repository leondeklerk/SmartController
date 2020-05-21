package com.leondeklerk.smartcontroller;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.databinding.adapters.RadioGroupBindingAdapter;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.leondeklerk.smartcontroller.databinding.ActivityDeviceColorBinding;
import com.leondeklerk.smartcontroller.databinding.ActivityDeviceEditBinding;
import com.leondeklerk.smartcontroller.devices.RGBLedController;
import com.leondeklerk.smartcontroller.devices.SmartDevice;
import com.leondeklerk.smartcontroller.utils.DeviceStorageUtils;
import java.util.ArrayList;
import org.jetbrains.annotations.NotNull;

public class DeviceColorActivity extends FragmentActivity implements View.OnClickListener {

  public static final String EXTRA_SELECTED_DEV = "com.leondeklerk.smartcontroller.SELECTED_DEV";
  private ActivityDeviceColorBinding binding;
  private RGBLedController device;

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

    SharedPreferences preferences =
        this.getSharedPreferences(getString(R.string.dev_prefs), Context.MODE_PRIVATE);
    DeviceStorageUtils deviceStorageUtils = new DeviceStorageUtils(preferences);

    ArrayList<SmartDevice> devices = deviceStorageUtils.getDevices();
    device = new RGBLedController(devices.get(deviceNum).getData());
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
        CommandTask task = new CommandTask();
        task.executeOnExecutor(
            AsyncTask.THREAD_POOL_EXECUTOR, device.getCommand(device.color(red, green, blue)));
        this.onBackPressed();
        break;
      default:
        Log.d("Clicked", "Non-existent button clicked (color)");
    }
  }
}
