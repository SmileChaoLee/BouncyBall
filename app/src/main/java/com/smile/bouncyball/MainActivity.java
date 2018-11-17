package com.smile.bouncyball;

import android.graphics.Point;
import android.os.Handler;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import com.smile.bouncyball.Utility.ScreenUtl;
import java.lang.reflect.Field;

public class MainActivity extends AppCompatActivity {

    // private properties
    private static final String TAG = new String("com.smile.bouncyball.MainActivity");

    private int screenWidth = 0;
    private int screenHeight = 0;

    private GameView gameView = null;
    private int gameViewWidth = 0;      // the width of game view
    private int gameViewHeight = 0;     // the height of game view

    // public properties
    public boolean gamePause = false;
    public Handler activityHandler = null;
    public LinearLayout gameLayout = null;

    public interface Constants {
        String LOG = "com.smile.bouncyball";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        System.out.println("onCreate() is called.");

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN ,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
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

        setContentView(R.layout.activity_main);

        gamePause = false;
        activityHandler = new Handler();

        Point size = new Point();
        ScreenUtl.getScreenSize(this, size);
        screenWidth  = size.x;
        screenHeight = size.y;

        int statusBarHeight = ScreenUtl.getStatusBarHeight(this);
        int actionBarHeight = ScreenUtl.getActionBarHeight(this);

        /*
        ActionBar actionBar = getSupportActionBar();
        // actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);    // enable customized action bar
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.LTGRAY));
        actionBar.setCustomView(R.layout.action_bar_layout);
        View actionBarView = actionBar.getCustomView();
        */

        LinearLayout mainUiLayout = findViewById(R.id.mainUiLayout);
        float weightSum = mainUiLayout.getWeightSum();
        if (weightSum == 0.0f) {
            weightSum = 20.0f;  // default weight sum
        }

        int realHeight = screenHeight - actionBarHeight;

        // game view
        gameLayout = findViewById(R.id.layoutForGameView);
        LinearLayout.LayoutParams fLp = (LinearLayout.LayoutParams) gameLayout.getLayoutParams();
        float gameWeight = fLp.weight;
        gameViewWidth = screenWidth;
        gameViewHeight = (int)((float)realHeight * (gameWeight/weightSum) );

        gameView = new GameView(this);   // create a gameView
        gameLayout.addView(gameView);

        // facebook banner ads view
        // LinearLayout adLayout = findViewById(R.id.facebookBannerAds);
        // LinearLayout.LayoutParams adLp = (LinearLayout.LayoutParams) adLayout.getLayoutParams();
        // float adsWeight = adLp.weight;
        // int adsHeight = realHeight - gameViewHeight;
        // System.out.println("Height of Banner Ads View = " + adsHeight);

        // boolean isTable = ScreenUtl.isTablet(this);
        // int adSizeId = (!isTable) ? 1 : 2; // phone is 1, others is 2 (like tablet)
        // facebookBannerAdView = new FacebookBannerAds(this,"253834931867002_253835175200311", adSizeId);
        // adLayout.addView(facebookBannerAdView.getBannerAdView());
        // facebookBannerAdView.showAd(TAG);

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

        synchronized (activityHandler) {
            gamePause = false;
            activityHandler.notifyAll();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        System.out.println("onPause() is called.");

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
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.newGame) {
            gameView.releaseSynchronizings();
            gameView.newGame();
            return true;
        }

        if (id == R.id.top_10_score) {
            gameView.getTop10ScoreList();
            return true;
        }

        if (id == R.id.scoreHistory) {
            gameView.getScoreHistory();
            return true;
        }

        if (id == R.id.quitGame) {
            Handler handlerClose = new Handler();
            handlerClose.postDelayed(new Runnable() {
                public void run() {
                    finish();
                }
            },300);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void finishApplication() {
        // release resources and threads
        gameView.releaseSynchronizings();
        gameView.stopThreads();
    }
}
