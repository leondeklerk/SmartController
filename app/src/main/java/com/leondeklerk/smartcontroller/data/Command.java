package com.leondeklerk.smartcontroller.data;

import lombok.Getter;
import lombok.Setter;

/**
 * A class that represents an new MQTT command. Contains a topic and message. The topic will be used
 * to publish the message on.
 */
public class Command {
  @Getter @Setter private String topic;

  @Getter @Setter private String message;

  /**
   * Class constructor.
   *
   * @param topic the topic that the message will be published on.
   * @param message the message that will be published.
   */
  public Command(String topic, String message) {
    this.topic = topic;
    this.message = message;
  }
}
