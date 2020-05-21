package com.leondeklerk.smartcontroller.utils;

import androidx.recyclerview.widget.DiffUtil.Callback;
import com.leondeklerk.smartcontroller.devices.SmartDevice;
import java.util.ArrayList;

public class DiffUtilCallback extends Callback {

  private ArrayList<SmartDevice> oldList;
  private ArrayList<SmartDevice> newList;

  public DiffUtilCallback(ArrayList<SmartDevice> oldList, ArrayList<SmartDevice> newList) {
    this.oldList = oldList;
    this.newList = newList;
  }

  @Override
  public int getOldListSize() {
    return oldList.size();
  }

  @Override
  public int getNewListSize() {
    return newList.size();
  }

  @Override
  public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
    return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
  }

  @Override
  public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
    return oldList.get(oldItemPosition).getData().equals(newList.get(newItemPosition).getData());
  }
}
