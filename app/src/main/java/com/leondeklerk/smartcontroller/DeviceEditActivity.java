package com.leondeklerk.smartcontroller;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.appbar.MaterialToolbar;
import org.jetbrains.annotations.NotNull;

public class DeviceEditActivity extends FragmentActivity {

  public static final String EXTRA_SELECTED_DEV = "com.leondeklerk.smartcontroller.SELECTED_DEV";
  public static final String EXTRA_NUM_DEV = "com.leondeklerk.smartcontroller.NUM_DEV";
  private static int numOfDevices;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_device_edit);

    MaterialToolbar toolbar = findViewById(R.id.toolbar);
    toolbar.setNavigationOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            onBackPressed();
          }
        });

    Intent intent = getIntent();
    numOfDevices = intent.getIntExtra(EXTRA_NUM_DEV, 0);

    ViewPager2 viewPager = findViewById(R.id.pager);
    FragmentStateAdapter pagerAdapter = new DeviceEditFragmentAdapter(this);

    viewPager.setAdapter(pagerAdapter);
    viewPager.setCurrentItem(intent.getIntExtra(EXTRA_SELECTED_DEV, 0));
  }

  private static class DeviceEditFragmentAdapter extends FragmentStateAdapter {

    DeviceEditFragmentAdapter(FragmentActivity fragmentActivity) {
      super(fragmentActivity);
    }

    @NotNull
    @Override
    public Fragment createFragment(int position) {
      // TODO pass device data along
      Fragment fragment = new DeviceEditFragment();
      Bundle args = new Bundle();
      args.putInt(DeviceEditFragment.ARG_FRAG_NUM, position);
      fragment.setArguments(args);
      return fragment;
    }

    @Override
    public int getItemCount() {
      return numOfDevices;
    }
  }
}
