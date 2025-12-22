package com.smile.bouncyball

import android.app.Application
import com.facebook.ads.AudienceNetworkAds
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.InitializationStatus
import com.smile.bouncyball.tools.LogUtil

class BouncyBallApp : Application() {

    companion object {
        private const val TAG = "BouncyBallApp"
        var DATABASE_NAME: String = "bouncy_ball.db"
        var googleAdMobBannerID: String = ""
    }

    /*  No Interstitial ad for now
    var facebookAds: FacebookInterstitial? = null
    var googleInterstitialAd: AdMobInterstitial? = null
    */

    override fun onCreate() {
        super.onCreate()
        val ctx = applicationContext

        // Facebook ads (Interstitial ads)
        AudienceNetworkAds.initialize(this)
        // val facebookPlacementID = "253834931867002_253835175200311"
        // facebookAds = FacebookInterstitial(ctx, facebookPlacementID)

        // Google AdMob
        // String googleAdMobAppID = getString(R.string.google_AdMobAppID);
        val googleAdMobInterstitialID = "ca-app-pub-8354869049759576/8555812568"
        MobileAds.initialize(
            ctx) { status: InitializationStatus? ->
            LogUtil.d(TAG, "Google AdMob was initialized successfully.")
        }
        /*
        googleInterstitialAd = AdMobInterstitial(ctx, googleAdMobInterstitialID)
        googleInterstitialAd!!.loadAd() // load first ad
        val adHandler = Handler(Looper.getMainLooper())
        val adRunnable = Runnable {
            adHandler.removeCallbacksAndMessages(null)
            if (googleInterstitialAd != null) {
                googleInterstitialAd!!.loadAd() // load first google ad
            }
            if (facebookAds != null) {
                facebookAds!!.loadAd() // load first facebook ad
            }
        }
        adHandler.postDelayed(adRunnable, 1000)
        */

        googleAdMobBannerID = "ca-app-pub-8354869049759576/7770302361"
    }
}
