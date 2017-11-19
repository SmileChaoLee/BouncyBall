package com.smile.bouncyball.models;

import com.smile.bouncyball.BallGoThread;
import com.smile.bouncyball.GameView;
import com.smile.bouncyball.MainActivity;

/**
 * Created by chaolee on 2017-11-18.
 */

public class ButtonHoldThread extends Thread {

    private GameView gameView = null;
    private MainActivity activity = null;
    private boolean keepRunning = true;
    private Banner banner = null;
    private boolean isButtonHold = false;
    private int bannerMoveSpeed = 0;

    public ButtonHoldThread(GameView gameView) {
        this.gameView = gameView;
        this.activity = this.gameView.getActivity();
        this.banner = this.gameView.getBanner();
        this.keepRunning = true;
        this.isButtonHold = false;
        this.bannerMoveSpeed = bannerMoveSpeed;
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
        while (keepRunning) {
            synchronized (activity.activityHandler) {
                // for application's (Main activity) synchronizing
                while (activity.gamePause) {
                    try {
                        activity.activityHandler.wait();
                    } catch (InterruptedException e) {
                    }
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

            // do the work of holding button
            while (isButtonHold) {
                int bannerX = banner.getBannerX();
                bannerX += bannerMoveSpeed;
                if (bannerX < 0) {
                    bannerX = 0;
                }
                if (bannerX > gameView.getScreenWidth()) {
                    bannerX = gameView.getScreenWidth();
                }
                // set position of banner
                banner.setBannerX(bannerX);
                try{Thread.sleep(10);}
                catch(Exception e){e.printStackTrace();}
            }

        }
    }
}
