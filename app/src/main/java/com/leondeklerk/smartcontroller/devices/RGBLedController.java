package com.leondeklerk.smartcontroller.devices;

import android.annotation.SuppressLint;
import com.leondeklerk.smartcontroller.data.Command;
import com.leondeklerk.smartcontroller.data.DeviceData;

/** A specific instance of a SmartDevice that indicates that this device is a LED Controller. */
public class RGBLedController extends SmartDevice {

  /**
   * Default constructor
   *
   * @param data the data that represents this device.
   */
  public RGBLedController(DeviceData data) {
    super(data);
  }

  /**
   * Get the command for checking the status.
   *
   * @return the color status Command
   */
  public Command getColor() {
    return new Command(super.getTopic("Color"), "?");
  }

  /**
   * Set the color of the device.
   *
   * @param red the value of red.
   * @param green the value of green.
   * @param blue the value of blue.
   * @return a new command that will be published on the MQTT client.
   */
  @SuppressLint("DefaultLocale")
  public Command setColor(int red, int green, int blue) {
    return new Command(super.getTopic("Color2"), String.format("%d,%d,%d", red, green, blue));
  }
}
