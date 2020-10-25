package com.leondeklerk.smartcontroller.data;

import com.leondeklerk.smartcontroller.devices.SmartDevice;
import lombok.Getter;

/**
 * A class that represents and entry in the device storage data structure. Contains a id and a
 * device reference. The id indicates the entry of the device in the list, while device is the
 * actual device that is stored.
 */
public class Entry {
  @Getter private int id;
  @Getter private SmartDevice device;

  /**
   * Default constructor
   *
   * @param id the id of the device in the list (MainActivity RecyclerView)
   * @param device the device itself.
   */
  public Entry(int id, SmartDevice device) {
    this.id = id;
    this.device = device;
  }
}
