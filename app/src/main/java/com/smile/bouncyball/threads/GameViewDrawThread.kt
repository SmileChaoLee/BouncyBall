package com.smile.bouncyball.threads

import android.graphics.Canvas
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
            var c: Canvas?
            c = null
            // lock the whole canvas. high requirement on memory, do not use null advised
            surfaceHolder?.let { sHolder ->
                try {
                    c = sHolder.lockCanvas(null)
                    if (c != null) {
                        // synchronized (gView.surfaceHolder) {
                        synchronized(sHolder) {
                            gameView.doDraw(c) // draw
                        }
                    } else {
                        LogUtil.d(TAG, "run.lockCanvas.Canvas = null.")
                    }
                } finally {
                    if (c != null) {
                        // fresh the screen
                        sHolder.unlockCanvasAndPost(c)
                    }
                }
            }
            SystemClock.sleep(synchronizeTime.toLong())
        }
    }

    fun setKeepRunning(keepRunning: Boolean) {
        this.keepRunning = keepRunning
    }
}
