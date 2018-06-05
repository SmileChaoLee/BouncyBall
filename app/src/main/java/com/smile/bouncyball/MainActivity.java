package com.smile.bouncyball;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smile.bouncyball.Utility.ScreenUtl;

import static android.content.ContentValues.TAG;

import java.lang.reflect.Field;

import static android.content.DialogInterface.BUTTON_NEUTRAL;

public class MainActivity extends AppCompatActivity {

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

        gamePause = false;
        activityHandler = new Handler();
        gameView = new GameView(this);   // create a gameView

        /*
        View mainView = getLayoutInflater().inflate(R.layout.activity_main,null);
        frameLayout = mainView.findViewById(R.id.frameLayout);
        frameLayout.addView(gameView);
        setContentView(frameLayout);
        */
        setContentView(gameView);
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
        // release and destroy threads and resources before destroy activity
        finishApplication();

        System.out.println("onDestroy --> Setting Screen orientation to User");
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

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
