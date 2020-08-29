package com.leondeklerk.smartcontroller.data;

import androidx.databinding.BaseObservable;
import androidx.databinding.library.baseAdapters.BR;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * A class that represents all the data related to a device. Each device contains a set of data,
 * which is shared between all SmartDevices. This class extends BaseObservable to accommodate for
 * databinding with the UI.
 */
public class DeviceData extends BaseObservable {

  @Getter private int id;
  @Getter private String name;
  @Getter private boolean isProtected;
  @Getter @Setter private String status;
  @Getter @Setter private boolean enabled;
  @Getter private String type;
  @Getter private String topic;

  @Getter
  @Setter
  @Accessors(chain = true)
  private String password; // TODO: Encrypt

  @Getter
  @Accessors(chain = true)
  private String username;

  /**
   * Default constructor
   *
   * @param id the id of this device
   * @param name the name of the device
   * @param isProtected indicated if the device is password protected
   * @param status the status of the device
   * @param enabled indicates if the device is enabled or not
   * @param type the type of the device
   * @param topic the topic this device will listen to
   */
  public DeviceData(
      int id,
      String name,
      boolean isProtected,
      String status,
      boolean enabled,
      String type,
      String topic) {
    this.id = id;
    this.name = name;
    this.isProtected = isProtected;
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
        && isProtected == that.isProtected
        && enabled == that.enabled
        && Objects.equals(topic, that.topic)
        && Objects.equals(name, that.name)
        && Objects.equals(status, that.status)
        && Objects.equals(password, that.password)
        && Objects.equals(username, that.username);
  }

  /**
   * Setter for the protected field.
   *
   * @param isProtected if the device is credentials protected or not.
   * @return this data
   */
  public DeviceData setProtected(boolean isProtected) {
    this.isProtected = isProtected;
    notifyPropertyChanged(BR._all);
    return this;
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
   * Set the username of the device
   *
   * @param username the new username
   * @return this instance
   */
  public DeviceData setUsername(String username) {
    this.username = username;
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
