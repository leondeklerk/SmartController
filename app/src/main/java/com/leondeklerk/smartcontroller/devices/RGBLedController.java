package com.leondeklerk.smartcontroller.devices;

import android.annotation.SuppressLint;
import com.leondeklerk.smartcontroller.data.DeviceData;

public class RGBLedController extends SmartDevice {
  public RGBLedController(DeviceData data) {
    super(data);
  }

  @SuppressLint("DefaultLocale")
  public String brightness(int brightness) {
    return String.format("dimmer %d", brightness);
  }

  @SuppressLint("DefaultLocale")
  public String color(int r, int g, int b) {
    return String.format("color %d,%d,%d", r, g, b);
  }
}
