package com.smile.bouncyball;

import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.InterruptedIOException;

public class GameViewDrawThread extends Thread {

    private int sleepSpan = 100;
    private MainActivity activity = null;
    private GameView gView = null;

    private boolean flag = true;
    private boolean pause = false;

    public GameViewDrawThread(GameView gView) {
        this.gView = gView;
        this.activity = gView.getActivity();
        this.sleepSpan = gView.synchronizeTime / 2;
    }

    public void run() {
        while (flag) {
            synchronized (activity.gameHandler) {
                while (activity.gamePause) {
                    try {
                        activity.gameHandler.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }

            Canvas c;
            c = null;
            // lock the whole canvas. high requirement on memory, do not use null advised
            try {
                c = gView.surfaceHolder.lockCanvas(null);
                if (c != null) {
                    synchronized (gView.surfaceHolder) {
                        gView.doDraw(c); // draw
                        // System.out.println("Drawing .............");
                    }
                } else {
                    // System.out.println("Canvas = null\n");
                }
            } finally {
                if (c != null) {
                    // fresh the screen
                    gView.surfaceHolder.unlockCanvasAndPost(c);
                }
            }
            try {
                Thread.sleep(sleepSpan);
            } catch (Exception e) {
                e.printStackTrace(); // error message
            }
        }

    }

    public boolean getFlag() {
        return this.flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public boolean getPause() {
       return this.pause;
    }

    public void setPause(boolean pause) {
        this.pause = pause;
    }
}
