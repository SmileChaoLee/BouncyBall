package com.smile.bouncyball.threads

import android.os.SystemClock
import com.smile.bouncyball.GameView
import com.smile.bouncyball.tools.LogUtil
import java.util.Vector
import kotlin.concurrent.Volatile

class BallGoThread(private val gameView: GameView) : Thread() {

    companion object {
        private const val TAG = "BallGoThread"
    }

    @Volatile
    var flag = true // flag = true -> move ball
    // keepRunning = true -> loop in run() still going
    @Volatile
    var keepRunning = true
    private val stageScore = intArrayOf(0, 10, 30, 60, 100) // hits for each stage
    var score: Int = 0 //  score that user got
        private set
    var status: Int = GameView.START_STATUS
        private set
    private val bouncyBall = gameView.bouncyBall
    private val banner = gameView.banner
    private val obstacleThreads: Vector<ObstacleThread>
    private val bottomY = gameView.bottomY
    private val gameViewWidth = gameView.gameViewWidth
    private var synchronizeTime = gameView.synchronizeTime.toLong()

    init {
        val obsThreads = gameView.obstacleThreads
        // obstacleThreads must not be null
        if (obsThreads == null) {
            throw NullPointerException("obstacleThreads must not be null.")
        }
        obstacleThreads = obsThreads
        score = 0
        status = GameView.START_STATUS
    }

