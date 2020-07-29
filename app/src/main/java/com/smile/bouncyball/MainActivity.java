package com.smile.bouncyball;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Handler;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.smile.bouncyball.Service.GlobalTop10IntentService;
import com.smile.bouncyball.Service.LocalTop10IntentService;
import com.smile.smilelibraries.alertdialogfragment.AlertDialogFragment;
import com.smile.smilelibraries.showing_instertitial_ads_utility.ShowingInterstitialAdsUtil;
import com.smile.smilelibraries.utilities.ScreenUtil;

import java.lang.reflect.Field;

public class MainActivity extends AppCompatActivity {

    // private properties
    private static final String TAG = new String("com.smile.bouncyball.MainActivity");

    private float textFontSize;
    private float fontScale;
    private GameView gameView = null;
    private LinearLayout bannerLinearLayout = null;
    private AdView bannerAdView = null;
    private AlertDialogFragment showLoadingDialog;
    private String showLoadingDialogTag = "ShowLoadingDialogTag";

    // public properties
    public boolean gamePause = false;
    public Handler activityHandler = null;
    public LinearLayout gameLayout = null;

    private final int LocalTop10RequestCode = 1;
    private final int GlobalTop10RequestCode = 2;
    private BroadcastReceiver bReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        System.out.println("onCreate() is called.");

        /*
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN ,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        */

        // for adding three dots on actionbar on some android devices
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if(menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ex) {
            // Ignore
        }
        //

        if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        float defaultTextFontSize = ScreenUtil.getDefaultTextSizeFromTheme(this, BouncyBallApp.FontSize_Scale_Type, null);
        textFontSize = ScreenUtil.suitableFontSize(this, defaultTextFontSize, BouncyBallApp.FontSize_Scale_Type, 0.0f);
        fontScale = ScreenUtil.suitableFontScale(this, BouncyBallApp.FontSize_Scale_Type, 0.0f);

        String loadingString = getString(R.string.loadingString);
        showLoadingDialog = AlertDialogFragment.newInstance(loadingString, BouncyBallApp.FontSize_Scale_Type, textFontSize, Color.RED, 0, 0, true);
        setContentView(R.layout.activity_main);

        gamePause = false;
        activityHandler = new Handler();

        // game view
        gameLayout = findViewById(R.id.layoutForGameView);
        LinearLayout.LayoutParams fLp = (LinearLayout.LayoutParams) gameLayout.getLayoutParams();

        gameView = new GameView(this, textFontSize);   // create a gameView
        gameLayout.addView(gameView);

        if (!BouncyBallApp.googleAdMobBannerID.isEmpty()) {
            bannerLinearLayout = findViewById(R.id.linearlayout_for_ads_in_myActivity);
            bannerAdView = new AdView(this);

            LinearLayout.LayoutParams bannerLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            bannerLp.gravity = Gravity.CENTER;
            bannerAdView.setLayoutParams(bannerLp);
            bannerAdView.setAdSize(AdSize.BANNER);
            bannerAdView.setAdUnitId(BouncyBallApp.googleAdMobBannerID);
            bannerLinearLayout.addView(bannerAdView);
            AdRequest adRequest = new AdRequest.Builder().build();
            bannerAdView.loadAd(adRequest);
        }

