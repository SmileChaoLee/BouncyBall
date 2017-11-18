package com.smile.bouncyball;

import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InterruptedIOException;

public class GameViewDrawThread extends Thread {

    private int sleepSpan = 100;
    private MainActivity activity = null;
    private GameView gameView = null;
    private boolean flag = true;
    private SurfaceHolder surfaceHolder = null;
    private BallGoThread ballGoThread = null;

    public GameViewDrawThread(GameView gView) {
        this.gameView = gView;
        this.activity = gView.getActivity();
        this.sleepSpan = gView.getSynchronizeTime() / 2;
        this.surfaceHolder = gView.getSurfaceHolder();
        this.ballGoThread = gameView.getBallGoThread();
    }

    public void run() {
        while (flag) {
            synchronized (activity.activityHandler) {
                while (activity.gamePause) {
                    try {
                        activity.activityHandler.wait();
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

            /*
            try {
                Thread.sleep(sleepSpan);
            } catch (Exception ex) {
                ex.printStackTrace(); // error message
            }
            */
        }
    }

    public boolean getFlag() {
        return this.flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }
}
