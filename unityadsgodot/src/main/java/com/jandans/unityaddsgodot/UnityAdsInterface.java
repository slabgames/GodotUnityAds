package com.jandans.unityaddsgodot;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.UnityAdsShowOptions;
import com.unity3d.services.banners.BannerErrorInfo;
import com.unity3d.services.banners.BannerView;
import com.unity3d.services.banners.IUnityBannerListener;
import com.unity3d.services.banners.UnityBannerSize;
import com.unity3d.services.banners.UnityBanners;
import com.unity3d.services.banners.view.BannerPosition;

import org.godotengine.godot.Godot;
import org.godotengine.godot.plugin.GodotPlugin;
import org.godotengine.godot.plugin.SignalInfo;
import org.godotengine.godot.plugin.UsedByGodot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UnityAdsInterface extends GodotPlugin implements IUnityAdsInitializationListener {

    private final String TAG = "UnityAdsInterface";
    // This banner view object will be placed at the top of the screen:
    BannerView topBanner;
    // This banner view object will be placed at the bottom of the screen:
    BannerView bottomBanner;
    // View objects to display banners:
    RelativeLayout topBannerView;
    RelativeLayout bottomBannerView;
    private SignalInfo UnityAdsReady = new SignalInfo("UnityAdsReady",String.class);
    private SignalInfo UnityAdsStart = new SignalInfo("UnityAdsStart",String.class);
    private SignalInfo UnityAdsClicked = new SignalInfo("UnityAdsClicked",String.class);
    private SignalInfo UnityAdsFinish = new SignalInfo("UnityAdsFinish", String.class, String.class);
    private SignalInfo UnityAdsRewardedFinished = new SignalInfo("UnityAdsRewardedFinished", String.class);
    private SignalInfo UnityAdsRewardedSkipped = new SignalInfo("UnityAdsRewardedSkipped", String.class);

    private SignalInfo UnityAdsLoadError = new SignalInfo("UnityAdsLoadError", String.class,String.class);
    private SignalInfo UnityAdsShowError = new SignalInfo("UnityAdsShowError", String.class,String.class);
    private SignalInfo UnityBannerLoaded = new SignalInfo("UnityBannerLoaded",String.class);
    private SignalInfo UnityBannerUnloaded = new SignalInfo("UnityBannerUnloaded",String.class);
    private SignalInfo UnityBannerShow = new SignalInfo("UnityBannerShow",String.class);
    private SignalInfo UnityBannerClick = new SignalInfo("UnityBannerClick",String.class);
    private SignalInfo UnityBannerHide = new SignalInfo("UnityBannerHide",String.class);
    private SignalInfo UnityBannerError = new SignalInfo("UnityBannerError", String.class,String.class);

    private IUnityAdsLoadListener loadListener = new IUnityAdsLoadListener() {
        @Override
        public void onUnityAdsAdLoaded(String placementId) {
//            UnityAds.show((Activity)getActivity(), placementId, new UnityAdsShowOptions(), showListener);
            emitSignal(UnityAdsReady.getName(),placementId);
        }

        @Override
        public void onUnityAdsFailedToLoad(String placementId, UnityAds.UnityAdsLoadError error, String message) {
            Log.e("UnityAdsExample", "Unity Ads failed to load ad for " + placementId + " with error: [" + error + "] " + message);
            emitSignal(UnityAdsLoadError.getName(),placementId,message);
        }
    };

    private IUnityAdsShowListener rewardedListener = new IUnityAdsShowListener() {
        @Override
        public void onUnityAdsShowFailure(String placementId, UnityAds.UnityAdsShowError error, String message) {
            Log.e(TAG, "Unity Ads failed to show ad for " + placementId + " with error: [" + error + "] " + message);
            emitSignal(UnityAdsShowError.getName(),placementId,message);
        }

        @Override
        public void onUnityAdsShowStart(String placementId) {
            Log.v(TAG, "onUnityAdsShowStart: " + placementId);
            emitSignal(UnityAdsStart.getName(),placementId);
        }

        @Override
        public void onUnityAdsShowClick(String placementId) {
            Log.v(TAG, "onUnityAdsShowClick: " + placementId);
            emitSignal(UnityAdsClicked.getName(),placementId);
        }

        @Override
        public void onUnityAdsShowComplete(String placementId, UnityAds.UnityAdsShowCompletionState finishState) {
            Log.v("UnityAdsExample", "onUnityAdsShowComplete: " + placementId);
            if (finishState.equals(UnityAds.UnityAdsShowCompletionState.COMPLETED)) {
                // Reward the user for watching the ad to completion
                emitSignal(UnityAdsRewardedFinished.getName(),placementId);
            } else {
                // Do not reward the user for skipping the ad
                emitSignal(UnityAdsRewardedSkipped.getName(),placementId);
            }
        }
    };

    private IUnityAdsShowListener interListener = new IUnityAdsShowListener() {
        @Override
        public void onUnityAdsShowFailure(String placementId, UnityAds.UnityAdsShowError error, String message) {
            Log.e(TAG, "Unity Ads failed to show ad for " + placementId + " with error: [" + error + "] " + message);
            emitSignal(UnityAdsShowError.getName(),placementId,message);
        }

        @Override
        public void onUnityAdsShowStart(String placementId) {
            Log.v(TAG, "onUnityAdsShowStart: " + placementId);
            emitSignal(UnityAdsStart.getName(),placementId);
        }

        @Override
        public void onUnityAdsShowClick(String placementId) {
            Log.v(TAG, "onUnityAdsShowClick: " + placementId);
            emitSignal(UnityAdsClicked.getName(),placementId);
        }

        @Override
        public void onUnityAdsShowComplete(String placementId, UnityAds.UnityAdsShowCompletionState finishState) {
            Log.v("UnityAdsExample", "onUnityAdsShowComplete: " + placementId);
            int state = -1;

        // Implement conditional logic for each ad completion status:
        if (finishState == UnityAds.UnityAdsShowCompletionState.COMPLETED) {
            // Reward the user for watching the ad to completion.
            state = 2;
        } else if (finishState == UnityAds.UnityAdsShowCompletionState.SKIPPED) {
            // user skipped the ad.
            state = 1;
        } else {
            // Log an error.
            Log.e(TAG, placementId);
            state = 0;
        }
            emitSignal(UnityAdsFinish.getName(), placementId, String.format("%d", state));
        }
    };

    private BannerView.IListener bannerListener = new BannerView.IListener() {
        @Override
        public void onBannerLoaded(BannerView bannerAdView) {
            // Called when the banner is loaded.
            Log.v(TAG, "onBannerLoaded: " + bannerAdView.getPlacementId());
            // Enable the correct button to hide the ad
//            (bannerAdView.getPlacementId().equals("topBanner") ? hideTopBannerButton : hideBottomBannerButton).setEnabled(true);
            emitSignal(UnityBannerLoaded.getName(),bannerAdView.getPlacementId());
        }

        @Override
        public void onBannerFailedToLoad(BannerView bannerAdView, BannerErrorInfo errorInfo) {
            Log.e(TAG, "Unity Ads failed to load banner for " + bannerAdView.getPlacementId() + " with error: [" + errorInfo.errorCode + "] " + errorInfo.errorMessage);
            // Note that the BannerErrorInfo object can indicate a no fill (refer to the API documentation).
            emitSignal(UnityBannerError.getName(),bannerAdView.getPlacementId(),errorInfo.errorMessage);
        }

        @Override
        public void onBannerClick(BannerView bannerAdView) {
            // Called when a banner is clicked.
            Log.v(TAG, "onBannerClick: " + bannerAdView.getPlacementId());
            emitSignal(UnityBannerClick.getName(),bannerAdView.getPlacementId());
        }

        @Override
        public void onBannerLeftApplication(BannerView bannerAdView) {
            // Called when the banner links out of the application.
            Log.v(TAG, "onBannerLeftApplication: " + bannerAdView.getPlacementId());

        }
    };

    public UnityAdsInterface(Godot godot) {
        super(godot);
    }

    @androidx.annotation.NonNull
    @Override
    public String getPluginName() {
        return "UnityAdsGodot";
    }


    @NonNull
    @Override
    public Set<SignalInfo> getPluginSignals() {
        return new HashSet<SignalInfo>() {
            {
                add(UnityAdsReady);
                add(UnityAdsStart);
                add(UnityAdsFinish);
                add(UnityAdsRewardedFinished);
                add(UnityAdsRewardedSkipped);
                add(UnityAdsClicked);
                add(UnityAdsLoadError);
                add(UnityAdsShowError);
                add(UnityBannerLoaded);
                add(UnityBannerUnloaded);
                add(UnityBannerShow);
                add(UnityBannerHide);
                add(UnityBannerError);
            }
        };
    }

    @UsedByGodot
    public void initialise(String appId, boolean testMode)
    {
        try
        {
            UnityAds.initialize(getActivity(), appId, testMode,this);
        }
        catch (Exception ex)
        {
            Log.e(TAG, ex.getMessage());
        }
    }


    @UsedByGodot
    public void loadAd(String placementId)
    {
        UnityAds.load(placementId,loadListener);
    }

    @UsedByGodot
    public boolean showInter(String placementId)
    {
        if (UnityAds.isInitialized())
        {
            try
            {
                UnityAds.show(getActivity(), placementId,interListener);
            }
            catch (Exception ex)
            {
                Log.e(TAG, ex.getMessage());
                return false;
            }
            return true;
        }
        else
        {
            Log.i(TAG, "Adds not ready");
            return false;
        }
    }

    @UsedByGodot
    public boolean showRewarded(String placementId)
    {
        if (UnityAds.isInitialized())
        {
            try
            {
                UnityAds.show(getActivity(), placementId,rewardedListener);
            }
            catch (Exception ex)
            {
                Log.e(TAG, ex.getMessage());
                return false;
            }
            return true;
        }
        else
        {
            Log.i(TAG, "Adds not ready");
            return false;
        }
    }

    @UsedByGodot
    public void showBanner(final String placementID, final boolean top) {

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try
                {
                    if (top)
                    {
                        topBannerView.addView(topBanner);
                        bottomBannerView.removeAllViews();
                    }
                    else
                    {
                        bottomBannerView.addView(bottomBanner);
                        topBannerView.removeAllViews();
                    }

                }
                catch (Exception ex)
                {
                    Log.e(TAG, ex.getMessage());
                }
            }
        });
    }

    @UsedByGodot
    public void hideBanner() {
        try
        {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(topBanner!=null)
                    {
                        topBannerView.removeAllViews();

//                        topBannerView=null;
//                        topBanner=null;

                    }

                    if(bottomBanner!=null)
                    {
                        bottomBannerView.removeAllViews();
//                        bottomBannerView=null;
//                        bottomBanner=null;
                    }
                }
            });
