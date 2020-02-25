package com.leondeklerk.smartcontroller.data;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

public class DeviceData {

  @Getter
  private int id;
  @Getter
  private String ip;
  @Getter
  private String name;
  @Getter
  private boolean isProtected;

  @Getter
  @Setter
  @Accessors(chain = true)
  private String password; // TODO: Encrypt

  @Getter
  @Setter
  @Accessors(chain = true)
  private String username;

  public DeviceData(int id, String ip, String name, boolean isProtected) {
    this.id = id;
    this.ip = ip;
    this.name = name;
    this.isProtected = isProtected;
  }
}
