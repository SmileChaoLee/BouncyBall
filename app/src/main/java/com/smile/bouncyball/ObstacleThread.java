package com.smile.bouncyball;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;

import java.util.Random;

/**
 * Created by chaolee on 2017-11-14.
 */

public class ObstacleThread extends Thread{

    private final int[] obsColor = {Color.BLACK, Color.DKGRAY, Color.RED, Color.GREEN, Color.MAGENTA,Color.CYAN};
    private final int obstacleWidth = 100;
    private final int obstacleHeight = 20;

    private MainActivity activity = null;
    private int sleepSpan = 0;
    private boolean flag = true;
    private int direction = 1;  // 1->left, 2->right, 3->up, 4->down
    private int speed = 0;  // no moving, moving speed (left, right, up, or down)
    private int color = Color.BLACK;    // the color of obstacle
    private Point position = null;  // the position of the center of this obstacle

    private int xRangeOfObstacle = 0;
    private int yRangeOfObstacle = 0;

    private Random random = null;

    public ObstacleThread(GameView gView) {
        this.activity = gView.getActivity();
        this.sleepSpan  = gView.getSynchronizeTime();
        this.xRangeOfObstacle = gView.getScreenWidth();
        this.yRangeOfObstacle = gView.getScreenHeight() / 3;    // one-third of the height of Game View
        this.position = new Point();
        this.random = new Random();
        random = new Random(System.currentTimeMillis());

        initializeObstacle();

    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public void run () {
        while (flag) {
            synchronized (activity.gameHandler) {
                while (activity.gamePause) {
                    try {
                        activity.gameHandler.wait();
                    } catch (InterruptedException e) {}
                }
            }
            // move the obstacle
            moveObstacle();

            try {
                Thread.sleep(sleepSpan);
            } catch (Exception e) {
                e.printStackTrace(); // error message
            }
        }
    }

    private void initializeObstacle() {
        float dr =  random.nextFloat();
        // only left and right for now
        if (dr <= 0.5) {
            direction = 1;  // left
        } else {
            direction = 2;  // right
        }
        speed = 5 + (int)(random.nextFloat() * 10.0f); // 5 ~ 15
        int col = (int)(random.nextFloat() * 5.0f);  // 0 ~ 5
        color = obsColor[col];
        float x = random.nextFloat();   // 0.0 ~ 1.0
        x *= xRangeOfObstacle;
        float y = random.nextFloat();   // 0.0 ~ 1.0
        y *= yRangeOfObstacle;
        position.set((int)x,(int)y);    // position of the center
    }

    private void moveObstacle() {
        int x = position.x;
        int y = position.y;
        if (direction == 1) {
            // left
            x -= speed;
            if (x<0) {
                x = 0;
                // left then change to right
                direction = 2;
            }
        } else if (direction == 2) {
            // right
            x += speed;
            if (x>xRangeOfObstacle) {
                x = xRangeOfObstacle;
                // right then chage to left
                direction = 1;
            }
        } else if (direction == 3) {
            // up
            y -= speed;
            if (y < 0) {
                y = 0;
                // up then change to down
                direction = 4;
            }
        } else {
            // down
            y += speed;
            if (y > yRangeOfObstacle) {
                y = yRangeOfObstacle;
                // down then change to up
                direction = 3;
            }
        }
        position.set(x,y);
    }

    public void drawObstacle(Canvas canvas) {

        int left = position.x - obstacleWidth / 2;
        int right = position.x + obstacleWidth / 2;
        int top = position.y - obstacleHeight / 2;
        int bottom = position.y + obstacleHeight / 2;

        Rect rect = new Rect(left, top, right, bottom);
        Paint paint = new Paint();

        paint.setColor(color);

        canvas.drawRect(rect,paint);
    }
}
