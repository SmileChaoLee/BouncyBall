package com.smile.bouncyball;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;
import static android.content.ContentValues.TAG;

import java.lang.reflect.Field;

import static android.content.DialogInterface.BUTTON_NEUTRAL;

public class MainActivity extends AppCompatActivity {

    int screenWidth = 0;
    int screenHeight = 0;
    GameView gameView=null;

    private int autoRotate = 1;
    private boolean firstRun = true;

    public boolean gamePause = false;
    public Handler gameHandler = null;


    public interface Constants {
        String LOG = "com.smile.bouncyball";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN ,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // setContentView(R.layout.activity_main);
        System.out.println("onCreate()\n");

        autoRotate = android.provider.Settings.System.getInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
        // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
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


        // Display d = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        screenWidth  = size.x;
        screenHeight = size.y-100;

        gamePause = false;
        gameHandler = new Handler();
        gameView = new GameView(this);   // create a gameView
        // setContentView(gameView);
    }

    @Override
    public void onStart() {
        super.onStart();
    }


    @Override
    public void onResume() {
        super.onResume();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (android.os.Build.VERSION.SDK_INT >=23) {
            if (!Settings.System.canWrite(MainActivity.this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + MainActivity.this.getPackageName()));
                startActivity(intent);
            }
        }

        try {
            android.provider.Settings.System.putInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, autoRotate);
            System.out.println("onResume --> Succeeded to set screen rotation setting.");
        } catch (Exception e) {
            System.out.println("onResume --> Failed to set screen rotation setting.");
            e.printStackTrace();
        }

        synchronized (gameHandler) {
            gamePause = false;
            gameHandler.notifyAll();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        try {
            android.provider.Settings.System.putInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, autoRotate);
            System.out.println("onPause --> Succeeded to set screen rotation setting.");
        } catch (Exception e) {
            System.out.println("onPause --> Failed to set screen rotation setting.");
            e.printStackTrace();
        }

        synchronized (gameHandler) {
            gamePause = true;
        }
        // super.onPause();
    }

    @Override
    public void onStop() {
        try {
            android.provider.Settings.System.putInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, autoRotate);
            System.out.println("onStop --> Succeeded to set screen rotation setting.");
        } catch (Exception e) {
            System.out.println("onStop --> Failed to set screen rotation setting.");
            e.printStackTrace();
        }
        super.onStop();
    }

    @Override
    public void onRestart() {
        super.onRestart();
    }

    @Override
    public void onDestroy() {
        try {
            android.provider.Settings.System.putInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, autoRotate);
            System.out.println("onDestroy --> Succeeded to set screen rotation setting.");

        } catch (Exception e) {
            System.out.println("onDestroy --> Failed to set screen rotation setting.");
            e.printStackTrace();
        }
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
