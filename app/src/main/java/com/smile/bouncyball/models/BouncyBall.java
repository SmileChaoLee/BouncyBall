package com.smile.bouncyball.models;

import android.graphics.Bitmap;

/**
 * Created by chaolee on 2017-11-15.
 */

public class BouncyBall {
    private int ballX;       //  coordinate (x-axis) of the ball
    private int ballY;       //  coordinate (y-axis) of the ball
    private int ballSize = 16;          // size of the ball
    private int ballRadius = ballSize/2;
    private int ballSpan = 8;           // speed of the ball
    private int direction = 0;
    private Bitmap bitmap = null;

    public BouncyBall(int ballX, int ballY, int ballSize, int ballSpan, Bitmap bitmap) {
        this.ballX = ballX;
        this.ballY = ballY;
        this.ballSize = ballSize;
        this.ballRadius = this.ballSize / 2;
        this.ballSpan = ballSpan;
        this.bitmap = bitmap;
    }

    public int getBallX() {
        return this.ballX;
    }
    public void setBallX(int ballX) {
        this.ballX = ballX;
    }
    public int getBallY() {
        return this.ballY;
    }
    public void setBallY(int ballY) {
        this.ballY = ballY;
    }
    public int getBallSize() {
        return this.ballSize;
    }
    public void setBallSize(int ballSize) {
        this.ballSize = ballSize;
        this.ballRadius = this.ballSize / 2;
    }
    public int getBallRadius() {
        return this.ballRadius;
    }
    public int getBallSpan() {
        return this.ballSpan;
    }
    public void setBallSpan(int ballSpan) {
        this.ballSpan = ballSpan;
    }
    public int getDirection() {
        return this.direction;
    }
    public void setDirection(int direction) {
        this.direction = direction;
    }
    public Bitmap getBitmap() {
        return this.bitmap;
    }
    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
