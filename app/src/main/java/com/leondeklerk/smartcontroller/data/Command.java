package com.leondeklerk.smartcontroller.data;

/**
 * A class that represents an new MQTT command. Contains a topic and message. The topic will be used
 * to publish the message on.
 */
public class Command {
  private String topic;

  private String message;

  /**
   * Class constructor.
   *
   * @param topic   the topic that the message will be published on.
   * @param message the message that will be published.
   */
  public Command(String topic, String message) {
    this.topic = topic;
    this.message = message;
  }

  public String getTopic() {
    return topic;
  }

  public void setTopic(String topic) {
    this.topic = topic;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
