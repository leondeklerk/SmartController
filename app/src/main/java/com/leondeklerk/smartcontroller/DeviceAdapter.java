package com.leondeklerk.smartcontroller;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton.OnCheckedChangeListener;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textview.MaterialTextView;
import com.leondeklerk.smartcontroller.DeviceAdapter.CardViewHolder;
import com.leondeklerk.smartcontroller.data.DeviceData;
import com.leondeklerk.smartcontroller.devices.SmartDevice;
import com.leondeklerk.smartcontroller.widget.ColorDotView;
import java.util.ArrayList;
import org.jetbrains.annotations.NotNull;

public class DeviceAdapter extends RecyclerView.Adapter<CardViewHolder> {

  private ArrayList<SmartDevice> devices;
  private Context context;
  private Resources resources;

  // Provide a reference to the views for each data item
  // Complex data items may need more than one view per item, and
  // you provide access to all the views for a data item in a view holder
  public static class CardViewHolder extends RecyclerView.ViewHolder {

    // Each item in the adapter is a CardView
    public MaterialCardView cardView;

    public CardViewHolder(MaterialCardView v) {
      super(v);
      cardView = v;
    }
  }

  public DeviceAdapter(ArrayList<SmartDevice> devices, Context context) {
    this.devices = devices;
    this.context = context;
    this.resources = context.getResources();
  }

  // Create new views (invoked by the layout manager)
  @NotNull
  @Override
  public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    // Create a new MaterialCardView for this item
    MaterialCardView v =
        (MaterialCardView)
            LayoutInflater.from(parent.getContext())
                .inflate(R.layout.component_cards, parent, false);
    return new CardViewHolder(v);
  }

  // Replace the contents of a view (invoked by the layout manager)
  @Override
  public void onBindViewHolder(@NotNull CardViewHolder holder, int position) {
    DeviceData data = devices.get(position).getData();

    MaterialTextView deviceName = holder.cardView.findViewById(R.id.deviceName);
    deviceName.setText(resources.getString(R.string.device_name, data.getName()));

    ColorDotView statusLed = holder.cardView.findViewById(R.id.statusLed);
    statusLed.setVisibility(View.INVISIBLE);

    MaterialTextView deviceStatus = holder.cardView.findViewById(R.id.deviceStatus);
    deviceStatus.setText(resources.getString(R.string.status_unknown));

    MaterialTextView deviceIp = holder.cardView.findViewById(R.id.deviceIp);
    deviceIp.setText(resources.getString(R.string.device_ip, data.getIp()));

    SwitchMaterial devicePower = holder.cardView.findViewById(R.id.devicePower);
    devicePower.setOnCheckedChangeListener((OnCheckedChangeListener) context);
  }

  // Return the size of your dataset (invoked by the layout manager)
  @Override
  public int getItemCount() {
    return devices.size();
  }
}
