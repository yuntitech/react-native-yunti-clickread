package com.yunti.clickread.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.yt.ytdeep.client.dto.ClickReadDTO;
import com.yt.ytdeep.client.dto.ClickReadPage;
import com.yunti.clickread.R;
import com.yunti.clickread.RNYtClickreadModule;
import com.yunti.clickread.fragment.ClickReadCatalogFragment;
import com.yunti.clickread.fragment.ClickReadFragment;
import com.yunti.clickread.fragment.ClickReadFragment.ClickReadFragmentDelegate;


public class ClickReadActivity extends AppCompatActivity
        implements ClickReadFragmentDelegate,
        ClickReadCatalogFragment.ClickReadCatalogFragmentDelegate,
        ClickReadCatalogFragment.OperationCallback {

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
    private ClickReadPage mCurGoPage;

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
            mClickReadCatalogFragment.setOperationCallback(this);
        }
        mDrawer = findViewById(R.id.layout_drawer);
        mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mDrawer.setStatusBarBackgroundColor(Color.BLUE);
        mDrawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                mClickReadCatalogFragment.highLightPageSection(mClickReadFragment.getCurrentPage());
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                if (mCurGoPage != null) {
                    mClickReadFragment.scrollToPage(mCurGoPage);
                    mCurGoPage = null;
                }

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
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
    public void onBuyResult(boolean isBought) {
        if (mClickReadCatalogFragment != null && isBought) {
            mClickReadCatalogFragment.buySuccess();
        }
    }

    @Override
    public void onCatalogClick() {
        if (mDrawer != null) {
            mDrawer.openDrawer(Gravity.LEFT);
        }
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
            case "notifyDownloadStatusChanged":
                if (mClickReadCatalogFragment != null) {
                    mClickReadCatalogFragment.getAndRenderDownloadStatus();
                }
                break;
            case "userHasChanged":
                if (mClickReadFragment != null) {
                    mClickReadFragment.userHasChanged();
                }
                break;
            case "notifyDownloadTotalFileCount":
                if (mClickReadCatalogFragment != null) {
                    long totalFileCount = intent.getLongExtra("totalFileCount", 0);
                    mClickReadCatalogFragment.setTotalFileCount(totalFileCount);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void goSectionPage(ClickReadPage page) {
        moveClickReadFragment();
        this.mCurGoPage = page;
    }

    public void moveClickReadFragment() {
        mDrawer.closeDrawer(Gravity.LEFT);
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
