package com.leondeklerk.smartcontroller.data;

import lombok.Getter;
import lombok.Setter;

public class Command {
  @Getter @Setter private String topic;

  @Getter @Setter private String message;

  public Command(String topic, String message) {
    this.topic = topic;
    this.message = message;
  }
}
