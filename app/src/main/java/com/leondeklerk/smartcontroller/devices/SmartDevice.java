package com.leondeklerk.smartcontroller.devices;

import com.leondeklerk.smartcontroller.data.DeviceData;
import lombok.Getter;

/**
 * A SmartDevice is the base class for all the devices supported. This includes encapsulating the
 * basic data, like id, name, IP and optional credentials. This class also provides some basic
 * commands like checking the power status, turning it on or off. Other devices can be extended from
 * this one to provide additional functionality like color control for LED.
 */
public class SmartDevice {

  @Getter private DeviceData data;

  /**
   * Default constructor to create a new SmartDevice, based on some given device data.
   *
   * @param data the data for this device.
   */
  public SmartDevice(DeviceData data) {
    this.data = data;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    return this.getData().equals(((SmartDevice) o).getData());
  }

  /**
   * Get a command to execute based on the data of the device and the command.
   *
   * @param command the command that the device needs to execute.
   * @return the command formatted with the device data.
   */
  public String getCommand(String command) {
    if (data.isProtected()) {
      return String.format(
          "http://%s/cm?&user=%s&password=%s&cmnd=%s",
          data.getIp(), data.getUsername(), data.getPassword(), command);
    }
    return String.format("http://%s/cm?&cmnd=%s", data.getIp(), command);
  }

  /**
   * Get the command for checking the status.
   *
   * @return the power status String command
   */
  public String getPowerStatus() {
    return "power";
  }

  /**
   * Get the command to turn the power on or off.
   *
   * @param on bool whether or not the power the device on or off
   * @return the String command to turn the device on or off.
   */
  public String turnOn(boolean on) {
    return on ? "power on" : "power off";
  }

  /**
   * Clone a device and return an exact copy with another memory address.
   *
   * @param other the device to clone.
   * @return a cloned instance of the other device.
   */
  public static SmartDevice clone(SmartDevice other) {
    DeviceData otherData = other.getData();
    return new SmartDevice(
        new DeviceData(
                otherData.getId(),
                otherData.getName(),
                otherData.getIp(),
                otherData.isProtected(),
                otherData.getStatus(),
                otherData.isEnabled())
            .setPassword(otherData.getPassword())
            .setUsername(otherData.getUsername()));
  }
}
