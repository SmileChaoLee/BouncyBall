package com.smile.bouncyball;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import static android.content.ContentValues.TAG;

import java.lang.reflect.Field;

import static android.content.DialogInterface.BUTTON_NEUTRAL;

public class MainActivity extends AppCompatActivity {

    String[] stageLevels ;
    String startStr = "";
    String pauseStr = "";
    String resumeStr = "";

    String beginStr = "";
    String gameOverStr = "";
    String winStr = "";

    TextView stageName;
    ImageView scoreImage0;
    ImageView scoreImage1;
    ImageView scoreImage2;

    private int screenWidth = 0;
    private int screenHeight = 0;

    private GameView gameView = null;
    // private int autoRotate = 1;
    private boolean firstRun = true;

    public boolean gamePause = false;
    public Handler activityHandler = null;
    public FrameLayout frameLayout = null;

    public interface Constants {
        String LOG = "com.smile.bouncyball";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN ,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);


        ActionBar actionBar = getSupportActionBar();
        // actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);    // enable customized action bar
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.LTGRAY));

        actionBar.setCustomView(R.layout.action_bar_layout);
        View actionBarView = actionBar.getCustomView();

        /*
        ActionBar.LayoutParams params = new ActionBar.LayoutParams(
                // center the TextView in action bar
                ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT,
                Gravity.CENTER
        );
        View actionBarView = getLayoutInflater().inflate(R.layout.action_bar_layout,null);
        */

        System.out.println("onCreate()\n");

        // autoRotate = android.provider.Settings.System.getInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

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

        stageLevels = getResources().getStringArray(R.array.stageLevels);

        startStr = getResources().getString(R.string.start_string);
        pauseStr = getResources().getString(R.string.pause_string);
        resumeStr = getResources().getString(R.string.resume_string);

        beginStr = getResources().getString(R.string.begin_string);
        gameOverStr = getResources().getString(R.string.gameOver_string);
        winStr = getResources().getString(R.string.win_string);

        stageName = (TextView)actionBarView.findViewById(R.id.stageName);
        stageName.setText(stageLevels[0]);   // start from stage 1
        scoreImage0 = actionBarView.findViewById(R.id.scoreView0);
        scoreImage1 = actionBarView.findViewById(R.id.scoreView1);
        scoreImage2 = actionBarView.findViewById(R.id.scoreView2);

        // Display d = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth  = size.x;
        screenHeight = size.y - 100;

        gamePause = false;
        activityHandler = new Handler();
        gameView = new GameView(this);   // create a gameView

        View mainView = getLayoutInflater().inflate(R.layout.activity_main,null);
        frameLayout = mainView.findViewById(R.id.frameLayout);
        frameLayout.addView(gameView);
        // setContentView(gameView);
        setContentView(frameLayout);
    }

    @Override
    public void onStart() {
        super.onStart();
    }


    @Override
    public void onResume() {
        super.onResume();

        synchronized (activityHandler) {
            gamePause = false;
            activityHandler.notifyAll();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

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
        super.onDestroy();
        System.out.println("onDestroy --> Setting Screen orientation to User");
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
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

        if (gamePause) {
            // in pause status
            synchronized (activityHandler) {
                gamePause = false;
                activityHandler.notifyAll();
            }
        }

        if (gameView.gameViewPause) {
            // GameView in pause status
            synchronized (gameView.gameViewHandler) {
                gameView.gameViewPause = false;
                gameView.gameViewHandler.notifyAll();
            }
        }

        if (id == R.id.replayGame) {
            gameView.newGame();
            return true;
        }

        if (id == R.id.quitGame) {
            gameView.stopThreads();
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public int getScreenWidth() {
        return this.screenWidth;
    }
    public int getScreenHeight() {
        return this.screenHeight;
    }
}
