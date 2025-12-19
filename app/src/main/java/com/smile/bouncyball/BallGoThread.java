package com.smile.bouncyball;

import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.SystemClock;

import com.smile.bouncyball.models.Banner;
import com.smile.bouncyball.models.BouncyBall;
import com.smile.bouncyball.tools.LogUtil;

import java.util.Random;
import java.util.Vector;

public class BallGoThread extends Thread {
    private final static String TAG = "BallGoThread";
	private GameView gameView = null;
	private Vector<ObstacleThread> obstacleThreads = null;
	private int gameViewWidth = 0;
    private int synchronizeTime = 70;
    private boolean flag = true;        // flag = true -> move ball
    // keepRunning = true -> loop in run() still going
    private volatile boolean keepRunning = true;
    private Random random = null;
    private int bottomY = 0;
    private BouncyBall bouncyBall = null;
    private Banner banner = null;
    private final int[] stageScore = {0, 10, 30, 60,100};    // hits for each stage
    private int score = 0;     //  score that user got
    private int status = GameView.START_STATUS;

	public BallGoThread(GameView gView) {

		gameView = gView;
        obstacleThreads = gameView.getObstacleThreads();
        // obstacleThreads must not be null
        if (obstacleThreads == null) {
            throw new NullPointerException("obstacleThreads must not be null.");
        }

        gameViewWidth = gView.getGameViewWidth();
        synchronizeTime  = gView.synchronizeTime;
        bottomY = gView.getBottomY();
        bouncyBall = gView.getBouncyBall();
        banner = gView.getBanner();

        random = new Random(System.currentTimeMillis());
        int direction = random.nextInt(2)*3;  //   0 or 1  multiple 3 ------>0 or 3
        bouncyBall.setDirection(direction);    // direction of bouncy ball

        score = 0;
        status = GameView.START_STATUS;
	}

    public void setKeepRunning(boolean keepRunning) {
        this.keepRunning = keepRunning;
    }
    public boolean getFlag() {
        return this.flag;
    }
    public void setFlag(boolean flag) {
        this.flag = flag;
    }
    public int getScore() {
        return this.score;
    }
    public int getStatus() {
	    return this.status;
    }

	public void run() {
	    status = GameView.FIRST_STAGE; // start running is first stage
		while(keepRunning) {
            synchronized (gameView.mainLock) {
                // for application's (Main activity) synchronizing
                while (!gameView.isGameVisible) {
                    try {
                        gameView.mainLock.wait();
                    } catch (InterruptedException ex) {
                        LogUtil.e(TAG, "run.mainLock.InterruptedException", ex);
                    }
                }
            }
            synchronized (gameView.gameLock) {
                // for GameView's synchronizing
                while (gameView.isPausedByUser) {
                    try {
                        gameView.gameLock.wait();
                    } catch (InterruptedException ex) {
                        LogUtil.e(TAG, "run.gameLock.InterruptedException", ex);
                    }
                }
            }
            if (flag) {
                // 2017-11-19
                checkCollision();   // collision with banner or walls
                if ( (status == GameView.FAILED_STATUS) || (status == GameView.FINISHED_STATUS) ) {
                    // failed or reach highest score (finished)
                    // stop running this thread, means set keepRunning to false;
                    keepRunning = false;
                } else {
                    // 2017-11-19 morning
                    for (ObstacleThread obstacleThread : obstacleThreads) {
                        // obstacleThread.isHitBouncyBall();    // removed on 2017-11-19
                        isHitObstacle(obstacleThread);
                    }
                }
            }
            SystemClock.sleep(synchronizeTime);
		}
	}

