package com.leondeklerk.smartcontroller;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import org.jetbrains.annotations.NotNull;

public class NetworkHandler extends NetworkCallback {

  final NetworkRequest networkRequest;
  private Context context;
  private static int count = 0;

  public NetworkHandler(Context context) {
    networkRequest =
        new NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build();
    this.context = context;
  }

  public void set() {
    ConnectivityManager connectivityManager =
        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    if (connectivityManager != null) {
      connectivityManager.registerNetworkCallback(networkRequest, this);
    }
  }

  public void remove() {
    ConnectivityManager connectivityManager =
        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    if (connectivityManager != null) {
      connectivityManager.unregisterNetworkCallback(this);
    }
  }

  @Override
  public void onAvailable(@NotNull Network network) {
    if (count > 1) {
      // Do what you need to do here
      ((MainActivity) context).onNetworkChange();
    }
    count++;
  }
}