    override fun run() {
        status = GameView.FIRST_STAGE // start running is first stage
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
            if (flag) {
                // 2017-11-19
                checkCollision() // collision with banner or walls
                if ((status == GameView.FAILED_STATUS) || (status == GameView.FINISHED_STATUS)) {
                    // failed or reach highest score (finished)
                    // stop running this thread, means set keepRunning to false;
                    keepRunning = false
                } else {
                    // 2017-11-19 morning
                    for (obstacleThread in obstacleThreads) {
                        isHitObstacle(obstacleThread)
                    }
                }
            }
            SystemClock.sleep(synchronizeTime)
        }
    }

    private fun  checkCollision() {
        val logStr = "checkCollision"
        val bBall = bouncyBall?: return
        val tempX = bBall.ballX
        LogUtil.d(TAG, "$logStr.bBall.ballX = $tempX")
        val tempY = bBall.ballY
        LogUtil.d(TAG, "$logStr.bBall.ballY = $tempY")
        val dirV = bBall.dirVector
        LogUtil.d(TAG, "$logStr.dirV = (${dirV.x}, ${dirV.y })")
        val speed = bBall.speed
        LogUtil.d(TAG, "$logStr.speed = $speed")
        bBall.ballX = tempX + dirV.x
        bBall.ballY = tempY - dirV.y
        if (dirV.x >= 0 && dirV.y >= 0) {
            // going to right top
            if ((bBall.ballX + bBall.ballRadius) > gameViewWidth) {
                if ((tempX > (gameViewWidth - bBall.ballRadius)) && (tempX < gameViewWidth)) {
                    bBall.ballX = gameViewWidth - bBall.ballRadius
                } else {
                    // hit the right wall
                    // bBall.direction = GameView.BB_LEFT_TOP
                    bBall.dirVector.x = -dirV.x  // going to left top
                }
            } else if ((bBall.ballY - bBall.ballRadius) < 0) {
                if ((tempY < bBall.ballRadius) && (tempY > 0)) {
                    bBall.ballY = bBall.ballRadius
                } else {
                    // hit the top wall
                    bBall.dirVector.y = -dirV.y  // going to right bottom
                }
            }
        } else if (dirV.x < 0 && dirV.y >= 0) {
            // going to left top
            if ((bBall.ballX - bBall.ballRadius) < 0) {
                if ((tempX < bBall.ballRadius) && (tempX > 0)) {
                    bBall.ballX = bBall.ballRadius
                } else {
                    // hit the left wall
                    bBall.dirVector.x = -bBall.dirVector.x  // going to right top
                }
            } else if ((bBall.ballY - bBall.ballRadius) < 0) {
                if ((tempY < bBall.ballRadius) && (tempY > 0)) {
                    bBall.ballY = bBall.ballRadius
                } else {
                    // hit the top wall
                    bBall.dirVector.y = -dirV.y  // going to left bottom
                }
            }
        } else if (dirV.x >= 0 && dirV.y < 0) {
            // (dirV.x >= 0 && dirV.y < 0)
            // going to right bottom
            if ((bBall.ballY + bBall.ballRadius) > bottomY) {
                if ((tempY > (bottomY - bBall.ballRadius)) && (tempY < bottomY)) {
                    bBall.ballY = bottomY - bBall.ballRadius
                } else {
                    // hit the bottom wall
                    checkHitBanner()
                }
            } else if ((bBall.ballX + bBall.ballRadius) > gameViewWidth) {
                if ((tempX > (gameViewWidth - bBall.ballRadius)) && (tempX < gameViewWidth)) {
                    bBall.ballX = gameViewWidth - bBall.ballRadius
                } else {
                    //hit the right wall
                    bBall.dirVector.x = -dirV.x  // going to left bottom
                }
            }
        } else {
            // (dirV.x < 0 && dirV.y < 0)
            // going to left bottom
            if ((bBall.ballY + bBall.ballRadius) > bottomY) {
                if ((tempY > (bottomY - bBall.ballRadius)) && (tempY < bottomY)) {
                    bBall.ballY = bottomY - bBall.ballRadius
                } else {
                    // hit the bottom wall
                    checkHitBanner()
                }
            } else if ((bBall.ballX - bBall.ballRadius) < 0) {
                if ((tempX < bBall.ballRadius) && (tempX > 0)) {
                    bBall.ballX = bBall.ballRadius
                } else {
                    // hit the left wall
                    bBall.dirVector.x = -dirV.x  // going to right bottom
                }
            }
        }
    }

    private fun checkHitBanner(): Boolean {
        var isHit = false
        val bBall = bouncyBall?: return isHit
        val ban = banner?: return isHit
        val bannerX1 = ban.bannerX - ban.bannerWidth / 2
        val bannerX2 = ban.bannerX + ban.bannerWidth / 2
        if (((bBall.ballX + bBall.ballRadius) >= bannerX1)
            && ((bBall.ballX - bBall.ballRadius) <= bannerX2)) {
            // hit the banner, then going up
            bBall.dirVector.y = -bBall.dirVector.y
            // score policy: add one score when it hit the banner. Added on 2017-11-07
            score++
            isHit = true
        }

        checkStatus(isHit)

        return isHit
    }

    private fun checkStatus(isHit: Boolean) {
        if (!isHit) {
            status = GameView.FAILED_STATUS
            return
        }

        // maximum value of the number that banner is to be hit to make user win
        // -1-> failed and game over, 0->waiting to start, 1->first stage (playing), 2->second stage (playing)
        // 3->final stage (playing), 4-finished the game
        val highest = 999
        if (score < highest) {
            // has not reach highest score yet
            if (status < GameView.FINAL_STAGE) {
                if (score >= stageScore[status]) {
                    status++
                    synchronizeTime -= 10
                }
            }
        } else {
            // reached highest score then finished
            status = GameView.FINISHED_STATUS
        }
    }

    private fun isHitObstacle(obstacleThread: ObstacleThread): Boolean {
        var isHit = false
        val bBall = bouncyBall?: return isHit
        val dirV = bBall.dirVector
        val ballCenterX = bBall.ballX
        val ballCenterY = bBall.ballY
        val radius = bBall.ballRadius
        val ballLeft = ballCenterX - radius
        val ballRight = ballCenterX + radius
        val ballTop = ballCenterY - radius
        val ballBottom = ballCenterY + radius
        val position = obstacleThread.obsCenterPos
        val obstacleLeft = position.x - obstacleThread.obstacleWidth / 2
        val obstacleRight = position.x + obstacleThread.obstacleWidth / 2
        val obstacleTop = position.y - obstacleThread.obstacleHeight / 2
        val obstacleBottom = position.y + obstacleThread.obstacleHeight / 2

        if ((ballRight >= obstacleLeft) && (ballLeft <= obstacleRight)) {
            if (dirV.y >= 0) {
                // going up (to  top)
                if ((ballTop >= obstacleTop) && (ballTop <= obstacleBottom)) {
                    // hit
                    bBall.ballY = obstacleBottom + radius
                    isHit = true
                }
            } else {
                // (dirV.x >= 0 && dirV.y < 0)
                // going down (to bottom)
                if ((ballBottom >= obstacleTop) && (ballBottom <= obstacleBottom)) {
                    bBall.ballY = obstacleTop - radius
                    isHit = true
                }
            }
            if (isHit) {
                bBall.dirVector.y = -dirV.y
            }
        }

        return isHit
    }
}