package com.leondeklerk.smartcontroller.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.leondeklerk.smartcontroller.R;
import com.leondeklerk.smartcontroller.devices.SmartDevice;
import java.lang.reflect.Type;
import java.util.ArrayList;

/** A class to handle the storage and retrieval of devices in the applications SharedPreferences. */
public class DeviceStorageUtils {
  private SharedPreferences preferences;
  private Context context;

  /**
   * Basic constructor for the DeviceStorageUtils class.
   *
   * @param preferences the preferences to store and retrieve in/from.
   */
  public DeviceStorageUtils(SharedPreferences preferences, Context context) {
    this.preferences = preferences;
    this.context = context;
  }

  /**
   * A method that retrieves all SmartDevices from a String in the SharedPreferences using the GSON
   * library.
   *
   * @return a list of retrieved SmartDevices.
   */
  public ArrayList<SmartDevice> getDevices() {
    String json = preferences.getString("deviceList", null);
    if (json != null) {
      Gson gson = new Gson();
      // Convert back to a Java Object
      Type type = new TypeToken<ArrayList<SmartDevice>>() {}.getType();
      return gson.fromJson(json, type);
    } else {
      return new ArrayList<>();
    }
  }

  /**
   * A method that converts a list of SmartDevices to a String and stores it in the
   * SharedPreferences specified by the class Object.
   *
   * @param devices the list of devices to store.
   */
  public void storeDevices(ArrayList<SmartDevice> devices) {
    // Set the status to unknown (prevent the status from being stored)
    for(SmartDevice device : devices) {
      device.getData().setStatus(context.getString(R.string.status_unknown));
    }

    Editor prefsEditor = preferences.edit();
    Gson gson = new Gson();
    // Convert the object to a String
    String json = gson.toJson(devices);
    // Store the string
    prefsEditor.putString("deviceList", json);
    prefsEditor.apply();
  }
}