	private void checkCollision() {

        int tempX = bouncyBall.getBallX();
        int tempY = bouncyBall.getBallY();
        int direction = bouncyBall.getDirection();
        switch (direction) {
            case GameView.BB_RIGHT_TOP:
                // going to right top
                bouncyBall.setBallX(tempX + bouncyBall.getBallSpan());
                bouncyBall.setBallY(tempY - bouncyBall.getBallSpan());
                // ballX = ballX + gView.ballSpan;
                // ballY = ballY - gView.ballSpan;

                if ((bouncyBall.getBallX() + bouncyBall.getBallRadius()) > gameViewWidth) {
                    if ((tempX > (gameViewWidth - bouncyBall.getBallRadius())) && (tempX < gameViewWidth)) {
                        bouncyBall.setBallX(gameViewWidth - bouncyBall.getBallRadius());
                        // ballX = gameViewWidth - ballRadius;
                    } else {
                        // hit the right wall
                        bouncyBall.setDirection(GameView.BB_LEFT_TOP);
                    }
                } else if ((bouncyBall.getBallY() - bouncyBall.getBallRadius()) < 0) {
                    if ((tempY < bouncyBall.getBallRadius()) && (tempY > 0)) {
                        bouncyBall.setBallY(bouncyBall.getBallRadius());
                        // ballY = ballRadius;
                    } else {
                        // hit the top wall
                        bouncyBall.setDirection(GameView.BB_RIGHT_BOTTOM);
                    }
                }
                break;
            case GameView.BB_LEFT_TOP:
                // going to left top
                bouncyBall.setBallX(tempX - bouncyBall.getBallSpan());
                // ballX = ballX - gView.ballSpan;
                bouncyBall.setBallY(tempY - bouncyBall.getBallSpan());
                // ballY = ballY - gView.ballSpan;
                if ((bouncyBall.getBallX() - bouncyBall.getBallRadius()) < 0) {
                    if ((tempX < bouncyBall.getBallRadius()) && (tempX > 0)) {
                        bouncyBall.setBallX(bouncyBall.getBallRadius());
                        // ballX = ballRadius;
                    } else {
                        // hit the left wall
                        bouncyBall.setDirection(GameView.BB_RIGHT_TOP);
                    }
                } else if ((bouncyBall.getBallY() - bouncyBall.getBallRadius()) < 0) {
                    if ((tempY < bouncyBall.getBallRadius()) && (tempY > 0)) {
                        bouncyBall.setBallY(bouncyBall.getBallRadius());
                        // ballY = ballRadius;
                    } else {
                        // hit the top wall
                        bouncyBall.setDirection(GameView.BB_LEFT_BOTTOM);
                    }
                }
                break;
            case GameView.BB_RIGHT_BOTTOM:
                // going to right bottom
                bouncyBall.setBallX(tempX + bouncyBall.getBallSpan());
                // ballX = ballX + gView.ballSpan;
                bouncyBall.setBallY(tempY + bouncyBall.getBallSpan());
                // ballY = ballY + gView.ballSpan;

                if ((bouncyBall.getBallY() + bouncyBall.getBallRadius()) > bottomY) {
                    if ((tempY > (bottomY - bouncyBall.getBallRadius())) && (tempY < bottomY)) {
                        bouncyBall.setBallY(bottomY - bouncyBall.getBallRadius());
                        // ballY = bottomY - ballRadius;
                    } else {
                        // hit the bottom wall
                        checkHitBanner(GameView.BB_RIGHT_BOTTOM);
                    }
                } else if ((bouncyBall.getBallX() + bouncyBall.getBallRadius()) > gameViewWidth) {
                    if ((tempX > (gameViewWidth - bouncyBall.getBallRadius())) && (tempX < gameViewWidth)) {
                        bouncyBall.setBallX(gameViewWidth - bouncyBall.getBallRadius());
                        // ballX = gameViewWidth - ballRadius;
                    } else {
                        //hit the right wall
                        bouncyBall.setDirection(GameView.BB_LEFT_BOTTOM);
                    }
                }
                break;
            case GameView.BB_LEFT_BOTTOM:
                // going to left bottom
                bouncyBall.setBallX(tempX - bouncyBall.getBallSpan());
                // ballX = ballX - gView.ballSpan;
                bouncyBall.setBallY(tempY + bouncyBall.getBallSpan());
                // ballY = ballY + gView.ballSpan;

                if ((bouncyBall.getBallY() + bouncyBall.getBallRadius()) > bottomY) {
                    if ((tempY > (bottomY - bouncyBall.getBallRadius())) && (tempY < bottomY)) {
                        bouncyBall.setBallY(bottomY - bouncyBall.getBallRadius());
                        // ballY = bottomY - ballRadius;
                    } else {
                        // hit the bottom wall
                        checkHitBanner(GameView.BB_LEFT_BOTTOM);
                    }
                } else if ((bouncyBall.getBallX() - bouncyBall.getBallRadius()) < 0) {
                    if ((tempX < bouncyBall.getBallRadius()) && (tempX > 0)) {
                        bouncyBall.setBallX(bouncyBall.getBallRadius());
                        // ballX = ballRadius;
                    } else {
                        // hit the left wall
                        bouncyBall.setDirection(GameView.BB_RIGHT_BOTTOM);
                    }
                }
                break;
        }
    }

    private boolean checkHitBanner(int direction) {

        boolean isHit = false;

        int bannerX1, bannerX2;
        bannerX1 = banner.getBannerX() - banner.getBannerWidth()/2;
        bannerX2 = banner.getBannerX() + banner.getBannerWidth()/2;
        // (gView.bannerX,gView.bannerY) is the center of the banner
        if ( ((bouncyBall.getBallX()+bouncyBall.getBallRadius())>=bannerX1) && ((bouncyBall.getBallX()-bouncyBall.getBallRadius())<=bannerX2) ) {
            // hit the banner
            switch(direction){
                case 1:
                    bouncyBall.setDirection(GameView.BB_RIGHT_TOP);
                    break;
                case 2:
                    bouncyBall.setDirection(GameView.BB_LEFT_TOP);
                    break;
            }
            // score policy: add one score when it hit the banner. Added on 2017-11-07
            score++;
            isHit = true;
        }

        checkStatus(isHit);

        return isHit;
    }

