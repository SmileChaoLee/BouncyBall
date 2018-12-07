package com.smile.bouncyball;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

import com.google.android.gms.ads.MobileAds;
import com.smile.smilepublicclasseslibrary.facebook_ads_util.FacebookInterstitialAds;
import com.smile.smilepublicclasseslibrary.google_admob_ads_util.GoogleAdMobInterstitial;
import com.smile.smilepublicclasseslibrary.scoresqlite.ScoreSQLite;
import com.smile.smilepublicclasseslibrary.showing_instertitial_ads_utility.ShowingInterstitialAdsUtil;

public class BouncyBallApp extends Application {

    // public final String REST_Website = new String("http://192.168.0.11:5000/Playerscore");
    public static final String REST_Website = "http://ec2-13-59-195-3.us-east-2.compute.amazonaws.com/Playerscore";
    public static final int GameId = 3; // this GameId is for backend game_id in playerscore table

    public static Resources AppResources;
    public static Context AppContext;
    public static ScoreSQLite ScoreSQLiteDB;

    public static ShowingInterstitialAdsUtil InterstitialAd;
    public static String googleAdMobBannerID = "";

    private static FacebookInterstitialAds facebookAds;
    private static GoogleAdMobInterstitial googleInterstitialAd;

    @Override
    public void onCreate() {
        super.onCreate();
        AppResources = getResources();
        AppContext = getApplicationContext();
        ScoreSQLiteDB = new ScoreSQLite(AppContext);
        // Facebook ads (Interstitial ads)
        String facebookPlacementID = new String("253834931867002_253835175200311");
        facebookAds = new FacebookInterstitialAds(AppContext, facebookPlacementID);
        facebookAds.loadAd();

        // Google AdMob
        String googleAdMobAppID = getString(R.string.google_AdMobAppID);
        String googleAdMobInterstitialID = "ca-app-pub-8354869049759576/8555812568";
        MobileAds.initialize(AppContext, googleAdMobAppID);
        googleInterstitialAd = new GoogleAdMobInterstitial(AppContext, googleAdMobInterstitialID);
        googleInterstitialAd.loadAd(); // load first ad
        googleAdMobBannerID = "ca-app-pub-8354869049759576/7770302361";

        InterstitialAd = new ShowingInterstitialAdsUtil(facebookAds, googleInterstitialAd);
    }
}
