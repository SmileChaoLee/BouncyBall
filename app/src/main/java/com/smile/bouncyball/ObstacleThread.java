package com.smile.bouncyball;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;

import com.smile.bouncyball.models.Banner;
import com.smile.bouncyball.models.BouncyBall;

import java.util.Random;

/**
 * Created by chaolee on 2017-11-14.
 */

public class ObstacleThread extends Thread{

    private final int[] obsColor = {Color.BLACK, Color.DKGRAY, Color.RED, Color.GREEN, Color.MAGENTA,Color.CYAN};
    private int obstacleWidth = 100;
    private int obstacleHeight = 20;

    private MainActivity activity = null;
    private GameView gameView = null;
    private BallGoThread ballGoThread = null;

    private boolean keepRunning = true; // keepRunning = true -> loop in run() still going
    private int direction = 1;  // 1->left, 2->right, 3->up, 4->down
    private int speed = 0;  // no moving, moving speed (left, right, up, or down)
    private int color = Color.BLACK;    // the color of obstacle
    private Point position = null;  // the position of the center of this obstacle

    private int xRangeOfObstacle = 0;
    private int yRangeOfObstacle = 0;

    private BouncyBall bouncyBall = null;
    private Banner banner = null;

    private Random random = null;

    public ObstacleThread(GameView gView, int stageNo) {

        this.gameView = gView;
        this.activity = gameView.getActivity();
        this.ballGoThread = gameView.getBallGoThread();
        if (this.ballGoThread == null) {
            // must not be null
            throw new NullPointerException("ballGoThread must not be null.");
        }
        this.xRangeOfObstacle = gameView.getScreenWidth();
        this.yRangeOfObstacle = gameView.getScreenHeight() / 3;    // one-third of the height of Game View
        this.bouncyBall = gameView.getBouncyBall();
        this.obstacleHeight = bouncyBall.getBallRadius();
        this.banner = gameView.getBanner();
        this.obstacleWidth = banner.getBannerWidth();

        this.position = new Point();
        this.random = new Random();
        random = new Random(System.currentTimeMillis());

        initializeObstacle(stageNo);

    }

    public void setKeepRunning(boolean keepRunning) {
        this.keepRunning = keepRunning;
    }
    public Point getPosition() {
        return this.position;
    }
    public int getObstacleWidth() {
        return this.obstacleWidth;
    }
    public int getObstacleHeight() {
        return this.obstacleHeight;
    }

    public void run () {
        while (keepRunning) {
            synchronized (activity.activityHandler) {
                // for application's (Main activity) synchronizing
                while (activity.gamePause) {
                    try {
                        activity.activityHandler.wait();
                    } catch (InterruptedException ex) {}
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
                    // move the obstacle
                    moveObstacle();
                    // isHitBouncyBall();   // moved to BallGoThread on 2017-11-19
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void initializeObstacle(int stageNo) {
        float dr = random.nextFloat();
        // only left and right for now
        if (dr <= 0.5) {
            direction = 1;  // left
        } else {
            direction = 2;  // right
        }
        speed = 5 + (int) (random.nextFloat() * 10.0f); // 5 ~ 15
        int col = (int) (random.nextFloat() * 5.0f);  // 0 ~ 5
        color = obsColor[col];
        float x = random.nextFloat();   // 0.0 ~ 1.0
        x *= xRangeOfObstacle;
        float y = bouncyBall.getBallSize() * stageNo * 2;
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

    private boolean isHitBouncyBall() { // no longer used, it has been moved to BallGoThread that named isHitObstacle()

        int ballCenterX = bouncyBall.getBallX();
        int ballCenterY = bouncyBall.getBallY();
        int radius = bouncyBall.getBallRadius();

        int ballLeft = ballCenterX - radius;
        int ballRight = ballCenterX + radius;
        int ballTop = ballCenterY - radius;
        int ballBottom = ballCenterY + radius;

        int obstacleLeft = position.x - obstacleWidth/2;
        int obstacleRight = position.x + obstacleWidth/2;
        int obstacleTop = position.y - obstacleHeight/2;
        int obstacleBottom = position.y + obstacleHeight/2;

        boolean isHit = false;
        if ( (ballRight >= obstacleLeft) && (ballLeft <= obstacleRight) ) {
            // center point is inside the range of the obstacle
            int ballDirection = bouncyBall.getDirection();
            switch (ballDirection) {
                case GameView.BouncyBall_RIGHT_TOP:
                    if ((ballTop >= obstacleTop) && (ballTop <= obstacleBottom)) {
                        // hit
                        bouncyBall.setDirection(GameView.BouncyBall_RIGHT_BOTTOM);
                        bouncyBall.setBallY(obstacleBottom + radius);
                        isHit = true;
                    }
                    break;
                case GameView.BouncyBall_LEFT_TOP:
                    if ((ballTop >= obstacleTop) && (ballTop <= obstacleBottom)) {
                        // hit
                        bouncyBall.setDirection(GameView.BouncyBall_LEFT_BOTTOM);
                        bouncyBall.setBallY(obstacleBottom + radius);
                        isHit = true;
                    }
                    break;
                case GameView.BouncyBall_RIGHT_BOTTOM:
                    if ((ballBottom >= obstacleTop) && (ballBottom <= obstacleBottom)) {
                        bouncyBall.setDirection(GameView.BouncyBall_RIGHT_TOP);
                        bouncyBall.setBallY(obstacleTop - radius);
                        isHit = true;
                    }
                    break;
                case GameView.BouncyBall_LEFT_BOTTOM:
                    if ((ballBottom >= obstacleTop) && (ballBottom <= obstacleBottom)) {
                        bouncyBall.setDirection(GameView.BouncyBall_LEFT_TOP);
                        bouncyBall.setBallY(obstacleTop - radius);
                        isHit = true;
                    }
                    break;
            }
        }

        return isHit;
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
