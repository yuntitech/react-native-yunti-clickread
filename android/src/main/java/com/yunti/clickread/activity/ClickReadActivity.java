package com.yunti.clickread.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.yt.ytdeep.client.dto.ClickReadDTO;
import com.yunti.clickread.R;
import com.yunti.clickread.RNYtClickreadModule;
import com.yunti.clickread.fragment.ClickReadCatalogFragment;
import com.yunti.clickread.fragment.ClickReadFragment;
import com.yunti.clickread.fragment.ClickReadFragment.ClickReadFragmentDelegate;


public class ClickReadActivity extends AppCompatActivity
        implements ClickReadFragmentDelegate {

    ClickReadCatalogFragment mClickReadCatalogFragment;
    ClickReadFragment mClickReadFragment;
    DrawerLayout mDrawer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clickread);
        FragmentManager fragmentManager = getSupportFragmentManager();
        mClickReadFragment
                = (ClickReadFragment) fragmentManager.findFragmentById(R.id.clickread_fragment);
        if (mClickReadFragment != null) {
            mClickReadFragment.setDelegate(this);
            mClickReadFragment.setArguments(getIntent().getExtras());
        }
        mClickReadCatalogFragment
                = (ClickReadCatalogFragment) fragmentManager.findFragmentById(R.id.catalog_fragment);
        mDrawer = findViewById(R.id.layout_drawer);
        mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mDrawer.setStatusBarBackgroundColor(Color.BLUE);
        RNYtClickreadModule.activity = this;
    }

    @Override
    protected void onDestroy() {
        RNYtClickreadModule.activity = null;
        super.onDestroy();
    }

    @Override
    public void onResponse(final ClickReadDTO clickReadDTO) {
        if (mClickReadCatalogFragment != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mClickReadCatalogFragment.refresh(clickReadDTO);
                }
            });
        }
    }

    @Override
    public void onCatalogClick() {
        if (mDrawer != null) {
            mDrawer.openDrawer(Gravity.LEFT);
        }
//
    }

    @Override
    public void onBackClick() {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        RNYtClickreadModule.pop(getApplicationContext());
        super.onBackPressed();
    }

    class MyRunnable implements Runnable {

        @Override
        public void run() {
//            mDrawer.postDelayed(new MyRunnable(), 2000);
//
//
//            ActivityManager mActivityManager = (ActivityManager) getSystemService("activity");
//            List<ActivityManager.RunningTaskInfo> runningTaskInfos = mActivityManager.getRunningTasks(1);
//            if (runningTaskInfos != null) {
//                ActivityManager.RunningTaskInfo info = (ActivityManager.RunningTaskInfo) runningTaskInfos.get(0);
//                Log.d("##", "numActivities is  " + info.numActivities);
//            }
        }
    }

}
