package com.leondeklerk.smartcontroller.utils;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import com.google.android.material.textfield.TextInputLayout;
import com.leondeklerk.smartcontroller.R;
import com.leondeklerk.smartcontroller.data.DeviceData;
import com.leondeklerk.smartcontroller.devices.SmartDevice;
import java.util.ArrayList;
import lombok.Setter;

public class TextInputLayoutUtils {

  private ArrayList<TextInputLayout> layouts;
  private Context context;
  private Resources resources;
  @Setter String matchPwd;

  //TODO: change this to use AddTextChangedListeners
  public TextInputLayoutUtils(ArrayList<TextInputLayout> layouts, Context context) {
    this.layouts = layouts;
    this.context = context;
    this.matchPwd = null;
    resources = context.getResources();
  }

  public void addLayout(TextInputLayout layout) {
    layouts.add(layout);
  }

  public void removeLayout(TextInputLayout layout) {
    layouts.remove(layout);
  }

  @SuppressWarnings("ConstantConditions")
  public void setErrorListeners() {
    for (final TextInputLayout layout : layouts) {
      layout.setHelperTextEnabled(true);
      final EditText editText = layout.getEditText();
      setClearFocusListener(editText, layout);

      editText.setOnFocusChangeListener(
          new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
              if (!hasFocus) {
                String text = editText.getText().toString();
                if (TextUtils.isEmpty(text)) {
                  setError(layout, resources.getString(R.string.error_input_required));
                } else if (layout.isCounterEnabled()
                    && text.length() > layout.getCounterMaxLength()) {
                  setError(layout, resources.getString(R.string.error_input_length));
                } else if (layout.getId() == R.id.new_ip || layout.getId() == R.id.edit_ip) {
                  if (text.split("\\.").length != 4) {
                    setError(layout, resources.getString(R.string.error_input_invalid_ip));
                  } else {
                    layout.setErrorEnabled(false);
                  }
                } else if (layout.getId() == R.id.change_pwd_old) {
                  if (!getText(layout).equals(matchPwd)) {
                    setError(layout, resources.getString(R.string.update_pwd_wrong));
                  }
                } else {
                  layout.setErrorEnabled(false);
                }
              } else {
                layout.setErrorEnabled(false);
              }
            }
          });
    }
  }

  public void setClearFocusListener(EditText editText, final TextInputLayout layout) {
    editText.setOnEditorActionListener(
        new OnEditorActionListener() {
          @Override
          public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
              layout.clearFocus();
            }
            return false;
          }
        });
  }

  public boolean hasErrors() {
    for (TextInputLayout layout : layouts) {
      layout.requestFocus();
      layout.clearFocus();
      if (layout.isEnabled() && layout.getError() != null) {
        layout.requestFocus();
        InputMethodManager imm =
            (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        assert imm != null;
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
        return true;
      }
    }
    return false;
  }

  @SuppressWarnings("ConstantConditions")
  public SmartDevice readDevice(boolean isProtected, int nextId) {
    ArrayList<String> inputs = new ArrayList<>();
    for (TextInputLayout layout : layouts) {
      EditText editText = layout.getEditText();
      inputs.add(editText.getText().toString());
    }

    DeviceData data =
        new DeviceData(nextId, inputs.get(0), inputs.get(1), isProtected, "UNKNOWN", false);
    if (isProtected) {
      data.setUsername(inputs.get(2)).setPassword(inputs.get(3));
    }
    return new SmartDevice(data);
  }

  /**
   * Check if all the input in the given layouts is the same input.
   *
   * @return true if all are equals, false if not.
   */
  public boolean checkEqual(TextInputLayout first, TextInputLayout second) {
    first.setErrorEnabled(false);
    second.setErrorEnabled(false);
    if (!getText(first).equals(getText(second))) {
      setError(first, resources.getString(R.string.update_pwd_no_match));
      setError(second, resources.getString(R.string.update_pwd_no_match));
      return false;
    }
    return true;
  }

  /**
   * Retrieve a string from a TextInputLayout.
   *
   * @param layout the layout to retrieve the text from.
   * @return the input text.
   */
  public static String getText(TextInputLayout layout) {
    return layout.getEditText().getText().toString();
  }

  /**
   * Set an error on a TextInputLayout.
   *
   * @param layout the layout to set the error on.
   * @param error the value of the error.
   */
  public static void setError(TextInputLayout layout, String error) {
    layout.setErrorEnabled(true);
    layout.setError(error);
  }
}
