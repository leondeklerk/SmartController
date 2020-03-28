package com.leondeklerk.smartcontroller;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import org.jetbrains.annotations.NotNull;

public class DeviceEditActivity extends FragmentActivity {

  private static final int NUM_PAGES = 5; // TODO give num via intent
  // TODO set current device

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_device_edit);

    ViewPager2 viewPager = findViewById(R.id.pager);
    FragmentStateAdapter pagerAdapter = new DeviceEditFragmentAdapter(this);
    viewPager.setAdapter(pagerAdapter);
  }

  private static class DeviceEditFragmentAdapter extends FragmentStateAdapter {

    DeviceEditFragmentAdapter(FragmentActivity fa) {
      super(fa);
    }

    @NotNull
    @Override
    public Fragment createFragment(int position) {
      //TODO pass device data along
      return new DeviceEditFragment();
    }

    @Override
    public int getItemCount() {
      return NUM_PAGES;
    }
  }
}
