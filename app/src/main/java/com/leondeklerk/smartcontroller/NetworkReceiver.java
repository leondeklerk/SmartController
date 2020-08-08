package com.leondeklerk.smartcontroller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NetworkReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    // Notify the MainActivity that it needs to update it's cards
    ((MainActivity) context).updateNetworkChange();
    MainActivity.NET_CHANGED = true;
  }
}
