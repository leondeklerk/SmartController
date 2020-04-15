package com.leondeklerk.smartcontroller.data;

import androidx.databinding.BaseObservable;
import androidx.databinding.library.baseAdapters.BR;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

public class DeviceData extends BaseObservable {

  @Getter private int id;
  @Getter private String ip;
  @Getter private String name;
  @Getter private boolean isProtected;
  @Getter @Setter private String status;
  @Getter @Setter private boolean enabled;

  @Getter
  @Setter
  @Accessors(chain = true)
  private String password; // TODO: Encrypt

  @Getter
  @Accessors(chain = true)
  private String username;

  public DeviceData(
      int id, String name, String ip, boolean isProtected, String status, boolean enabled) {
    this.id = id;
    this.name = name;
    this.ip = ip;
    this.isProtected = isProtected;
    this.status = status;
    this.enabled = enabled;
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
    return id == that.id &&
        isProtected == that.isProtected &&
        enabled == that.enabled &&
        Objects.equals(ip, that.ip) &&
        Objects.equals(name, that.name) &&
        Objects.equals(status, that.status) &&
        Objects.equals(password, that.password) &&
        Objects.equals(username, that.username);
  }

  public DeviceData setProtected(boolean isProtected) {
    this.isProtected = isProtected;
    notifyPropertyChanged(BR._all);
    return this;
  }

  public DeviceData setIp(String ip) {
    this.ip = ip;
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
}
