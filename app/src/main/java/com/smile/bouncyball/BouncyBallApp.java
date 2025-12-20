package com.smile.bouncyball;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;

import com.facebook.ads.AudienceNetworkAds;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.smile.bouncyball.tools.LogUtil;
import com.smile.smilelibraries.facebook_ads_util.FacebookInterstitial;
import com.smile.smilelibraries.google_ads_util.AdMobInterstitial;
import com.smile.smilelibraries.scoresqlite.ScoreSQLite;
import com.smile.smilelibraries.show_interstitial_ads.ShowInterstitial;
import com.smile.smilelibraries.utilities.ScreenUtil;

public class BouncyBallApp extends Application {

    // public final String REST_Website = new String("http://192.168.0.11:5000/Playerscore");
    public static final String REST_Website = "http://ec2-13-59-195-3.us-east-2.compute.amazonaws.com/Playerscore";
    public static final String GameId = "3"; // this GameId is for backend game_id in playerscore table
    public static final int FontSize_Scale_Type = ScreenUtil.FontSize_Pixel_Type;

    public static Resources AppResources;
    public static Context AppContext;
    public static ScoreSQLite ScoreSQLiteDB;
    public static String DATABASE_NAME = "bouncy_ball.db";

    public static ShowInterstitial InterstitialAd;
    public static String googleAdMobBannerID = "";

    public static FacebookInterstitial facebookAds;
    public static AdMobInterstitial googleInterstitialAd;

    private static final String TAG = "BouncyBallApp";

    @Override
    public void onCreate() {
        super.onCreate();
        AppResources = getResources();
        AppContext = getApplicationContext();
        ScoreSQLiteDB = new ScoreSQLite(AppContext, DATABASE_NAME);
        // Facebook ads (Interstitial ads)

        AudienceNetworkAds.initialize(this);
        String facebookPlacementID = "253834931867002_253835175200311";
        facebookAds = new FacebookInterstitial(AppContext, facebookPlacementID);

        // Google AdMob
        String googleAdMobAppID = getString(R.string.google_AdMobAppID);
        String googleAdMobInterstitialID = "ca-app-pub-8354869049759576/8555812568";
        MobileAds.initialize(AppContext, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                LogUtil.d(TAG, "Google AdMob was initialized successfully.");
            }

        });
        googleInterstitialAd = new AdMobInterstitial(AppContext, googleAdMobInterstitialID);
        googleInterstitialAd.loadAd(); // load first ad
        googleAdMobBannerID = "ca-app-pub-8354869049759576/7770302361";

        // InterstitialAd = new ShowingInterstitialAdsUtil(AppContext, facebookAds, googleInterstitialAd);

        final Handler adHandler = new Handler(Looper.getMainLooper());
        final Runnable adRunnable = new Runnable() {
            @Override
            public void run() {
                adHandler.removeCallbacksAndMessages(null);
                if (googleInterstitialAd != null) {
                    googleInterstitialAd.loadAd(); // load first google ad
                }
                if (facebookAds != null) {
                    facebookAds.loadAd();   // load first facebook ad
                }
            }
        };
        adHandler.postDelayed(adRunnable, 1000);
    }
}
