package com.leondeklerk.smartcontroller;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.util.Log;
import androidx.preference.PreferenceManager;
import com.leondeklerk.smartcontroller.data.Command;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import lombok.Getter;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MqttClient implements MqttCallback {

  private static MqttClient INSTANCE;

  private MqttAndroidClient client;

  String serverUri;
  final String subscriptionTopic = "stat/+/RESULT";

  private final Map<String, ConnectionsHandler> registeredHandlers;
  @Getter
  private ConnectionsHandler currentHandler;
  private final SharedPreferences preferences;

  private MqttClient(Context context) {
    preferences = PreferenceManager.getDefaultSharedPreferences(context);
    //TODO add setting
    serverUri =
        String.format(
            "ssl://%s:%s",
            preferences.getString("mqtt_ip", "localhost"),
            Integer.parseInt(preferences.getString("mqtt_port", "8883")));
    client =
        new MqttAndroidClient(
            context, serverUri, org.eclipse.paho.client.mqttv3.MqttClient.generateClientId());
    registeredHandlers = new HashMap<>();

    currentHandler = (ConnectionsHandler) context;

    connect(context);
  }

  private void connect(Context context) {
    MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
    mqttConnectOptions.setAutomaticReconnect(false);
    mqttConnectOptions.setCleanSession(false);
    mqttConnectOptions.setUserName(preferences.getString("mqtt_username", "admin"));
    mqttConnectOptions.setPassword(preferences.getString("mqtt_password", "admin").toCharArray());

    try {
      CertificateFactory cf = CertificateFactory.getInstance("X.509");
      InputStream caInput = new BufferedInputStream(context.getAssets().open("minica.pem"));
      Certificate ca;
      try {
        ca = cf.generateCertificate(caInput);
      } finally {
        caInput.close();
      }

      // Create a KeyStore containing our trusted CAs
      String keyStoreType = KeyStore.getDefaultType();
      KeyStore keyStore = KeyStore.getInstance(keyStoreType);
      keyStore.load(null, null);
      keyStore.setCertificateEntry("ca", ca);

      // Create a TrustManager that trusts the CAs in our KeyStore
      String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
      TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
      tmf.init(keyStore);

      // Create an SSLContext that uses our TrustManager
      SSLContext sslContext = SSLContext.getInstance("TLS");
      sslContext.init(null, tmf.getTrustManagers(), null);
      mqttConnectOptions.setSocketFactory(sslContext.getSocketFactory());

      client.connect(
          mqttConnectOptions,
          null,
          new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
              Log.d("MqttClient@connect#onSuccess", "Connected to: " + serverUri);
              DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
              disconnectedBufferOptions.setBufferEnabled(false);
              disconnectedBufferOptions.setBufferSize(100);
              disconnectedBufferOptions.setPersistBuffer(false);
              disconnectedBufferOptions.setDeleteOldestMessages(false);
              client.setBufferOpts(disconnectedBufferOptions);
              currentHandler.onMqttConnected(true);
              subscribeToTopic();
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
              Log.d("MqttClient@connect#onFailure",
                  "Failed to connect to: " + serverUri + exception.toString(), exception);
              currentHandler.onMqttConnected(false);
            }
          });

    } catch (Exception ex) {
      Log.d("MqttClient@connect#catch", "Error while connecting", ex);
    }
  }

  public void setHandler(String key) {
    ConnectionsHandler handler = registeredHandlers.get(key);
    if (handler != null) {
      Log.d("MqttClient@setHandler#notNull", key);
      currentHandler = handler;
    }
  }

  public void setCallback() {
    client.setCallback(this);
  }

  private void subscribeToTopic() {
    try {
      client.subscribe(
          subscriptionTopic,
          0,
          null,
          new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
              Log.d("MqttClient@subscribeToTopic#onSuccess", "Subscribed!");
              setCallback();
              currentHandler.onMqttSubscribe();
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
              Log.d("MqttClient@subscribeToTopic#onFailure", "Subscribed fail", exception);
            }
          });

    } catch (MqttException ex) {
      Log.d("MqttClient@subscribeToTopic#catch", "Error while subscribing", ex);
    }
  }

  public void destroy() {
    try {
      if (client != null) {
        if (client.isConnected()) {
          client.disconnect();
        }
      }
      Log.d("MqttClient@destroy#try", "Client destroyed");
    } catch (MqttException e) {
      Log.d("MqttClient@destroy#catch", "Error while destroying", e);
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
      Log.d("MqttClient@publish#try", command.getMessage());
    } catch (MqttException e) {
      Log.d("MqttClient@publish#catch", "Error while publishing", e);
    }
  }

  /**
   * Register a new ConnectionsHandler that the client can be switched to.
   *
   * @param key        the key of the handler to identify it.
   * @param newHandler the new handler that needs to be registered.
   */
  public void registerHandler(String key, ConnectionsHandler newHandler) {
    Log.d("MqttClient@registerHandler", key);
    registeredHandlers.put(key, newHandler);
  }

  /**
   * Get a (new) instance of the client.
   *
   * @param context the context that needs to be registered.
   * @return a (new) instance of the MqttClient
   */
  public static MqttClient getInstance(Context context) {
    if (INSTANCE == null) {
      Log.d("MqttClient@getInstance", "null");
      INSTANCE = new MqttClient(context);
    }
    return INSTANCE;
  }

  public static MqttClient reconnect(Context context) {
    Log.d("MqttClient@reconnect", "Reconnecting");
    INSTANCE.destroy();
    INSTANCE = null;
    return getInstance(context);
  }

  @Override
  public void connectionLost(Throwable cause) {
    Log.d("MqttClient@connectionLost", "Connection lost", cause);
  }

  @Override
  public void messageArrived(String topic, MqttMessage message) {
    Log.d("MqttClient@messageArrived", message.toString());
    currentHandler.onMqttMessage(topic, message);
  }

  @Override
  public void deliveryComplete(IMqttDeliveryToken token) {
    Log.d("MqttClient@deliveryComplete", "Delivered");
  }

  public boolean isConnected() {
    return client.isConnected();
  }
}
