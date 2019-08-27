package com.yunti.clickread.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.yt.ytdeep.client.dto.ClickReadDTO;
import com.yunti.clickread.R;
import com.yunti.clickread.RNYtClickreadModule;
import com.yunti.clickread.fragment.ClickReadCatalogFragment;
import com.yunti.clickread.fragment.ClickReadFragment;
import com.yunti.clickread.fragment.ClickReadFragment.ClickReadFragmentDelegate;


public class ClickReadActivity extends AppCompatActivity
        implements ClickReadFragmentDelegate,
        ClickReadCatalogFragment.ClickReadCatalogFragmentDelegate {

    public static final String NAME = "com.yunti.clickread.activity.ClickReadActivity";
    private ClickReadCatalogFragment mClickReadCatalogFragment;
    private ClickReadFragment mClickReadFragment;
    private DrawerLayout mDrawer;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (NAME.equals(intent.getAction())) {
                doAction(intent);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(mBroadcastReceiver, new IntentFilter(NAME));
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
        if (mClickReadCatalogFragment != null) {
            mClickReadCatalogFragment.setDelegate(this);
        }
        mDrawer = findViewById(R.id.layout_drawer);
        mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mDrawer.setStatusBarBackgroundColor(Color.BLUE);
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(getApplicationContext())
                .unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }

    @Override
    public void onResponse(final ClickReadDTO clickReadDTO) {
        if (mClickReadCatalogFragment != null) {
            mClickReadCatalogFragment.refresh(clickReadDTO);
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

    @Override
    public boolean isBought() {
        return mClickReadFragment != null && mClickReadFragment.isBought();
    }

    private void doAction(Intent intent) {
        String action = intent.getStringExtra("action");
        switch (action) {
            case "buySuccess":
                if (mClickReadFragment != null) {
                    mClickReadFragment.buySuccess();
                }
                if (mClickReadCatalogFragment != null) {
                    mClickReadCatalogFragment.buySuccess();
                }
                break;
            case "notifyDownloadStatus":
                if (mClickReadCatalogFragment != null) {
                    mClickReadCatalogFragment.renderDownloadStatus(intent.getStringExtra("status"));
                }
                break;
            case "notifyDownloadStatusChanged":
                if (mClickReadCatalogFragment != null) {
                    mClickReadCatalogFragment.getAndRenderDownloadStatus();
                }
                break;
            default:
                break;
        }
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
