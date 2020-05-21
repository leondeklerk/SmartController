package com.leondeklerk.smartcontroller;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
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

public class DeviceAdapter extends RecyclerView.Adapter<CardViewHolder> {

  private ArrayList<SmartDevice> devices;
  private Activity context;

  // Provide a reference to the views for each data item
  // Complex data items may need more than one view per item, and
  // you provide access to all the views for a data item in a view holder
  static class CardViewHolder extends RecyclerView.ViewHolder {

    // Each item in the adapter is a CardView
    ComponentCardsBinding binding;

    CardViewHolder(ComponentCardsBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
    }

    public void bind(SmartDevice device) {
      binding.setDevice(device);
      binding.executePendingBindings();
    }
  }

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

    binding.deviceColor.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(context, DeviceColorActivity.class);
        intent.putExtra(DeviceColorActivity.EXTRA_SELECTED_DEV, position);
        context.startActivity(intent);
      }
    });

    binding.devicePower.setOnCheckedChangeListener(
        new OnCheckedChangeListener() {
          @Override
          public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (buttonView.isPressed()) {
              ResponseTask task = new ResponseTask((NetworkCallback) context, position);
              task.executeOnExecutor(
                  AsyncTask.THREAD_POOL_EXECUTOR, device.getCommand(device.turnOn(isChecked)));
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