        bReceiver = new GroundhogHunterBroadcastReceiver();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case LocalTop10RequestCode:
                if (resultCode == Activity.RESULT_OK) {
                    Log.i(TAG, "Top10ScoreActivity returned successfully.");
                } else {
                    Log.i(TAG, "Top10ScoreActivity did not return successfully.");
                }
                Log.i(TAG, "Showing Ad from AdMob or Facebook");
                if (BouncyBallApp.InterstitialAd != null) {
                    int entryPoint = 0; //  no used
                    ShowingInterstitialAdsUtil.ShowInterstitialAdThread showInterstitialAdThread =
                            BouncyBallApp.InterstitialAd.new ShowInterstitialAdThread(entryPoint);
                    showInterstitialAdThread.startShowAd();
                }
                break;
            case GlobalTop10RequestCode:
                Log.i(TAG, "Showing Ad from AdMob or Facebook");
                if (BouncyBallApp.InterstitialAd != null) {
                    int entryPoint = 0; //  no used
                    ShowingInterstitialAdsUtil.ShowInterstitialAdThread showInterstitialAdThread =
                            BouncyBallApp.InterstitialAd.new ShowInterstitialAdThread(entryPoint);
                    showInterstitialAdThread.startShowAd();
                }
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        System.out.println("onStart() is called.");
    }


    @Override
    public void onResume() {
        super.onResume();
        System.out.println("onResume() is called.");

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocalTop10IntentService.Action_Name);
        intentFilter.addAction(GlobalTop10IntentService.Action_Name);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(bReceiver, intentFilter);

        synchronized (activityHandler) {
            gamePause = false;
            activityHandler.notifyAll();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        System.out.println("onPause() is called.");

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.unregisterReceiver(bReceiver);

        synchronized (activityHandler) {
            gamePause = true;
        }
        // super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onRestart() {
        super.onRestart();
    }

    @Override
    public void onDestroy() {
        // release and destroy threads and resources before destroy activity

        System.out.println("onDestroy --> Setting Screen orientation to User");
        // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        finishApplication();

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        // capture the event of back button when it is pressed
        // change back button behavior
        quitGame();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        final Context wrapper = new ContextThemeWrapper(this, R.style.menu_text_style);

        // according to the above explanations, the following statement will fit every situation
        ScreenUtil.resizeMenuTextIconSize(wrapper, menu, fontScale);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.local_top_10) {
            getLocalTop10ScoreList();
            return true;
        }

        if (id == R.id.global_top_10) {
            getGlobalTop10ScoreList();
            return true;
        }

        if (id == R.id.newGame) {
            gameView.releaseSynchronizings();
            gameView.newGame();
            return true;
        }

        if (id == R.id.quitGame) {
            quitGame();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void quitGame() {
        Log.i(TAG, "Showing Ad from AdMob or Facebook");
        if (BouncyBallApp.InterstitialAd != null) {
            int entryPoint = 0; //  no used
            ShowingInterstitialAdsUtil.ShowInterstitialAdThread showInterstitialAdThread =
                    BouncyBallApp.InterstitialAd.new ShowInterstitialAdThread(entryPoint
                            , new ShowingInterstitialAdsUtil.AfterDismissFunctionOfShowAd() {
                        @Override
                        public void executeAfterDismissAds(int endPoint) {
                            quitApplication();
                        }
                    });
            showInterstitialAdThread.startShowAd();
        }
    }

    private void quitApplication() {
        final Handler handlerClose = new Handler();
        final int timeDelay = 200;
        handlerClose.postDelayed(new Runnable() {
            public void run() {
                // quit game
                finish();
            }
        },timeDelay);
    }

    private void finishApplication() {
        // release resources and threads
        gameView.releaseSynchronizings();
        gameView.stopThreads();
    }

    private void getLocalTop10ScoreList() {
        // showing loading message
        showLoadingDialog.show(getSupportFragmentManager(), showLoadingDialogTag);

        Intent serviceIntent = new Intent(BouncyBallApp.AppContext, LocalTop10IntentService.class);
        startService(serviceIntent);
    }

    private void getGlobalTop10ScoreList() {
        // showing loading message
        showLoadingDialog.show(getSupportFragmentManager(), showLoadingDialogTag);

        Intent serviceIntent = new Intent(BouncyBallApp.AppContext, GlobalTop10IntentService.class);
        String webUrl = BouncyBallApp.REST_Website + "/GetTop10PlayerscoresREST";  // ASP.NET Core
        webUrl += "?gameId=" + BouncyBallApp.GameId;   // parameters
        serviceIntent.putExtra("WebUrl", webUrl);
        startService(serviceIntent);
    }

    // private class (Nested class)
    private class GroundhogHunterBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            showLoadingDialog.dismissAllowingStateLoss();

            if (intent == null) {
                return;
            }

            Bundle extras = intent.getExtras();

            String actionName = intent.getAction();
            switch (actionName) {
                case LocalTop10IntentService.Action_Name:
                    // dismiss showing message
                    // dismissShowingLoadingMessage();
                    Intent localTop10Intent = new Intent(getApplicationContext(), Top10ScoreActivity.class);
                    Bundle localTop10Extras = new Bundle();
                    localTop10Extras.putString("Top10TitleName", getString(R.string.local_top_10_score_title));
                    localTop10Extras.putStringArrayList("Top10Players", extras.getStringArrayList("PlayerNames"));
                    localTop10Extras.putIntegerArrayList("Top10Scores", extras.getIntegerArrayList("PlayerScores"));
                    localTop10Extras.putFloat("TextFontSize", textFontSize);
                    localTop10Intent.putExtras(localTop10Extras);
                    startActivityForResult(localTop10Intent, LocalTop10RequestCode);
                    break;
                case GlobalTop10IntentService.Action_Name:
                    // dismiss showing message
                    // dismissShowingLoadingMessage();
                    Intent globalTop10Intent = new Intent(getApplicationContext(), Top10ScoreActivity.class);
                    Bundle globalTop10Extras = new Bundle();
                    globalTop10Extras.putString("Top10TitleName", getString(R.string.global_top_10_score_title));
                    globalTop10Extras.putStringArrayList("Top10Players", extras.getStringArrayList("PlayerNames"));
                    globalTop10Extras.putIntegerArrayList("Top10Scores", extras.getIntegerArrayList("PlayerScores"));
                    globalTop10Extras.putFloat("TextFontSize", textFontSize);
                    globalTop10Intent.putExtras(globalTop10Extras);
                    startActivityForResult(globalTop10Intent, GlobalTop10RequestCode);
                    break;
            }

        }
    }
}