//            UnityBanners.destroy();
        }
        catch (Exception ex)
        {
            Log.e(TAG, ex.getMessage());
        }
    }

    @UsedByGodot
    public void loadBanner(String placementId, boolean top) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (top==true)
                {
                    topBanner = new BannerView(getActivity(), placementId, new UnityBannerSize(320,50));
                    topBanner.setListener(bannerListener);
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.addRule(RelativeLayout.ALIGN_TOP,RelativeLayout.TRUE);
                    topBannerView = new RelativeLayout((Context) getActivity(), (AttributeSet) params);

                    topBanner.load();


                }
                else
                {
                    bottomBanner = new BannerView(getActivity(), placementId, new UnityBannerSize(320,50));
                    bottomBanner.setListener(bannerListener);
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.addRule(RelativeLayout.ALIGN_BOTTOM,RelativeLayout.TRUE);
                    bottomBannerView = new RelativeLayout((Context) getActivity(), (AttributeSet) params);

                    bottomBanner.load();
                }
            }
        });

    }

    //Banner stuff goes here


    @Override
    public void onInitializationComplete() {
        Log.v(TAG, "Unity Ads initialization completed");
    }

    @Override
    public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {
        Log.e(TAG, "Unity Ads initialization failed with error: [" + error + "] " + message);
    }
}
