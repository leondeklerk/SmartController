package com.leondeklerk.smartcontroller.data;

import androidx.databinding.BaseObservable;
import androidx.databinding.library.baseAdapters.BR;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

/**
 * A class that represents all the data related to a device. Each device contains a set of data,
 * which is shared between all SmartDevices. This class extends BaseObservable to accommodate for
 * databinding with the UI.
 */
public class DeviceData extends BaseObservable {

  @Getter private int id;
  @Getter private String name;
  @Getter @Setter private String status;
  @Getter @Setter private boolean enabled;
  @Getter private String type;
  @Getter private String topic;

  /**
   * Default constructor
   *
   * @param id the id of this device
   * @param name the name of the device
   * @param status the status of the device
   * @param enabled indicates if the device is enabled or not
   * @param type the type of the device
   * @param topic the topic this device will listen to
   */
  public DeviceData(
      int id, String name, String status, boolean enabled, String type, String topic) {
    this.id = id;
    this.name = name;
    this.status = status;
    this.enabled = enabled;
    this.type = type;
    this.topic = topic;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DeviceData that = (DeviceData) o;
    return id == that.id
        && enabled == that.enabled
        && Objects.equals(topic, that.topic)
        && Objects.equals(name, that.name)
        && Objects.equals(status, that.status);
  }

  /**
   * Set the name of the device
   *
   * @param name the new name of the device
   * @return this
   */
  public DeviceData setName(String name) {
    this.name = name;
    notifyPropertyChanged(BR._all);
    return this;
  }

  /**
   * Set the topic of this device.
   *
   * @param topic the new topic.
   * @return this instance.
   */
  public DeviceData setTopic(String topic) {
    this.topic = topic;
    notifyPropertyChanged(BR._all);
    return this;
  }
}
