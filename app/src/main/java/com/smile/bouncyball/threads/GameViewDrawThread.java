package com.smile.bouncyball.threads;

import android.graphics.Canvas;
import android.os.SystemClock;
import android.view.SurfaceHolder;

import com.smile.bouncyball.GameView;
import com.smile.bouncyball.tools.LogUtil;

public class GameViewDrawThread extends Thread {

    private final static String TAG = "GameViewDrawThread";
    private GameView gameView = null;
    // keepRunning = true -> loop in run() still going
    private volatile boolean keepRunning = true;
    private SurfaceHolder surfaceHolder = null;
    private int synchronizeTime = 70;

    public GameViewDrawThread(GameView gView) {
        this.gameView = gView;
        this.surfaceHolder = gView.getSurfaceHolder();
        this.synchronizeTime  = gView.synchronizeTime;
    }

    public void run() {
        while (keepRunning) {
            synchronized (gameView.mainLock) {
                // for application's (Main activity) synchronizing
                while (!gameView.isGameVisible) {
                    try {
                        gameView.mainLock.wait();
                    } catch (InterruptedException e) {
                        LogUtil.e(TAG, "run.mainLock.InterruptedException", e);
                    }
                }
            }
            synchronized (gameView.gameLock) {
                // for GameView's synchronizing
                while (gameView.isPausedByUser) {
                    try {
                        gameView.gameLock.wait();
                    } catch (InterruptedException e) {
                        LogUtil.e(TAG, "run.gameLock.InterruptedException", e);
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
                    }
                } else {
                    LogUtil.d(TAG, "run.lockCanvas.Canvas = null.");
                }
            } finally {
                if (c != null) {
                    // fresh the screen
                    surfaceHolder.unlockCanvasAndPost(c);
                }
            }
            SystemClock.sleep(synchronizeTime);
        }
    }

    public void setKeepRunning(boolean keepRunning) {
        this.keepRunning = keepRunning;
    }
}
