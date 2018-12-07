package com.smile.bouncyball;

import android.graphics.Canvas;
import android.view.SurfaceHolder;
import java.io.InterruptedIOException;

public class GameViewDrawThread extends Thread {

    private MainActivity mainActivity = null;
    private GameView gameView = null;
    private boolean keepRunning = true; // keepRunning = true -> loop in run() still going
    private SurfaceHolder surfaceHolder = null;
    private int synchronizeTime = 70;

    public GameViewDrawThread(GameView gView) {
        this.gameView = gView;
        this.mainActivity = gView.getMainActivity();
        this.surfaceHolder = gView.getSurfaceHolder();
        this.synchronizeTime  = gView.getSynchronizeTime();
    }

    public void run() {
        while (keepRunning) {
            synchronized (mainActivity.activityHandler) {
                // for application's (Main activity) synchronizing
                while (mainActivity.gamePause) {
                    try {
                        mainActivity.activityHandler.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }

            synchronized (gameView.gameViewHandler) {
                // for GameView's synchronizing
                while (gameView.gameViewPause) {
                    try {
                        gameView.gameViewHandler.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }

            // start drawing
            Canvas c;
            c = null;
            // lock the whole canvas. high requirement on memory, do not use null advised
            try {
                c = surfaceHolder.lockCanvas(null);
                if (c != null) {
                    // synchronized (gView.surfaceHolder) {
                    synchronized (surfaceHolder) {
                        gameView.doDraw(c); // draw
                        // System.out.println("Drawing .............");
                    }
                } else {
                    System.out.println("Canvas = null.");
                }
            } finally {
                if (c != null) {
                    // fresh the screen
                    surfaceHolder.unlockCanvasAndPost(c);
                }
            }

            try{Thread.sleep(synchronizeTime);}
            catch(Exception e){e.printStackTrace();}
        }
    }

    public void setKeepRunning(boolean keepRunning) {
        this.keepRunning = keepRunning;
    }
}
