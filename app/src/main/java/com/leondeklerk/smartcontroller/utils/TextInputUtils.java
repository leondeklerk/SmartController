package com.leondeklerk.smartcontroller.utils;

import android.content.res.Resources;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import com.google.android.material.textfield.TextInputLayout;
import com.leondeklerk.smartcontroller.R;
import com.leondeklerk.smartcontroller.data.DeviceData;
import com.leondeklerk.smartcontroller.devices.RGBLedController;
import com.leondeklerk.smartcontroller.devices.SmartDevice;
import java.util.ArrayList;

/**
 * A collection of utility methods used to interact with TextInputLayouts. The functionalities range
 * from setting listeners, to checking for errors and comparing values.
 */
public class TextInputUtils {
  public static final String DEV_TYPE_DEF = "DEFAULT_TYPE";
  public static final String DEV_TYPE_RGB = "RGB_CONTROLLER_TYPE";
  // An input type that is an IPV4 ip
  public static final String IP_TYPE = "IP_TYPE";
  // An input type that is a field with a max length
  public static final String DEFAULT_TYPE = "DEFAULT_TYPE";

  /**
   * Check if a list of layouts has anny errors in them, this also checks for empty layouts.
   *
   * @param layouts the ArrayList of TextInputLayouts to check.
   * @return true if there are errors false if not.
   */
  public static boolean hasErrors(ArrayList<TextInputLayout> layouts) {
    // Check if one of the layouts is empty
    isEmpty(layouts);

    for (TextInputLayout layout : layouts) {
      if (layout.getError() != null) {
        // If a layout has an error, return true and request the focus on that one.
        layout.requestFocus();
        return true;
      }
    }
    return false;
  }

  /**
   * Checks if an ArrayList of TextInputLayouts has any empty fields or not. If one field is empty
   * the correct error will be set.
   *
   * @param layouts the list of layouts
   */
  @SuppressWarnings("ConstantConditions")
  private static void isEmpty(ArrayList<TextInputLayout> layouts) {
    for (TextInputLayout layout : layouts) {
      // Get the text and resources from the layout
      String text = layout.getEditText().getText().toString();
      Resources resources = layout.getResources();
      if (TextUtils.isEmpty(text)) {
        // If empty set an error
        layout.setError(resources.getString(R.string.error_input_required));
      }
    }
  }

  /**
   * Create a new SmartDevice by reading an ArrayList of TextInputLayouts together with some
   * additional information. Based on the values a new SmartDevice will be created and returned.
   *
   * @param layouts the list of fields to read the data from.
   * @param isProtected true if the device contains credentials, false if not.
   * @param nextId the id of this new device.
   * @return a new SmartDevice based on the read data.
   */
  @SuppressWarnings("ConstantConditions")
  public static SmartDevice readDevice(
      String type, ArrayList<TextInputLayout> layouts, boolean isProtected, int nextId) {
    ArrayList<String> inputs = new ArrayList<>();

    // Read each input and add it to the list of inputs
    for (TextInputLayout layout : layouts) {
      EditText editText = layout.getEditText();
      inputs.add(editText.getText().toString());
    }

    // Create a new device
    DeviceData data =
        new DeviceData(nextId, inputs.get(0), inputs.get(1), isProtected, "UNKNOWN", false, type);

    // If it requires credentials, also add these
    if (isProtected) {
      data.setUsername(inputs.get(2)).setPassword(inputs.get(3));
    }
    // Return the type of device
    if (type.equals(DEV_TYPE_RGB)) {
      return new RGBLedController(data);
    }
    return new SmartDevice(data);
  }

  /**
   * Check if two TextInputLayouts have the same input text, which is used for example to check if a
   * password is correctly confirmed.
   *
   * @param first the first layout to compare (the baseline)
   * @param second the second layout to compare.
   */
  public static void checkEqual(TextInputLayout first, TextInputLayout second) {
    // If they are not equal
    if (!getText(first).equals(getText(second))) {
      // Set errors with resource strings
      Resources resources = first.getResources();
      first.setError(resources.getString(R.string.update_pwd_no_match));
      second.setError(resources.getString(R.string.update_pwd_no_match));
    } else {
      // Else remove the error
      first.setError(null);
      second.setError(null);
    }
  }

  /**
   * Check if a layout correctly matches a reference password. This is used to check if a user can
   * correctly identify itself.
   *
   * @param pwd the layout containing the password.
   * @param reference the reference password to match.
   */
  public static void checkPwd(TextInputLayout pwd, String reference) {
    // If there is not match
    if (!getText(pwd).equals(reference)) {
      Resources resources = pwd.getResources();
      // Set an error
      pwd.setError(resources.getString(R.string.update_pwd_wrong));
    } else {
      // Remove any errors
      pwd.setError(null);
    }
  }

  /**
   * Check if an IP input field is of the correct length, meaning it consist out of 4 parts of
   * digits. Will set an error if this requirement is not matched.
   *
   * @param layout the layout to check.
   */
  @SuppressWarnings("ConstantConditions")
  public static void checkIp(TextInputLayout layout) {
    String text = layout.getEditText().getText().toString();
    Resources resources = layout.getResources();
    // If the IP is not in the 255.255.255.255 format set an error
    if (text.split("\\.").length != 4) {
      layout.setError(resources.getString(R.string.error_input_invalid_ip));
    } else {
      layout.setError(null);
    }
  }

  /**
   * Retrieve a string from a TextInputLayout.
   *
   * @param layout the layout to retrieve the text from.
   * @return the input text.
   */
  @SuppressWarnings("ConstantConditions")
  public static String getText(TextInputLayout layout) {
    return layout.getEditText().getText().toString();
  }

  /**
   * Set the correct filters and error listeners to handle errors on the user input.
   *
   * @param layout the layout to set the filter on.
   * @param type the type of input field, either IP_TYPE or DEFAULT_TYPE.
   */
  @SuppressWarnings("ConstantConditions")
  public static void setListener(final TextInputLayout layout, String type) {
    switch (type) {
      case IP_TYPE:
        // If it is an IP type set a filter on the EditText
        layout.getEditText().setFilters(new InputFilter[] {new IpInputFilter()});
        break;
      case DEFAULT_TYPE:
        // The default type needs a error handler for surpassing the maximum length.
        layout
            .getEditText()
            .addTextChangedListener(
                new TextWatcher() {
                  @Override
                  public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                  @Override
                  public void onTextChanged(CharSequence s, int start, int before, int count) {}

                  @Override
                  public void afterTextChanged(Editable s) {
                    // If the length is to great, write an error
                    if (s.length() > layout.getCounterMaxLength()) {
                      Resources resources = layout.getResources();
                      layout.setError(resources.getString(R.string.error_input_length));
                    } else {
                      layout.setError(null);
                    }
                  }
                });
        break;
      default:
        Log.d("TextInputLayout type", type);
    }
  }
}
