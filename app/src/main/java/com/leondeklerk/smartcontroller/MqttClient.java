package com.leondeklerk.smartcontroller;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.preference.PreferenceManager;
import com.leondeklerk.smartcontroller.data.Command;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MqttClient {
  private static MqttClient INSTANCE;

  private MqttAndroidClient client;

  String serverUri;
  final String subscriptionTopic = "stat/+/RESULT";

  private Map<String, MqttCallback> registeredCallbacks;
  private SharedPreferences preferences;

  private MqttClient(Context context) {
    preferences = PreferenceManager.getDefaultSharedPreferences(context);
    serverUri =
        String.format(
            "tcp://%s:%s",
            preferences.getString("mqtt_ip", "localhost"),
            Integer.parseInt(preferences.getString("mqtt_port", "1883")));
    client =
        new MqttAndroidClient(
            context, serverUri, org.eclipse.paho.client.mqttv3.MqttClient.generateClientId());
    registeredCallbacks = new HashMap<>();

    connect(context);
  }

  private void connect(final Context context) {
    MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
    mqttConnectOptions.setAutomaticReconnect(false);
    mqttConnectOptions.setCleanSession(false);
    mqttConnectOptions.setUserName(preferences.getString("mqtt_username", "admin"));
    mqttConnectOptions.setPassword(preferences.getString("mqtt_password", "admin").toCharArray());

    try {

      client.connect(
          mqttConnectOptions,
          null,
          new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
              DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
              disconnectedBufferOptions.setBufferEnabled(false);
              disconnectedBufferOptions.setBufferSize(100);
              disconnectedBufferOptions.setPersistBuffer(false);
              disconnectedBufferOptions.setDeleteOldestMessages(false);
              client.setBufferOpts(disconnectedBufferOptions);
              ((MainActivity) context).onConnection(true);
              subscribeToTopic(context);
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
              Log.d("Mqtt", "Failed to connect to: " + serverUri + exception.toString());
              ((MainActivity) context).onConnection(false);
            }
          });

    } catch (MqttException ex) {
      Log.d("Mqtt", "Error while connecting");
    }
  }

  public void setCallback(String key) {
    MqttCallback callback = registeredCallbacks.get(key);
    if (callback != null) {
      client.setCallback(registeredCallbacks.get(key));
    }
  }

  private void subscribeToTopic(Context context) {
    Log.d("mqtt", "subscriping reached?");
    try {
      client.subscribe(subscriptionTopic, 0, null, (MainActivity) context);

    } catch (MqttException ex) {
      Log.d("Mqtt", "Error while subscribing");
    }
  }

  public void destroy() {
    try {
      if (client != null) {
        if (client.isConnected()) {
          client.disconnect();
        }
      }
      Log.d("Mqtt", "Client destroyed");
    } catch (MqttException e) {
      Log.d("Mqtt", "Error while destroying");
    }
  }

  /**
   * Publish a command to the MQTT broker
   *
   * @param command the command to publish containing the topic and value.
   */
  public void publish(Command command) {
    try {
      MqttMessage message = new MqttMessage();
      message.setPayload(command.getMessage().getBytes());
      client.publish(command.getTopic(), message);
    } catch (MqttException e) {
      Log.d("Mqtt", "Error while publishing");
    }
  }

  /**
   * Register a new callback the client can be switched to.
   *
   * @param key the key of the callback to identify it.
   * @param newCallback the new callback that needs to be registered.
   */
  public void registerCallback(String key, MqttCallback newCallback) {
    registeredCallbacks.put(key, newCallback);
  }

  /**
   * Get a (new) instance of the client.
   *
   * @param context the context that needs to be registered.
   * @return a (new) instance of the MqttClient
   */
  public static MqttClient getInstance(Context context) {
    if (INSTANCE == null) {
      INSTANCE = new MqttClient(context);
    }
    return INSTANCE;
  }

  public static MqttClient reconnect(Context context) {
    INSTANCE.destroy();
    INSTANCE = null;
    return getInstance(context);
  }
}
