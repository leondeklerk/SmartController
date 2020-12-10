package com.leondeklerk.smartcontroller;

import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * And interface to accomodate for all the different types of connections that need to be properly
 * handled. Contains methods for the MQTT callbacks and a callback for the NetworkHandler class.
 */
public interface ConnectionsHandler {

  /**
   * Callback for when a new MqttMessage arrives on topic "stat/+/RESULT". From here on the message
   * can be parsed and handler accordingly.
   *
   * @param topic the topic that the message was received on.
   * @param message the actual message that was received.
   */
  void onMqttMessage(String topic, MqttMessage message);

  /**
   * Callback when the MqttClient is properly subscribed to the topic. Is used to notify the
   * activity that the client is now fully setup and ready.
   */
  void onMqttSubscribe();

  /**
   * Callback used when the client is connected to the server. No subscription has yet been
   * established. Is used to handle the subscribing and other parameters that need to be set after a
   * connection is made.
   *
   * @param connected indicates if the client is connected or not.
   */
  void onMqttConnected(boolean connected);

  /**
   * Callback for when the device network changes. Is used to properly handle status updates when
   * for example WiFi reconnects.
   */
  void onNetworkChange();
}
