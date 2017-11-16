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
    private GameView gView = null;
    private boolean flag = true;
    private SurfaceHolder surfaceHolder = null;

    public GameViewDrawThread(GameView gView) {
        this.gView = gView;
        this.activity = gView.getActivity();
        this.sleepSpan = gView.getSynchronizeTime() / 2;
        this.surfaceHolder = gView.getSurfaceHolder();
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
                c = surfaceHolder.lockCanvas(null);
                if (c != null) {
                    // synchronized (gView.surfaceHolder) {
                    synchronized (surfaceHolder) {
                        gView.doDraw(c); // draw
                        // System.out.println("Drawing .............");
                    }
                } else {
                    // System.out.println("Canvas = null\n");
                }
            } finally {
                if (c != null) {
                    // fresh the screen
                    surfaceHolder.unlockCanvasAndPost(c);
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
}
