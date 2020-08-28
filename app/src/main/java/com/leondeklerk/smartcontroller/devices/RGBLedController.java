package com.leondeklerk.smartcontroller.devices;

import android.annotation.SuppressLint;
import com.leondeklerk.smartcontroller.data.Command;
import com.leondeklerk.smartcontroller.data.DeviceData;

public class RGBLedController extends SmartDevice {
  public RGBLedController(DeviceData data) {
    super(data);
  }

  /**
   * Get the command for checking the status.
   *
   * @return the power status Command
   */
  public Command getColor() {
    return new Command(super.getTopic("Color"), "?");
  }

  @SuppressLint("DefaultLocale")
  public Command setColor(int red, int green, int blue) {
    return new Command(super.getTopic("Color2"), String.format("%d,%d,%d", red, green, blue));
  }
}
