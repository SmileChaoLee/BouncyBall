package com.smile.bouncyball.threads

import android.graphics.Canvas
import android.graphics.Point
import android.graphics.Rect
import android.os.SystemClock
import com.smile.bouncyball.GameView
import com.smile.bouncyball.models.Banner
import com.smile.bouncyball.models.BouncyBall
import com.smile.bouncyball.tools.LogUtil.e
import java.util.Random
import java.util.Vector
import kotlin.concurrent.Volatile

class BallGoThread(private val gameView: GameView) : Thread() {

    companion object {
        private const val TAG = "BallGoThread"
    }

    private var gameViewWidth = 0
    private var synchronizeTime = 70
    @Volatile
    var flag = true // flag = true -> move ball
    // keepRunning = true -> loop in run() still going
    @Volatile
    private var keepRunning = true
    private val random = Random(System.currentTimeMillis())
    private var bottomY = 0
    private val stageScore = intArrayOf(0, 10, 30, 60, 100) // hits for each stage
    var score: Int = 0 //  score that user got
        private set
    var status: Int = GameView.START_STATUS
        private set
    private var bouncyBall: BouncyBall? = null
    private var banner: Banner? = null
    private val obstacleThreads: Vector<ObstacleThread>

    init {
        val obsThreads = gameView.obstacleThreads
        // obstacleThreads must not be null
        if (obsThreads == null) {
            throw NullPointerException("obstacleThreads must not be null.")
        }
        obstacleThreads = obsThreads
        gameViewWidth = gameView.gameViewWidth
        synchronizeTime = gameView.synchronizeTime
        bottomY = gameView.bottomY
        bouncyBall = gameView.bouncyBall
        banner = gameView.banner
        //   0 or 1  multiple 3 ------>0 or 3
        // val direction = random.nextInt(2) * 3
        // direction of bouncy ball
        // bouncyBall?.direction = direction

        score = 0
        status = GameView.START_STATUS
    }

