package com.leondeklerk.smartcontroller.utils;

import android.text.InputFilter;
import android.text.Spanned;

public class IpInputFilter implements InputFilter {

  @Override
  public CharSequence filter(
      CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
    if (end > start) {
      String destTxt = dest.toString();
      String resultingTxt =
          destTxt.substring(0, dstart) + source.subSequence(start, end) + destTxt.substring(dend);
      if (!resultingTxt.matches(
          "^\\d{1,3}(\\." + "(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3})?)?)?)?)?)?")) {
        return "";
      } else {
        String[] splits = resultingTxt.split("\\.");
        for (String split : splits) {
          if (Integer.parseInt(split) > 255) {
            return "";
          }
        }
      }
    }
    return null;
  }
}
