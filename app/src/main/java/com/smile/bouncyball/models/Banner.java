package com.smile.bouncyball.models;

import android.graphics.Bitmap;

/**
 * Created by Chao Lee on 2017-11-15.
 */

public class Banner {
    private int bannerX;     //  the coordinate (x-axis) of the banner
    private int bannerY;     //  the coordinate (y-axis) of the banner
    private int bannerWidth;       // width of the banner
    private int bannerHeight;       // height of the banner
    private Bitmap bitmap;

    public Banner(int bannerX, int bannerY, int bannerWidth, int bannerHeight, Bitmap bitmap) {
        this.bannerX = bannerX;
        this.bannerY = bannerY;
        this.bannerWidth = bannerWidth;
        this.bannerHeight = bannerHeight;
        this.bitmap = bitmap;
    }

    public int getBannerX() {
        return this.bannerX;
    }
    public void setBannerX(int bannerX) {
        this.bannerX = bannerX;
    }
    public int getBannerY() {
        return this.bannerY;
    }
    public void setBannerY(int bannerY) {
        this.bannerY = bannerY;
    }
    public int getBannerWidth() {
        return this.bannerWidth;
    }
    public void setBannerWidth(int bannerWidth) {
        this.bannerWidth = bannerWidth;
    }
    public int getBannerHeight() {
        return this.bannerHeight;
    }
    public void setBannerHeight(int bannerHeight) {
        this.bannerHeight = bannerHeight;
    }
    public Bitmap getBitmap() {
        return this.bitmap;
    }
    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
