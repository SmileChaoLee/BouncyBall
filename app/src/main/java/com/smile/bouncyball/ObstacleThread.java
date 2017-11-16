package com.smile.bouncyball;

import android.app.Activity;
import android.graphics.Color;

/**
 * Created by chaolee on 2017-11-14.
 */

public class ObstacleThread {
    private Activity activity = null;
    private int sleepSpan = 0;
    private boolean flag = true;
    private int direction = 0;  // stay still. 1->up, 2->down, 3->left, 4->right
    private int speed = 0;  // no moving
    private int color = Color.BLACK;    // the color of obstacle

    public ObstacleThread(GameView gView) {
        this.activity = gView.getActivity();
        this.sleepSpan  = gView.getSynchronizeTime();

    }
}
