package com.smile.bouncyball;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Looper;

import com.smile.bouncyball.models.Banner;
import com.smile.bouncyball.models.BouncyBall;

import java.util.Random;
import android.os.Handler;

public class BallGoThread extends Thread{
    private MainActivity activity = null;
	private GameView gameView = null;
	private int screenWidth = 0;
	private int screenHeight = 0;
    private int sleepSpan = 80;
    private boolean flag = true;
    private boolean keepRunning = true;
    private Random random = null;
    private int score = 0;     //  score that user got
    private boolean isHitBanner = true;
    private int bottomY = 0;
    private BouncyBall bouncyBall = null;
    private Banner banner = null;

	public BallGoThread(GameView gView) {

		this.gameView = gView;
        this.activity = gView.getActivity();
        this.screenWidth = gView.getScreenWidth();
        this.screenHeight = gView.getScreenHeight();
        this.sleepSpan  = gView.getSynchronizeTime();
        this.bottomY = gView.getBottomY();
        this.bouncyBall = gView.getBouncyBall();
        this.banner = gView.getBanner();

        random = new Random(System.currentTimeMillis());
        int direction = random.nextInt(2)*3;  //   0 or 1  multiple 3 ------>0 or 3
        bouncyBall.setDirection(direction);    // direction of bouncy ball

	}

	public void run(){
        int tempX = 0;
        int tempY = 0;

		while(flag) {
            synchronized (activity.activityHandler) {
                while (activity.gamePause) {
                    try {
                        activity.activityHandler.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }

            synchronized (this) {
                if (keepRunning) {
                    tempX = bouncyBall.getBallX();
                    tempY = bouncyBall.getBallY();
                    int direction = bouncyBall.getDirection();
                    switch (direction) {
                        case GameView.BouncyBall_RIGHT_TOP:
                            // going to right top
                            bouncyBall.setBallX(tempX + bouncyBall.getBallSpan());
                            bouncyBall.setBallY(tempY - bouncyBall.getBallSpan());
                            // ballX = ballX + gView.ballSpan;
                            // ballY = ballY - gView.ballSpan;

                            if ((bouncyBall.getBallX() + bouncyBall.getBallRadius()) > screenWidth) {
                                if ((tempX > (screenWidth - bouncyBall.getBallRadius())) && (tempX < screenWidth)) {
                                    bouncyBall.setBallX(screenWidth - bouncyBall.getBallRadius());
                                    // ballX = screenWidth - ballRadius;
                                } else {
                                    // hit the right wall
                                    bouncyBall.setDirection(GameView.BouncyBall_LEFT_TOP);
                                }
                            } else if ((bouncyBall.getBallY() - bouncyBall.getBallRadius()) < 0) {
                                if ((tempY < bouncyBall.getBallRadius()) && (tempY > 0)) {
                                    bouncyBall.setBallY(bouncyBall.getBallRadius());
                                    // ballY = ballRadius;
                                } else {
                                    // hit the top wall
                                    bouncyBall.setDirection(GameView.BouncyBall_RIGHT_BOTTOM);
                                }
                            }
                            break;
                        case GameView.BouncyBall_LEFT_TOP:
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
                                    bouncyBall.setDirection(GameView.BouncyBall_RIGHT_TOP);
                                }
                            } else if ((bouncyBall.getBallY() - bouncyBall.getBallRadius()) < 0) {
                                if ((tempY < bouncyBall.getBallRadius()) && (tempY > 0)) {
                                    bouncyBall.setBallY(bouncyBall.getBallRadius());
                                    // ballY = ballRadius;
                                } else {
                                    // hit the top wall
                                    bouncyBall.setDirection(GameView.BouncyBall_LEFT_BOTTOM);
                                }
                            }
                            break;
                        case GameView.BouncyBall_RIGHT_BOTTOM:
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
                                    checkCollision(GameView.BouncyBall_RIGHT_BOTTOM);
                                }
                            } else if ((bouncyBall.getBallX() + bouncyBall.getBallRadius()) > screenWidth) {
                                if ((tempX > (screenWidth - bouncyBall.getBallRadius())) && (tempX < screenWidth)) {
                                    bouncyBall.setBallX(screenWidth - bouncyBall.getBallRadius());
                                    // ballX = screenWidth - ballRadius;
                                } else {
                                    //hit the right wall
                                    bouncyBall.setDirection(GameView.BouncyBall_LEFT_BOTTOM);
                                }
                            }
                            break;
                        case GameView.BouncyBall_LEFT_BOTTOM:
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
                                    checkCollision(GameView.BouncyBall_LEFT_BOTTOM);
                                }
                            } else if ((bouncyBall.getBallX() - bouncyBall.getBallRadius()) < 0) {
                                if ((tempX < bouncyBall.getBallRadius()) && (tempX > 0)) {
                                    bouncyBall.setBallX(bouncyBall.getBallRadius());
                                    // ballX = ballRadius;
                                } else {
                                    // hit the left wall
                                    bouncyBall.setDirection(GameView.BouncyBall_RIGHT_BOTTOM);
                                }
                            }
                            break;
                    }
                }
                notifyAll();
            }

            try{Thread.sleep(sleepSpan);}
            catch(Exception e){e.printStackTrace();}
		}
	}

	public void checkCollision(int direction) {
        int bannerX1, bannerX2;
        bannerX1 = banner.getBannerX() - banner.getBannerWidth()/2;
        bannerX2 = banner.getBannerX() + banner.getBannerWidth()/2;

        // (gView.bannerX,gView.bannerY) is the center of the banner
	  	if ( ((bouncyBall.getBallX()+bouncyBall.getBallRadius())>=bannerX1) && ((bouncyBall.getBallX()-bouncyBall.getBallRadius())<=bannerX2) ) {
            // hit the banner
            switch(direction){
                case 1:
                    bouncyBall.setDirection(GameView.BouncyBall_RIGHT_TOP);
                    break;
                case 2:
                    bouncyBall.setDirection(GameView.BouncyBall_LEFT_TOP);
                    break;
	  		}
            // score policy: add one score before it hit the banner. Added on 2017-11-07
	  		score++;
            isHitBanner = true;
	  	} else {
	  	    // did not hit the banner, means failed
	  		isHitBanner = false;
	  	}		
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
        if (tempX>screenWidth) {
            tempX = screenWidth;
            sPoint.x = tempX - bouncyBall.getBallSize();
            bouncyBall.setBallX(tempX - bouncyBall.getBallRadius());
            // ballX = tempX - ballRadius;
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

    public boolean getFlag() {
        return this.flag;
    }
    public void setKeepRunning(boolean keepRunning) {
	    this.keepRunning = keepRunning;
    }
    public void setFlag(boolean flag) {
        this.flag = flag;
    }
    public int getScore() {
	    return this.score;
    }
    public boolean getHitBanner() {
	    return this.isHitBanner;
    }
    public int getSleepSpan() {
	    return this.sleepSpan;
    }
    public void setSleepSpan(int sleepSpan) {
	    this.sleepSpan = sleepSpan;
    }

}