    fun setKeepRunning(keepRunning: Boolean) {
        this.keepRunning = keepRunning
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
                        e(TAG, "run.mainLock.InterruptedException", ex)
                    }
                }
            }
            synchronized(gameView.gameLock) {
                // for GameView's synchronizing
                while (gameView.isPausedByUser) {
                    try {
                        gameView.gameLock.wait()
                    } catch (ex: InterruptedException) {
                        e(TAG, "run.gameLock.InterruptedException", ex)
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
                        // obstacleThread.isHitBouncyBall();    // removed on 2017-11-19
                        isHitObstacle(obstacleThread)
                    }
                }
            }
            SystemClock.sleep(synchronizeTime.toLong())
        }
    }

    private fun checkCollision() {
        val bBall = bouncyBall?: return
        val tempX = bBall.ballX
        val tempY = bBall.ballY
        val direction = bBall.direction
        when (direction) {
            GameView.BB_RIGHT_TOP -> {
                // going to right top
                bBall.ballX = tempX + bBall.ballRadius
                bBall.ballY = tempY - bBall.ballRadius

                if ((bBall.ballX + bBall.ballRadius) > gameViewWidth) {
                    if ((tempX > (gameViewWidth - bBall.ballRadius)) && (tempX < gameViewWidth)) {
                        bBall.ballX = gameViewWidth - bBall.ballRadius
                    } else {
                        // hit the right wall
                        bBall.direction = GameView.BB_LEFT_TOP
                    }
                } else if ((bBall.ballY - bBall.ballRadius) < 0) {
                    if ((tempY < bBall.ballRadius) && (tempY > 0)) {
                        bBall.ballY = bBall.ballRadius
                    } else {
                        // hit the top wall
                        bBall.direction = GameView.BB_RIGHT_BOTTOM
                    }
                }
            }

            GameView.BB_LEFT_TOP -> {
                // going to left top
                bBall.ballX = tempX - bBall.ballRadius
                bBall.ballY = tempY - bBall.ballRadius
                // ballY = ballY - gView.ballSpan;
                if ((bBall.ballX - bBall.ballRadius) < 0) {
                    if ((tempX < bBall.ballRadius) && (tempX > 0)) {
                        bBall.ballX = bBall.ballRadius
                    } else {
                        // hit the left wall
                        bBall.direction = GameView.BB_RIGHT_TOP
                    }
                } else if ((bBall.ballY - bBall.ballRadius) < 0) {
                    if ((tempY < bBall.ballRadius) && (tempY > 0)) {
                        bBall.ballY = bBall.ballRadius
                    } else {
                        // hit the top wall
                        bBall.direction = GameView.BB_LEFT_BOTTOM
                    }
                }
            }

            GameView.BB_RIGHT_BOTTOM -> {
                // going to right bottom
                bBall.ballX = tempX + bBall.ballRadius
                bBall.ballY = tempY + bBall.ballRadius

                // ballY = ballY + gView.ballSpan;
                if ((bBall.ballY + bBall.ballRadius) > bottomY) {
                    if ((tempY > (bottomY - bBall.ballRadius)) && (tempY < bottomY)) {
                        bBall.ballY = bottomY - bBall.ballRadius
                    } else {
                        // hit the bottom wall
                        checkHitBanner(GameView.BB_RIGHT_BOTTOM)
                    }
                } else if ((bBall.ballX + bBall.ballRadius) > gameViewWidth) {
                    if ((tempX > (gameViewWidth - bBall.ballRadius)) && (tempX < gameViewWidth)) {
                        bBall.ballX = gameViewWidth - bBall.ballRadius
                    } else {
                        //hit the right wall
                        bBall.direction = GameView.BB_LEFT_BOTTOM
                    }
                }
            }

            GameView.BB_LEFT_BOTTOM -> {
                // going to left bottom
                bBall.ballX = tempX - bBall.ballRadius
                bBall.ballY = tempY + bBall.ballRadius

                // ballY = ballY + gView.ballSpan;
                if ((bBall.ballY + bBall.ballRadius) > bottomY) {
                    if ((tempY > (bottomY - bBall.ballRadius)) && (tempY < bottomY)) {
                        bBall.ballY = bottomY - bBall.ballRadius
                    } else {
                        // hit the bottom wall
                        checkHitBanner(GameView.BB_LEFT_BOTTOM)
                    }
                } else if ((bBall.ballX - bBall.ballRadius) < 0) {
                    if ((tempX < bBall.ballRadius) && (tempX > 0)) {
                        bBall.ballX = bBall.ballRadius
                    } else {
                        // hit the left wall
                        bBall.direction = GameView.BB_RIGHT_BOTTOM
                    }
                }
            }
        }
    }

    private fun checkHitBanner(direction: Int): Boolean {
        var isHit = false
        val bBall = bouncyBall?: return isHit
        val ban = banner?: return isHit
        val bannerX1 = ban.bannerX - ban.bannerWidth / 2
        val bannerX2 = ban.bannerX + ban.bannerWidth / 2
        // (gView.bannerX,gView.bannerY) is the center of the banner
        if (((bBall.ballX + bBall.ballRadius) >= bannerX1) && ((bBall.ballX - bBall.ballRadius) <= bannerX2)) {
            // hit the banner
            when (direction) {
                1 -> bBall.direction = GameView.BB_RIGHT_TOP
                2 -> bBall.direction = GameView.BB_LEFT_TOP
            }
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
            // center point is inside the range of the obstacle
            val ballDirection = bBall.direction
            when (ballDirection) {
                GameView.BB_RIGHT_TOP -> if ((ballTop >= obstacleTop) && (ballTop <= obstacleBottom)) {
                    // hit
                    bBall.direction = GameView.BB_RIGHT_BOTTOM
                    bBall.ballY = obstacleBottom + radius
                    isHit = true
                }

                GameView.BB_LEFT_TOP -> if ((ballTop >= obstacleTop) && (ballTop <= obstacleBottom)) {
                    // hit
                    bBall.direction = GameView.BB_LEFT_BOTTOM
                    bBall.ballY = obstacleBottom + radius
                    isHit = true
                }

                GameView.BB_RIGHT_BOTTOM -> if ((ballBottom >= obstacleTop) && (ballBottom <= obstacleBottom)) {
                    bBall.direction = GameView.BB_RIGHT_TOP
                    bBall.ballY = obstacleTop - radius
                    isHit = true
                }

                GameView.BB_LEFT_BOTTOM -> if ((ballBottom >= obstacleTop) && (ballBottom <= obstacleBottom)) {
                    bBall.direction = GameView.BB_LEFT_TOP
                    bBall.ballY = obstacleTop - radius
                    isHit = true
                }
            }
        }

        return isHit
    }

    fun drawBouncyBall(canvas: Canvas) {
        // draw the ball
        val bBall = bouncyBall?: return
        var tempX = bBall.ballX - bBall.ballRadius
        if (tempX < 0) {
            tempX = 0
            bBall.ballX = bBall.ballRadius
        }
        var tempY = bBall.ballY - bBall.ballRadius
        if (tempY < 0) {
            tempY = 0
            bBall.ballY = bBall.ballRadius
        }
        val sPoint = Point(tempX, tempY)

        tempX = sPoint.x + bBall.ballSize
        if (tempX > gameViewWidth) {
            tempX = gameViewWidth
            sPoint.x = tempX - bBall.ballSize
            bBall.ballX = tempX - bBall.ballRadius
        }
        tempY = sPoint.y + bBall.ballSize
        if (tempY > bottomY) {
            tempY = bottomY
            sPoint.y = tempY - bBall.ballSize
            bBall.ballY = tempY - bBall.ballRadius
        }
        // draw the bouncy ball
        val rect2 = Rect(sPoint.x, sPoint.y, tempX, tempY)
        bBall.draw(canvas, rect2)
    }
}