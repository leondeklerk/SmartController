package com.leondeklerk.smartcontroller.data;

import android.util.Log;
import androidx.databinding.BaseObservable;
import androidx.databinding.library.baseAdapters.BR;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

public class DeviceData extends BaseObservable {

  @Getter private int id;
  @Getter private String name;
  @Getter private boolean isProtected;
  @Getter private String status;
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

  public DeviceData setProtected(boolean isProtected) {
    this.isProtected = isProtected;
    notifyPropertyChanged(BR._all);
    return this;
  }

  public DeviceData setName(String name) {
    this.name = name;
    notifyPropertyChanged(BR._all);
    return this;
  }

  public DeviceData setUsername(String username) {
    this.username = username;
    notifyPropertyChanged(BR._all);
    return this;
  }

  public DeviceData setTopic(String topic) {
    this.topic = topic;
    notifyPropertyChanged(BR._all);
    return this;
  }

  public void setStatus(String status) {
    this.status = status;
    notifyPropertyChanged(BR._all);
    Log.d("mqtt", status);
  }
}
