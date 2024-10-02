package com.hungama.music.utils.tritonbannerview;

import android.os.Bundle;
import android.view.View;

import com.hungama.myplay.activity.R;
import com.tritondigital.ads.SyncBannerView;

import java.util.ArrayList;

public class BannersWrapper {
    private ArrayList<SyncBannerView> mSyncBannerViews = new ArrayList<>();

    public BannersWrapper(View parentView) {
        SyncBannerView syncBannerView;

        syncBannerView = (SyncBannerView) parentView.findViewById(R.id.banner300x250);
        syncBannerView.setBannerSize(300, 250);
        mSyncBannerViews.add(syncBannerView);
    }


    public void onPause() {
        for (SyncBannerView bannerView : mSyncBannerViews) {
            bannerView.onPause();
        }
    }


    public void onResume() {
        for (SyncBannerView bannerView : mSyncBannerViews) {
            bannerView.onResume();
        }
    }


    public void clear() {
        for (SyncBannerView bannerView : mSyncBannerViews) {
            bannerView.clearBanner();
        }
    }


    public void release() {
        for (SyncBannerView bannerView : mSyncBannerViews) {
            bannerView.release();
        }
        mSyncBannerViews.clear();
    }


    public void loadCuePoint(Bundle cuePoint) {
        for (SyncBannerView bannerView : mSyncBannerViews) {
            bannerView.loadCuePoint(cuePoint);
        }
    }


    public void showAd(Bundle ad) {
        for (SyncBannerView bannerView : mSyncBannerViews) {
            bannerView.showAd(ad);
        }
    }
}

