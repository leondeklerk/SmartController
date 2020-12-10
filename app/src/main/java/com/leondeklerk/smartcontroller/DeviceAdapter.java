package com.leondeklerk.smartcontroller;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import androidx.recyclerview.widget.RecyclerView;
import com.leondeklerk.smartcontroller.DeviceAdapter.CardViewHolder;
import com.leondeklerk.smartcontroller.databinding.ComponentCardsBinding;
import com.leondeklerk.smartcontroller.devices.SmartDevice;
import java.util.ArrayList;
import org.jetbrains.annotations.NotNull;

/** Adapter for a RecyclerView filled with SmartDevice instances. */
public class DeviceAdapter extends RecyclerView.Adapter<CardViewHolder> {

  private ArrayList<SmartDevice> devices;
  private Activity context;

  /** A view for each of the cards in the RecyclerView. */
  static class CardViewHolder extends RecyclerView.ViewHolder {

    ComponentCardsBinding binding;

    /**
     * Default constructor
     *
     * @param binding the binding that represents the view.
     */
    CardViewHolder(ComponentCardsBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
    }

    /**
     * Bind the smartDevice to the layout.
     *
     * @param device the device to bind.
     */
    public void bind(SmartDevice device) {
      binding.setDevice(device);
      binding.executePendingBindings();
    }
  }

  /**
   * Default constructor for the adapter, takes the context and the list of devices.
   *
   * @param devices the devices to create this adapter with.
   * @param context the application context to use.
   */
  DeviceAdapter(ArrayList<SmartDevice> devices, Activity context) {
    this.devices = devices;
    this.context = context;
  }

  // Create new views (invoked by the layout manager)
  @NotNull
  @Override
  public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    // Create a new MaterialCardView for this item
    LayoutInflater inflater = LayoutInflater.from(parent.getContext());
    ComponentCardsBinding binding = ComponentCardsBinding.inflate(inflater, parent, false);
    return new CardViewHolder(binding);
  }

  // Replace the contents of a view (invoked by the layout manager)
  @Override
  public void onBindViewHolder(@NotNull CardViewHolder holder, final int position) {
    final SmartDevice device = devices.get(position);
    holder.bind(device);
    ComponentCardsBinding binding = holder.binding;

    // Button for the edit Activity
    binding.deviceEdit.setOnClickListener(
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            Intent intent = new Intent(context, DeviceEditActivity.class);
            intent.putExtra(DeviceEditActivity.EXTRA_SELECTED_DEV, position);
            intent.putExtra(DeviceEditActivity.EXTRA_NUM_DEV, getItemCount());

            context.startActivityForResult(intent, 0);
          }
        });

    // Button for the color Activity
    binding.deviceColor.setOnClickListener(
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            Intent intent = new Intent(context, DeviceColorActivity.class);
            intent.putExtra(DeviceColorActivity.EXTRA_SELECTED_DEV, position);
            context.startActivity(intent);
          }
        });

    // Switch for the power.
    binding.devicePower.setOnCheckedChangeListener(
        new OnCheckedChangeListener() {
          @Override
          public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            // Check if it is pressed by the users (not anything else)
            if (buttonView.isPressed()) {
              MqttClient client = ((MainActivity) context).getMqttClient();
              client.publish(device.setPower(isChecked));
            }
          }
        });
  }

  // Return the size of your dataset (invoked by the layout manager)
  @Override
  public int getItemCount() {
    return devices.size();
  }
}
