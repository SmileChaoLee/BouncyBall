package com.smile.bouncyball;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.facebook.ads.AudienceNetworkAds;
import com.google.android.gms.ads.MobileAds;
import com.smile.bouncyball.tools.LogUtil;
import com.smile.smilelibraries.facebook_ads_util.FacebookInterstitial;
import com.smile.smilelibraries.google_ads_util.AdMobInterstitial;

public class BouncyBallApp extends Application {
    public static String DATABASE_NAME = "bouncy_ball.db";
    public static String googleAdMobBannerID = "";
    public FacebookInterstitial facebookAds;
    public AdMobInterstitial googleInterstitialAd;

    private static final String TAG = "BouncyBallApp";

    @Override
    public void onCreate() {
        super.onCreate();
        Context ctx = getApplicationContext();
        // Facebook ads (Interstitial ads)

        AudienceNetworkAds.initialize(this);
        String facebookPlacementID = "253834931867002_253835175200311";
        facebookAds = new FacebookInterstitial(ctx, facebookPlacementID);

        // Google AdMob
        // String googleAdMobAppID = getString(R.string.google_AdMobAppID);
        String googleAdMobInterstitialID = "ca-app-pub-8354869049759576/8555812568";
        MobileAds.initialize(ctx, initializationStatus ->
                LogUtil.d(TAG, "Google AdMob was initialized successfully."));
        googleInterstitialAd = new AdMobInterstitial(ctx, googleAdMobInterstitialID);
        googleInterstitialAd.loadAd(); // load first ad
        googleAdMobBannerID = "ca-app-pub-8354869049759576/7770302361";

        final Handler adHandler = new Handler(Looper.getMainLooper());
        final Runnable adRunnable = () -> {
            adHandler.removeCallbacksAndMessages(null);
            if (googleInterstitialAd != null) {
                googleInterstitialAd.loadAd(); // load first google ad
            }
            if (facebookAds != null) {
                facebookAds.loadAd();   // load first facebook ad
            }
        };
        adHandler.postDelayed(adRunnable, 1000);
    }
}
