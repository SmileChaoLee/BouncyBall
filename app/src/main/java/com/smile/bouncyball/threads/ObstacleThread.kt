package com.smile.bouncyball.threads

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.graphics.Rect
import android.os.SystemClock
import com.smile.bouncyball.GameView
import com.smile.bouncyball.models.Banner
import com.smile.bouncyball.models.BouncyBall
import com.smile.bouncyball.tools.LogUtil.e
import java.util.Random
import kotlin.concurrent.Volatile

/**
 * Created by Chao Lee on 2017-11-14.
 */
class ObstacleThread(private val gameView: GameView,
                     private val stageNo: Int) : Thread() {

    companion object {
        private const val TAG = "ObstacleThread"
    }

    private val obsColor =
        intArrayOf(Color.BLACK, Color.DKGRAY, Color.RED, Color.GREEN, Color.MAGENTA, Color.CYAN)
    var obstacleWidth: Int = 100
        private set
    var obstacleHeight: Int = 20
        private set

    // keepRunning = true -> loop in run() still going
    @Volatile
    var keepRunning = true
    private var direction = 1 // 1->left, 2->right, 3->up, 4->down
    private var speed = 0 // no moving, moving speed (left, right, up, or down)
    private var color = Color.BLACK // the color of obstacle
    var obsCenterPos = Point()    // the position of the center of this obstacle
        private set

    private var xRangeOfObstacle = 0
    private var yRangeOfObstacle = 0
    private val random = Random(System.currentTimeMillis())
    private var bouncyBall: BouncyBall? = null
    private var banner: Banner? = null

    init {
        xRangeOfObstacle = gameView.gameViewWidth
        // one-third of the height of Game View
        yRangeOfObstacle = gameView.gameViewHeight / 3
        bouncyBall = gameView.bouncyBall
        obstacleHeight = bouncyBall?.ballRadius ?: 0
        banner = gameView.banner
        obstacleWidth = banner?.bannerWidth ?: 0
        initializeObstacle()
    }

    override fun run() {
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
            // move the obstacle
            moveObstacle()
            SystemClock.sleep(gameView.synchronizeTime.toLong())
        }
    }

    private fun initializeObstacle() {
        val dr = random.nextFloat()
        // only left and right for now
        direction = if (dr <= 0.5) {
            1 // left
        } else {
            2 // right
        }
        speed = 5 + (random.nextFloat() * 10.0f).toInt() // 5 ~ 15
        val col = (random.nextFloat() * 5.0f).toInt() // 0 ~ 5
        color = obsColor[col]
        var x = random.nextFloat() // 0.0 ~ 1.0
        x *= xRangeOfObstacle.toFloat()
        val y = ((bouncyBall?.ballSize ?: 0) * stageNo * 2).toFloat()
        obsCenterPos.set(x.toInt(), y.toInt()) // position of the center
    }

    private fun moveObstacle() {
        var x = obsCenterPos.x
        var y = obsCenterPos.y
        if (direction == 1) {
            // left
            x -= speed
            if (x < 0) {
                x = 0
                // left then change to right
                direction = 2
            }
        } else if (direction == 2) {
            // right
            x += speed
            if (x > xRangeOfObstacle) {
                x = xRangeOfObstacle
                // right then change to left
                direction = 1
            }
        } else if (direction == 3) {
            // up
            y -= speed
            if (y < 0) {
                y = 0
                // up then change to down
                direction = 4
            }
        } else {
            // down
            y += speed
            if (y > yRangeOfObstacle) {
                y = yRangeOfObstacle
                // down then change to up
                direction = 3
            }
        }
        obsCenterPos.set(x, y)
    }

    fun drawObstacle(canvas: Canvas) {
        val left = obsCenterPos.x - obstacleWidth / 2
        val right = obsCenterPos.x + obstacleWidth / 2
        val top = obsCenterPos.y - obstacleHeight / 2
        val bottom = obsCenterPos.y + obstacleHeight / 2

        val rect = Rect(left, top, right, bottom)
        val paint = Paint()

        paint.setColor(color)

        canvas.drawRect(rect, paint)
    }
}
