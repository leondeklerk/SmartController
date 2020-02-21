package com.leondeklerk.smartcontroller.data;

import lombok.Getter;

public class DeviceData {

  @Getter
  private int id;
  @Getter
  private String ip;
  @Getter
  private String name;
  @Getter
  private String password;
  @Getter
  private String username;

  public DeviceData(int id, String ip, String name, String password, String username) {
    this.id = id;
    this.ip = ip;
    this.name = name;
    this.password = password; // TODO: Encrypt password
    this.username = username;
  }
}