    private void checkStatus(boolean isHit) {

	    if (!isHit) {
	        status = GameView.FAILED_STATUS;
	        return;
        }

        // maximum value of the number that banner is to be hit to make user win
        // -1-> failed and game over, 0->waiting to start, 1->first stage (playing), 2->second stage (playing)
        // 3->final stage (playing), 4-finished the game
        int highest = 999;
        if (score < highest) {
	        // has not reach highest score yet
            if (status < GameView.FINAL_STAGE) {
                if (score >= stageScore[status]) {
                    status++;
                    if (status > GameView.FINAL_STAGE) {
                        // max stage is 4 (stage no is greater 4
                        status = GameView.FINAL_STAGE;  // 4, final stage
                    } else {
                        // status <= finalStageStatus
                        synchronizeTime -= 10;    // speed up by 10 pixels
                    }
                }
            }
        } else {
            // reached highest score then finished
            status = GameView.FINISHED_STATUS;
        }
    }

    private boolean isHitObstacle(ObstacleThread obstacleThread) {

        int ballCenterX = bouncyBall.getBallX();
        int ballCenterY = bouncyBall.getBallY();
        int radius = bouncyBall.getBallRadius();

        int ballLeft = ballCenterX - radius;
        int ballRight = ballCenterX + radius;
        int ballTop = ballCenterY - radius;
        int ballBottom = ballCenterY + radius;

        Point position = obstacleThread.getPosition();
        int obstacleLeft = position.x - obstacleThread.getObstacleWidth()/2;
        int obstacleRight = position.x + obstacleThread.getObstacleWidth()/2;
        int obstacleTop = position.y - obstacleThread.getObstacleHeight()/2;
        int obstacleBottom = position.y + obstacleThread.getObstacleHeight()/2;

        boolean isHit = false;
        if ( (ballRight >= obstacleLeft) && (ballLeft <= obstacleRight) ) {
            // center point is inside the range of the obstacle
            int ballDirection = bouncyBall.getDirection();
            switch (ballDirection) {
                case GameView.BB_RIGHT_TOP:
                    if ((ballTop >= obstacleTop) && (ballTop <= obstacleBottom)) {
                        // hit
                        bouncyBall.setDirection(GameView.BB_RIGHT_BOTTOM);
                        bouncyBall.setBallY(obstacleBottom + radius);
                        isHit = true;
                    }
                    break;
                case GameView.BB_LEFT_TOP:
                    if ((ballTop >= obstacleTop) && (ballTop <= obstacleBottom)) {
                        // hit
                        bouncyBall.setDirection(GameView.BB_LEFT_BOTTOM);
                        bouncyBall.setBallY(obstacleBottom + radius);
                        isHit = true;
                    }
                    break;
                case GameView.BB_RIGHT_BOTTOM:
                    if ((ballBottom >= obstacleTop) && (ballBottom <= obstacleBottom)) {
                        bouncyBall.setDirection(GameView.BB_RIGHT_TOP);
                        bouncyBall.setBallY(obstacleTop - radius);
                        isHit = true;
                    }
                    break;
                case GameView.BB_LEFT_BOTTOM:
                    if ((ballBottom >= obstacleTop) && (ballBottom <= obstacleBottom)) {
                        bouncyBall.setDirection(GameView.BB_LEFT_TOP);
                        bouncyBall.setBallY(obstacleTop - radius);
                        isHit = true;
                    }
                    break;
            }
        }

        return isHit;
    }

	public void drawBouncyBall(Canvas canvas) {

        // draw the ball
        int tempX = bouncyBall.getBallX() - bouncyBall.getBallRadius();
        if (tempX<0) {
            tempX = 0;
            bouncyBall.setBallX(tempX + bouncyBall.getBallRadius());
            // ballX = tempX + ballRadius;
        }
        int tempY = bouncyBall.getBallY() - bouncyBall.getBallRadius();
        if (tempY<0) {
            tempY = 0;
            bouncyBall.setBallY(tempY + bouncyBall.getBallRadius());
            // ballY = tempY + ballRadius;
        }
        Point sPoint = new Point(tempX,tempY);

        tempX = sPoint.x + bouncyBall.getBallSize();
        if (tempX>gameViewWidth) {
            tempX = gameViewWidth;
            sPoint.x = tempX - bouncyBall.getBallSize();
            bouncyBall.setBallX(tempX - bouncyBall.getBallRadius());
        }
        tempY = sPoint.y + bouncyBall.getBallSize();
        if (tempY>bottomY) {
            tempY = bottomY;
            sPoint.y = tempY - bouncyBall.getBallSize();
            bouncyBall.setBallY(tempY - bouncyBall.getBallRadius());
            // ballY = tempY - ballRadius;
        }
        // draw the bouncy ball
        Rect rect2 = new Rect(sPoint.x,sPoint.y,tempX,tempY);
        canvas.drawBitmap(bouncyBall.getBitmap(), null ,rect2, null);
        //

    }

}