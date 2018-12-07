package com.smile.bouncyball;

import com.smile.bouncyball.GameView;
import com.smile.bouncyball.MainActivity;
import com.smile.bouncyball.models.Banner;

/**
 * Created by chaolee on 2017-11-18.
 */

public class ButtonHoldThread extends Thread {

    private GameView gameView = null;
    private MainActivity mainActivity = null;
    private boolean keepRunning = true;
    private Banner banner = null;
    private boolean isButtonHold = false;
    private int bannerMoveSpeed = 0;

    public ButtonHoldThread(GameView gameView) {
        this.gameView = gameView;
        this.mainActivity = this.gameView.getMainActivity();
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
        while (keepRunning) {
            synchronized (mainActivity.activityHandler) {
                // for application's (Main activity) synchronizing
                while (mainActivity.gamePause) {
                    try {
                        mainActivity.activityHandler.wait();
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
                if (bannerX > gameView.getGameViewWidth()) {
                    bannerX = gameView.getGameViewWidth();
                }
                // set position of banner
                banner.setBannerX(bannerX);
                try{Thread.sleep(20);}
                catch(Exception e){e.printStackTrace();}
            }

            try{Thread.sleep(2);}
            catch(Exception e){e.printStackTrace();}
        }
    }
}
