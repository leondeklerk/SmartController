package com.leondeklerk.smartcontroller;

public interface MqttConnectCallback {
  void onConnection(boolean connected);
}
