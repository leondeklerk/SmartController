package com.leondeklerk.smartcontroller;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

/**
 * A class that handles changes in the network. If the device (re)connects to a (new) network, the
 * OnAvailable method will be used in combination with a ConnectionsHandler to act on state changes.
 */
public class NetworkHandler extends NetworkCallback {

  private static NetworkHandler INSTANCE;
  private static int count = 0;
  @Getter @Setter private ConnectionsHandler currentHandler;

  /** Private constructor. */
  private NetworkHandler() {}

  @Override
  public void onAvailable(@NotNull Network network) {
    // Make sure the first network change doesn't do anything (app startup)
    if (count > 1) {
      // Do what you need to do here
      if (currentHandler != null) {
        currentHandler.onNetworkChange();
      }
    }
    count++;
  }

  /**
   * Register the NetworkHandler for this application.
   *
   * @param context the context to retrieve the ConnectivityManager from.
   */
  public void register(Context context) {
    NetworkRequest request =
        new NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build();

    ConnectivityManager connectivityManager =
        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

    if (connectivityManager != null) {
      connectivityManager.registerNetworkCallback(request, this);
    }
  }

  /**
   * Unregister the handler for this application.
   *
   * @param context the context to retrieve the ConnectivityManager from.
   */
  public void unregister(Context context) {
    ConnectivityManager connectivityManager =
        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

    if (connectivityManager != null) {
      connectivityManager.unregisterNetworkCallback(this);
    }
  }

  /**
   * Get the instance of the NetworkHandler, will create a new instance if none already exists.
   *
   * @return the handler instance.
   */
  public static NetworkHandler getHandler() {
    if (INSTANCE == null) {
      INSTANCE = new NetworkHandler();
    }
    return INSTANCE;
  }
}
