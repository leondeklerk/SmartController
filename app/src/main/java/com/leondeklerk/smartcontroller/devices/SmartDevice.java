package com.leondeklerk.smartcontroller.devices;

import com.leondeklerk.smartcontroller.data.Command;
import com.leondeklerk.smartcontroller.data.DeviceData;

/**
 * A SmartDevice is the base class for all the devices supported. This includes encapsulating the
 * basic data, like id, name, IP and optional credentials. This class also provides some basic
 * commands like checking the power status, turning it on or off. Other devices can be extended from
 * this one to provide additional functionality like color control for LED.
 */
public class SmartDevice {

  private final DeviceData data;

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
   * Get a topic to publish to based on the data of the device and the type of command.
   *
   * @param command the command that the device needs to execute.
   * @return the topic formatted with the device data topic.
   */
  String getTopic(String command) {
    return "cmnd/" + data.getTopic() + "/" + command;
  }

  /**
   * Get the command for checking the status.
   *
   * @return the power status Command
   */
  public Command getPowerStatus() {
    return new Command(getTopic("POWER"), "?");
  }

  /**
   * Get the command to turn the power on or off.
   *
   * @param on bool whether or not the power the device on or off
   * @return the command to turn the device on or off.
   */
  public Command setPower(boolean on) {
    return new Command(getTopic("POWER"), on ? "ON" : "OFF");
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
                    otherData.getStatus(),
                    otherData.isEnabled(),
                    otherData.getType(),
                    otherData.getTopic()));
  }

  public DeviceData getData() {
    return data;
  }
}
