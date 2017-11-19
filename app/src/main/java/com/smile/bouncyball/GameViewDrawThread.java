package com.smile.bouncyball;

import android.graphics.Canvas;
import android.view.SurfaceHolder;
import java.io.InterruptedIOException;

public class GameViewDrawThread extends Thread {

    private MainActivity activity = null;
    private GameView gameView = null;
    private boolean keepRunning = true; // keepRunning = true -> loop in run() still going
    private SurfaceHolder surfaceHolder = null;
    private BallGoThread ballGoThread = null;

    public GameViewDrawThread(GameView gView) {
        this.gameView = gView;
        this.activity = gView.getActivity();
        this.surfaceHolder = gView.getSurfaceHolder();
        this.ballGoThread = gameView.getBallGoThread();
        if (this.ballGoThread == null) {
            // must not be null
            throw new NullPointerException("ballGoThread must not be null.");
        }
    }

    public void run() {
        while (keepRunning) {
            synchronized (activity.activityHandler) {
                // for application's (Main activity) synchronizing
                while (activity.gamePause) {
                    try {
                        activity.activityHandler.wait();
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

            synchronized (ballGoThread) {
                try {
                    ballGoThread.wait();
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
                } catch (InterruptedException ex) {
                    ex.printStackTrace();;
                }
            }
        }
    }

    public void setKeepRunning(boolean keepRunning) {
        this.keepRunning = keepRunning;
    }
}
