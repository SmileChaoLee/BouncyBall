package com.smile.bouncyball;

import android.os.SystemClock;

import com.smile.bouncyball.models.Banner;
import com.smile.bouncyball.tools.LogUtil;

/**
 * Created by Chao Lee on 2017-11-18.
 */

public class ButtonHoldThread extends Thread {
    private final static String TAG = "ButtonHoldThread";

    private GameView gameView = null;
    private volatile boolean keepRunning = true;
    private Banner banner = null;
    private volatile boolean isButtonHold = false;
    private int bannerMoveSpeed = 0;

    public ButtonHoldThread(GameView gameView) {
        this.gameView = gameView;
        this.banner = this.gameView.getBanner();
        this.keepRunning = true;
        this.isButtonHold = false;
        this.bannerMoveSpeed = 0;
    }

    public void setKeepRunning(boolean keepRunning) {
        this.keepRunning = keepRunning;
    }
    public void setIsButtonHold(boolean isButtonHold) {
        this.isButtonHold = isButtonHold;
    }
    public void setBannerMoveSpeed(int bannerMoveSpeed) {
        this.bannerMoveSpeed = bannerMoveSpeed;
    }

    public void run() {
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
            // do the work of holding button
            while (isButtonHold) {
                int bannerX = banner.getBannerX();
                bannerX += bannerMoveSpeed;
                if (bannerX < 0) {
                    bannerX = 0;
                }
                if (bannerX > gameView.getGameViewWidth()) {
                    bannerX = gameView.getGameViewWidth();
                }
                // set position of banner
                banner.setBannerX(bannerX);
                SystemClock.sleep(20);
            }
            SystemClock.sleep(2);
        }
    }
}
