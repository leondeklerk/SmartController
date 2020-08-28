package com.leondeklerk.smartcontroller.data;

import com.leondeklerk.smartcontroller.devices.SmartDevice;
import lombok.Getter;

public class Entry {
  @Getter
  private int id;
  @Getter
  private SmartDevice device;

  public Entry(int id, SmartDevice device) {
    this.id = id;
    this.device = device;
  }

}
