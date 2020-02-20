package com.leondeklerk.smartcontroller.devices;

import com.leondeklerk.smartcontroller.data.DeviceData;

public class SmartDevice {
  private DeviceData data;

  public SmartDevice(DeviceData data) {
    this.data = data;
  }

  public String getCommand(String command) {
    return String.format("http://%s/cm?&user=%s&password=%s&cmnd=%s",
        data.getIp(),
        data.getUsername(),
        data.getPassword(),
        command);
  }

  public String getPowerStatus() {
    return "power";
  }

  public String turnOn(boolean on) {
    return on ? "power on" : "power off";
  }
}
