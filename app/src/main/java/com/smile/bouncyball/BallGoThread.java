package com.smile.bouncyball;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;

import com.smile.bouncyball.models.Banner;
import com.smile.bouncyball.models.BouncyBall;

import java.nio.charset.MalformedInputException;
import java.util.Random;

public class BallGoThread extends Thread{
    private MainActivity activity = null;
	private GameView gView = null;
	private int screenWidth = 0;
	private int screenHeight = 0;
    private int sleepSpan = 80;
    private boolean flag = true;
    private Random random = null;
    private int direction = 0; //  the direction of the ball
    private int score = 0;     //  score that user got
    private boolean isHitBanner = true;
    private int bottomY = 0;
    private BouncyBall bouncyBall = null;
    private Banner banner = null;

	public BallGoThread(GameView gView) {
		this.gView = gView;
        this.activity = gView.getActivity();
        this.screenWidth = gView.getScreenWidth();
        this.screenHeight = gView.getScreenHeight();
        this.sleepSpan  = gView.getSynchronizeTime();
        this.bottomY = gView.getBottomY();
        this.bouncyBall = gView.getBouncyBall();
        this.banner = gView.getBanner();

        random = new Random(System.currentTimeMillis());
        direction = random.nextInt(2)*3;  //   0 or 1  multiple 3 ------>0 or 3
	}
    
	public void run(){
        int tempX = 0;
        int tempY = 0;

		while(flag) {
            synchronized (activity.gameHandler) {
                while (activity.gamePause) {
                    try {
                        activity.gameHandler.wait();
                    } catch (InterruptedException e) {}
                }
            }

            tempX = bouncyBall.getBallX();
            tempY = bouncyBall.getBallY();
			switch(direction) {
                case 0:
                    // going to right top
                    bouncyBall.setBallX(bouncyBall.getBallX() + bouncyBall.getBallSpan());
                    bouncyBall.setBallY(bouncyBall.getBallY() - bouncyBall.getBallSpan());
                    // ballX = ballX + gView.ballSpan;
                    // ballY = ballY - gView.ballSpan;

                    if ((bouncyBall.getBallX()+bouncyBall.getBallRadius())>screenWidth) {
                        if ( (tempX>(screenWidth-bouncyBall.getBallRadius())) && (tempX<screenWidth) ) {
                            bouncyBall.setBallX(screenWidth - bouncyBall.getBallRadius());
                            // ballX = screenWidth - ballRadius;
                        } else {
                            // hit the right wall
                            direction = 3;
                        }
                    } else if ((bouncyBall.getBallY()-bouncyBall.getBallRadius())<0) {
                        if ( (tempY<bouncyBall.getBallRadius()) && (tempY>0) ){
                            bouncyBall.setBallY(bouncyBall.getBallRadius());
                            // ballY = ballRadius;
                        } else {
                            // hit the top wall
                            direction = 1;
                        }
                    }
                    break;
                case 3:
                    // going to left top
                    bouncyBall.setBallX(bouncyBall.getBallX() - bouncyBall.getBallSpan());
                    // ballX = ballX - gView.ballSpan;
                    bouncyBall.setBallY(bouncyBall.getBallY() - bouncyBall.getBallSpan());
                    // ballY = ballY - gView.ballSpan;
                    if ((bouncyBall.getBallX() - bouncyBall.getBallRadius())<0) {
                        if ( (tempX<bouncyBall.getBallRadius()) && (tempX>0) ) {
                            bouncyBall.setBallX(bouncyBall.getBallRadius());
                            // ballX = ballRadius;
                        } else {
                            // hit the left wall
                            direction = 0;
                        }
				    } else if ((bouncyBall.getBallY()-bouncyBall.getBallRadius())<0) {
                        if ( (tempY<bouncyBall.getBallRadius()) && (tempY>0) ){
                            bouncyBall.setBallY(bouncyBall.getBallRadius());
                            // ballY = ballRadius;
                        } else {
                            // hit the top wall
                            direction = 2;
                        }
				    }
				    break;
				case 1:
				    // going to right bottom
                    bouncyBall.setBallX(bouncyBall.getBallX() + bouncyBall.getBallSpan());
				    // ballX = ballX + gView.ballSpan;
                    bouncyBall.setBallY(bouncyBall.getBallY() + bouncyBall.getBallSpan());
				    // ballY = ballY + gView.ballSpan;
				  
				    if ((bouncyBall.getBallY()+bouncyBall.getBallRadius())>bottomY) {
                        if ( (tempY>(bottomY-bouncyBall.getBallRadius())) && (tempY<bottomY) ) {
                            bouncyBall.setBallY(bottomY - bouncyBall.getBallRadius());
                            // ballY = bottomY - ballRadius;
                        } else {
                            // hit the bottom wall
                            checkCollision(1);
                        }
				    } else if ((bouncyBall.getBallX()+bouncyBall.getBallRadius())>screenWidth) {
                        if ( (tempX>(screenWidth-bouncyBall.getBallRadius())) && (tempX<screenWidth) ) {
                            bouncyBall.setBallX(screenWidth - bouncyBall.getBallRadius());
                            // ballX = screenWidth - ballRadius;
                        } else {
                            //hit the right wall
                            direction = 2;
                        }
				    }
				    break;
				case 2:
				    // going to left bottom
                    bouncyBall.setBallX(bouncyBall.getBallX() - bouncyBall.getBallSpan());
				    // ballX = ballX - gView.ballSpan;
                    bouncyBall.setBallY(bouncyBall.getBallY() + bouncyBall.getBallSpan());
				    // ballY = ballY + gView.ballSpan;
				  
				    if ((bouncyBall.getBallY()+bouncyBall.getBallRadius())>bottomY) {
                        if ( (tempY>(bottomY-bouncyBall.getBallRadius())) && (tempY<bottomY) )  {
                            bouncyBall.setBallY(bottomY - bouncyBall.getBallRadius());
                            // ballY = bottomY - ballRadius;
                        } else {
                            // hit the bottom wall
                            checkCollision(2);
                        }
				    } else if ((bouncyBall.getBallX()-bouncyBall.getBallRadius())<0) {
                        if ( (tempX<bouncyBall.getBallRadius()) && (tempX>0) ) {
                            bouncyBall.setBallX(bouncyBall.getBallRadius());
                            // ballX = ballRadius;
                        } else {
                            // hit the left wall
                            direction = 1;
                        }
				    }
				    break;
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
                    this.direction = 0;
                    break;
                case 2:
                    this.direction = 3;
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
    public void setFlag(boolean flag) {
        this.flag = flag;
    }
    public int getScore() {
	    return this.score;
    }
    public boolean getHitBanner() {
	    return this.isHitBanner;
    }

}