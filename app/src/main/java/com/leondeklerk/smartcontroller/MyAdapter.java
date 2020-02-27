package com.leondeklerk.smartcontroller;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.leondeklerk.smartcontroller.devices.SmartDevice;
import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

  private ArrayList<SmartDevice> devices;

  // Provide a reference to the views for each data item
  // Complex data items may need more than one view per item, and
  // you provide access to all the views for a data item in a view holder
  public static class MyViewHolder extends RecyclerView.ViewHolder {

    // each data item is just a string in this case
    public MaterialCardView cardView;

    public MyViewHolder(MaterialCardView v) {
      super(v);
      cardView = v;
    }
  }

  // Provide a suitable constructor (depends on the kind of dataset)
  public MyAdapter(ArrayList<SmartDevice> devices) {
    this.devices = devices;
  }

  // Create new views (invoked by the layout manager)
  @Override
  public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
      int viewType) {
    // create a new view
    MaterialCardView v = (MaterialCardView) LayoutInflater.from(parent.getContext())
        .inflate(R.layout.component_cards, parent, false);
    return new MyViewHolder(v);
  }

  // Replace the contents of a view (invoked by the layout manager)
  @Override
  public void onBindViewHolder(MyViewHolder holder, int position) {
  }

  // Return the size of your dataset (invoked by the layout manager)
  @Override
  public int getItemCount() {
    return devices.size();
  }

  public void setVisibility(View view, boolean on) {
    if (on) {
      view.setVisibility(View.VISIBLE);
    } else {
      view.setVisibility(View.INVISIBLE);
    }
  }
}