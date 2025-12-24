package com.smile.bouncyball.threads

import android.os.SystemClock
import android.view.SurfaceHolder
import com.smile.bouncyball.GameView
import com.smile.bouncyball.tools.LogUtil
import kotlin.concurrent.Volatile

class GameViewDrawThread(private val gameView: GameView) : Thread() {

    companion object {
        private const val TAG = "GameViewDrawThread"
    }

    // keepRunning = true -> loop in run() still going
    @Volatile
    private var keepRunning = true
    private var surfaceHolder: SurfaceHolder? = null
    private var synchronizeTime = 70

    init {
        this.surfaceHolder = gameView.surfaceHolder
        this.synchronizeTime = gameView.synchronizeTime
    }

    override fun run() {
        while (keepRunning) {
            synchronized(gameView.mainLock) {
                // for application's (Main activity) synchronizing
                while (!gameView.isGameVisible) {
                    try {
                        gameView.mainLock.wait()
                    } catch (e: InterruptedException) {
                        LogUtil.e(TAG, "run.mainLock.InterruptedException", e)
                    }
                }
            }
            synchronized(gameView.gameLock) {
                // for GameView's synchronizing
                while (gameView.isPausedByUser) {
                    try {
                        gameView.gameLock.wait()
                    } catch (e: InterruptedException) {
                        LogUtil.e(TAG, "run.gameLock.InterruptedException", e)
                    }
                }
            }
            // start drawing
            gameView.drawGameScreen()
            SystemClock.sleep(synchronizeTime.toLong())
        }
    }

    fun setKeepRunning(keepRunning: Boolean) {
        this.keepRunning = keepRunning
    }
}
