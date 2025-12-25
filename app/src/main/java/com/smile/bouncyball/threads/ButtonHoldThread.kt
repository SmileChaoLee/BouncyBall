package com.smile.bouncyball.threads

import android.os.SystemClock
import com.smile.bouncyball.GameView
import com.smile.bouncyball.tools.LogUtil
import kotlin.concurrent.Volatile

/**
 * Created by Chao Lee on 2017-11-18.
 */
class ButtonHoldThread(private val gameView: GameView) : Thread() {


    companion object {
        private const val TAG = "ButtonHoldThread"
    }

    @Volatile
    var keepRunning = true
    @Volatile
    var isButtonHold = false
    @Volatile
    var bannerMoveSpeed = 0
    private val banner = gameView.banner
    private val gameViewWidth = gameView.gameViewWidth

    init {
        keepRunning = true
        isButtonHold = false
        bannerMoveSpeed = 0
    }

    override fun run() {
        while (keepRunning) {
            synchronized(gameView.mainLock) {
                // for application's (Main activity) synchronizing
                while (!gameView.isGameVisible) {
                    try {
                        gameView.mainLock.wait()
                    } catch (ex: InterruptedException) {
                        LogUtil.e(TAG, "run.mainLock.InterruptedException", ex)
                    }
                }
            }
            synchronized(gameView.gameLock) {
                // for GameView's synchronizing
                while (gameView.isPausedByUser) {
                    try {
                        gameView.gameLock.wait()
                    } catch (ex: InterruptedException) {
                        LogUtil.e(TAG, "run.gameLock.InterruptedException", ex)
                    }
                }
            }
            // do the work of holding button
            while (isButtonHold) {
                banner?.let {
                    var bannerX = it.bannerX
                    bannerX += bannerMoveSpeed
                    if (bannerX < 0) {
                        bannerX = 0
                    }
                    if (bannerX > gameViewWidth) {
                        bannerX = gameViewWidth
                    }
                    // set position of banner
                    it.bannerX = bannerX
                }
                SystemClock.sleep(20)
            }
            SystemClock.sleep(2)
        }
    }
}
