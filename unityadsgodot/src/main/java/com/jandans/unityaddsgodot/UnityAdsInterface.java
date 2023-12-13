package com.jandans.unityaddsgodot;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.LayoutDirection;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.UnityAdsLoadOptions;
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
import java.util.UUID;

public class UnityAdsInterface extends GodotPlugin  {

    private final String TAG = "UnityAdsInterface";
    // This banner view object will be placed at the top of the screen:
    BannerView banner;
    // This banner view object will be placed at the bottom of the screen:
//    BannerView bottomBanner;
    // View objects to display banners:
    RelativeLayout bannerView;
//    RelativeLayout bottomBannerView;
    private SignalInfo UnityAdsInitCompleted = new SignalInfo("UnityAdsInitCompleted");
    private SignalInfo UnityAdsInitFailed = new SignalInfo("UnityAdsInitFailed",String.class);
    private SignalInfo UnityAdsReady = new SignalInfo("UnityAdsReady",String.class);
    private SignalInfo UnityAdsStart = new SignalInfo("UnityAdsStart",String.class);
    private SignalInfo UnityAdsClicked = new SignalInfo("UnityAdsClicked",String.class);
    private SignalInfo UnityAdsFinish = new SignalInfo("UnityAdsFinish", String.class, String.class);
    private SignalInfo UnityAdsRewardedFinished = new SignalInfo("UnityAdsRewardedFinished", String.class);
    private SignalInfo UnityAdsRewardedSkipped = new SignalInfo("UnityAdsRewardedSkipped", String.class);

    private SignalInfo UnityAdsLoadError = new SignalInfo("UnityAdsLoadError", String.class,String.class);
    private SignalInfo UnityAdsShowError = new SignalInfo("UnityAdsShowError", String.class,String.class);
    private SignalInfo UnityBannerLoaded = new SignalInfo("UnityBannerLoaded",String.class);
    // private SignalInfo UnityBannerUnloaded = new SignalInfo("UnityBannerUnloaded",String.class);
    // private SignalInfo UnityBannerShow = new SignalInfo("UnityBannerShow",String.class);
    private SignalInfo UnityBannerClick = new SignalInfo("UnityBannerClick",String.class);
    // private SignalInfo UnityBannerHide = new SignalInfo("UnityBannerHide",String.class);
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

    private IUnityAdsInitializationListener initListener = new IUnityAdsInitializationListener() {
        @Override
        public void onInitializationComplete() {

            emitSignal(UnityAdsInitCompleted.getName());
            Log.v(TAG, "Unity Ads initialization completed");
        }

        @Override
        public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {
            emitSignal(UnityAdsInitFailed.getName(),message);
            Log.e(TAG, "Unity Ads initialization failed with error: [" + error + "] " + message);
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
                add(UnityAdsInitCompleted);
                add(UnityAdsInitFailed);
                add(UnityAdsReady);
                add(UnityAdsStart);
                add(UnityAdsFinish);
                add(UnityAdsRewardedFinished);
                add(UnityAdsRewardedSkipped);
                add(UnityAdsClicked);
                add(UnityAdsLoadError);
                add(UnityAdsShowError);
                add(UnityBannerLoaded);
                // add(UnityBannerUnloaded);
                // add(UnityBannerShow);
                // add(UnityBannerHide);
                add(UnityBannerError);
                add(UnityBannerClick);
            }
        };
    }

    @UsedByGodot
    public void initialise(final String appId, final boolean testMode)
    {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try
                {
                    UnityAds.initialize(getActivity(), appId, testMode,initListener);
                }
                catch (Exception ex)
                {
                    Log.e(TAG, ex.getMessage());
                }
            }
        });

    }


    @UsedByGodot
    public void loadAd(final String placementId)
    {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                UnityAds.load(placementId,loadListener);
            }
        });

    }

    @UsedByGodot
    public void showInter(final String placementId)
    {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (UnityAds.isInitialized())
                {
                    try
                    {
                        UnityAds.show(getActivity(), placementId,interListener);
                    }
                    catch (Exception ex)
                    {
                        Log.e(TAG, ex.getMessage());
//                return false;
                    }
//            return true;
                }
                else
                {
                    Log.i(TAG, "Adds not ready");
//            return false;
                }
            }
        });

    }

    @UsedByGodot
    public void showRewarded(final String placementId)
    {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (UnityAds.isInitialized())
                {
                    try
                    {
                        UnityAds.show(getActivity(), placementId,rewardedListener);
                    }
                    catch (Exception ex)
                    {
                        Log.e(TAG, ex.getMessage());
//                return false;
                    }
//            return true;
                }
                else
                {
                    Log.i(TAG, "Ads not ready");
//            return false;
                }
            }
        });

    }

    @UsedByGodot
    public void showBanner(final String placementID, final boolean top) {

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try
                {
                    if(bannerView==null)
                    {
                        loadBanner(placementID,top);
                        
                    }
                    bannerView.addView(banner);

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
                    if(bannerView!=null)
                    {
                        bannerView.removeAllViews();
//                        banner=null;
//                        bannerView=null;

                    }

                }
            });
        }
        catch (Exception ex)
        {
            Log.e(TAG, ex.getMessage());
        }
    }

    @UsedByGodot
    public void loadBanner(String placementId, final boolean top) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                int gravity;
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                if (top==true)
                {
                    gravity = Gravity.TOP | Gravity.CENTER;
                    params.addRule(RelativeLayout.ALIGN_PARENT_TOP);

                }
                else
                {
                    gravity = Gravity.BOTTOM | Gravity.CENTER;
                    params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                }
                banner = new BannerView(getActivity(), placementId, new UnityBannerSize(320,50));
                banner.setListener(bannerListener);

                if(bannerView==null)
                {
                    RelativeLayout.LayoutParams viewParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
                    bannerView = new RelativeLayout((Context) getActivity());
                    bannerView.setLayoutParams(params);
                    bannerView.setGravity(gravity);
                    getActivity().addContentView(bannerView,viewParams);
                }


                String mObjectId = UUID.randomUUID().toString();
                UnityAdsLoadOptions loadOptions = new UnityAdsLoadOptions();
//                loadOptions.setAdMarkup(markup);
                loadOptions.setObjectId(mObjectId);

                banner.load(loadOptions);
            }
        });

    }

    //Banner stuff goes here



}
