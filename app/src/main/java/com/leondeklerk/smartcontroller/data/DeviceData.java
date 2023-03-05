package com.leondeklerk.smartcontroller.data;

import androidx.databinding.BaseObservable;

import com.leondeklerk.smartcontroller.BR;

import java.util.Objects;

/**
 * A class that represents all the data related to a device. Each device contains a set of data,
 * which is shared between all SmartDevices. This class extends BaseObservable to accommodate for
 * databinding with the UI.
 */
public class DeviceData extends BaseObservable {

  private final int id;
  private String name;
  private String status;
  private boolean enabled;
  private final String type;
  private String topic;

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

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
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

  public String getTopic() {
    return topic;
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

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getType() {
    return type;
  }
}
