package com.smile.bouncyball

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Point
import android.graphics.Rect
import android.graphics.Typeface
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import com.smile.bouncyball.models.Banner
import com.smile.bouncyball.models.BouncyBall
import com.smile.bouncyball.tools.LogUtil
import com.smile.smilelibraries.utilities.ScreenUtil
import java.util.Vector
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.lifecycleScope
import com.smile.bouncyball.models.RectBitmap
import com.smile.bouncyball.models.ThreePoints
import com.smile.bouncyball.models.Triangle
import com.smile.bouncyball.threads.BallGoThread
import com.smile.bouncyball.threads.ButtonHoldThread
import com.smile.bouncyball.threads.GameViewDrawThread
import com.smile.bouncyball.threads.ObstacleThread
import com.smile.smilelibraries.scoresqlite.ScoreSQLite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.*

@SuppressLint("ViewConstructor")
class GameView(private val mainActivity: MainActivity)
    : SurfaceView(mainActivity),
    SurfaceHolder.Callback {
    companion object {
        private const val TAG = "GameVew"
        // public properties
        const val FAILED_STATUS: Int = -1
        const val START_STATUS: Int = 0
        const val FIRST_STAGE: Int = 1
        const val SECOND_STAGE: Int = 2
        const val THIRD_STAGE: Int = 3
        const val FINAL_STAGE: Int = 4
        const val FINISHED_STATUS: Int = 5

        const val BB_RIGHT_TOP: Int = 0 // going to right top
        const val BB_LEFT_TOP: Int = 3 // going to left top
        const val BB_RIGHT_BOTTOM: Int = 1 // going to right bottom
        const val BB_LEFT_BOTTOM: Int = 2 // going to left bottom

        // private properties
        private const val BALL_SIZE_RATIO = 1.0f / 18f
        private const val HINT_WIDTH_RATIO = 1.0f / 1.5f
        private const val HINT_HEIGHT_RATIO = 1.0f / 8.0f
        private const val BUTTON_WIDTH_RATIO = 1.0f / 4.0f
        private const val BUTTON_HEIGHT_RATIO = 1.0f / 12.0f
        private const val BANNER_WIDTH_RATIO = 1.0f / 4.0f
        private const val BANNER_HEIGHT_RATIO = 1.0f / 20.0f
    }

    val mainLock = Object()
    val gameLock = Object() // for synchronizing
    // for running a thread when arrow button (left arrow or right arrow) is held
    var ballGoThread: BallGoThread? = null //BallGoThread
        private set
    var obstacleThreads: Vector<ObstacleThread>? = null
        private set
    var buttonHoldThread: ButtonHoldThread? = null
    private var gameViewDrawThread: GameViewDrawThread? = null

    private val backgroundRect = RectBitmap() // rectangle area for hint to start
    private val beginRect = RectBitmap() // rectangle area for hint to start
    private val startRect = RectBitmap() // rectangle area for start game
    private val pauseRect = RectBitmap() // rectangle area for start game
    private val resumeRect = RectBitmap() // rectangle area for start game
    private val rightArrowRect = RectBitmap() // rectangle area for right arrow
    private val leftArrowRect = RectBitmap() // rectangle area for left arrow
    private val gameOverRect = RectBitmap() // rectangle area for gae over
    private var shootArrow = Triangle()
    private val iScore = arrayOfNulls<Bitmap>(10) // score pictures (pictures for numbers)

    private var highestTextView: TextView? = null
    private var scoreImage0: ImageView? = null
    private var scoreImage1: ImageView? = null
    private var scoreImage2: ImageView? = null

    // string resources
    // private var stageLevels: Array<String>? = null
    private var status = START_STATUS
    private var score = 0 //  score that user got
    private var highestScore = 0
    private val scoreSQLiteDB = ScoreSQLite(mainActivity,
        BouncyBallApp.DATABASE_NAME)
    private var isGameJustCreated = true
    private var mThreeP: ThreePoints? = null
    private var mOldPosX = 0f

    val synchronizeTime = 70
    var isGameVisible = true
    var isPausedByUser = false
    var surfaceHolder: SurfaceHolder? = null
        private set
    var bottomY: Int = 0
        private set
    var gameViewWidth: Int = 0
        private set
    var gameViewHeight: Int = 0
        private set
    var bouncyBall: BouncyBall? = null
        private set
    var banner: Banner? = null
        private set

    init {
        val textFontSize = ScreenUtil.getPxTextFontSizeNeeded(mainActivity)
        isGameJustCreated = true
        isGameVisible = true
        val actionBar = mainActivity.supportActionBar
        // actionBar.setDisplayShowTitleEnabled(false);
        actionBar?.let { abIt ->
            abIt.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
            abIt.setDisplayShowCustomEnabled(true) // enable customized action bar
            abIt.setBackgroundDrawable(Color.LTGRAY.toDrawable())
            abIt.setCustomView(R.layout.action_bar_layout)
            val actionBarView = abIt.customView
            actionBarView?.let { abV ->
                highestTextView = abV.findViewById<TextView?>(R.id.highestTextView)
                val ratio = if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                    1.3f else 1f
                ScreenUtil.resizeTextSize(highestTextView, textFontSize * ratio)
                scoreImage0 = abV.findViewById(R.id.scoreView0)
                scoreImage1 = abV.findViewById(R.id.scoreView1)
                scoreImage2 = abV.findViewById(R.id.scoreView2)
                // stageLevels = resources.getStringArray(R.array.stageLevels)
            }
        }

        isPausedByUser = false // for synchronizing

        setZOrderOnTop(true)
        surfaceHolder = holder
        surfaceHolder?.let { holder ->
            holder.addCallback(this@GameView) // register the interface
            holder.setFormat(PixelFormat.TRANSLUCENT)
        }

        status = START_STATUS // waiting to start
        // setWillNotDraw(false);  // added on 2017-11-07 to activate onDraw() of SurfaceView
        // if set it to false, then the drawing might have to go through by onDraw() all the time
        setWillNotDraw(true) // added on 2017-11-07 for just in case, the default is false

        mainActivity.lifecycleScope.launch(Dispatchers.IO) {
            highestScore = scoreSQLiteDB.readHighestScore()
            withContext(Dispatchers.Main) {
                highestTextView?.text = highestScore.toString()
            }
        }

        LogUtil.d(TAG, "GameView created")
    }

    private fun initBitmapAndModels() {
        LogUtil.d(TAG, "initBitmapAndModels() is called")
        iScore[0] = BitmapFactory.decodeResource(resources, R.drawable.d0)
        iScore[1] = BitmapFactory.decodeResource(resources, R.drawable.d1)
        iScore[2] = BitmapFactory.decodeResource(resources, R.drawable.d2)
        iScore[3] = BitmapFactory.decodeResource(resources, R.drawable.d3)
        iScore[4] = BitmapFactory.decodeResource(resources, R.drawable.d4)
        iScore[5] = BitmapFactory.decodeResource(resources, R.drawable.d5)
        iScore[6] = BitmapFactory.decodeResource(resources, R.drawable.d6)
        iScore[7] = BitmapFactory.decodeResource(resources, R.drawable.d7)
        iScore[8] = BitmapFactory.decodeResource(resources, R.drawable.d8)
        iScore[9] = BitmapFactory.decodeResource(resources, R.drawable.d9)

        val bannerWidth = (gameViewWidth.toFloat() * BANNER_WIDTH_RATIO).toInt() // width of the banner
        // width of the banner
        val bannerHeight = (gameViewHeight.toFloat() * BANNER_HEIGHT_RATIO).toInt() // height of the banner
        // height of the banner
        val ballSize = (gameViewHeight.toFloat() * BALL_SIZE_RATIO).toInt() // size of the ball
        // size of the ball
        val beginWidth = (gameViewWidth.toFloat() * HINT_WIDTH_RATIO).toInt() // width of the hint
        val beginHeight = (gameViewHeight.toFloat() * HINT_HEIGHT_RATIO).toInt() // height of the hint
        val leftArrowWidth = (gameViewWidth.toFloat() * BUTTON_WIDTH_RATIO).toInt()
        val leftArrowHeight =
            (gameViewHeight.toFloat() * BUTTON_HEIGHT_RATIO * 1.5).toInt() // 1.5 * normal button
        val startWidth = (gameViewWidth.toFloat() * BUTTON_WIDTH_RATIO).toInt()
        val startHeight = (gameViewHeight.toFloat() * BUTTON_HEIGHT_RATIO).toInt()
        val rightArrowWidth = (gameViewWidth.toFloat() * BUTTON_WIDTH_RATIO).toInt()
        val rightArrowHeight =
            (gameViewHeight.toFloat() * BUTTON_HEIGHT_RATIO * 1.5).toInt() // 1.5 * normal button

        val iBack = BitmapFactory.decodeResource(resources, R.drawable.back)
        backgroundRect.set(0,
            0,
            gameViewWidth,
            gameViewHeight,
            iBack
        )

        val biasX = 10
        val biasY = 10

        val iLeftArrow =
            getBitmapFromResourceWithText(R.drawable.leftarrow, "", Color.BLUE) // no string
        val sPoint = Point(biasX, gameViewHeight - leftArrowHeight - biasY)
        leftArrowRect.set(sPoint.x,
            sPoint.y,
            sPoint.x + leftArrowWidth,
            sPoint.y + leftArrowHeight,
            iLeftArrow)

        val startStr = resources.getString(R.string.start_string)
        val iStart = getBitmapFromResourceWithText(R.drawable.start, startStr, Color.BLUE)
        sPoint.set((gameViewWidth - startWidth) / 2, gameViewHeight - startHeight - biasY)
        startRect.set(sPoint.x,
            sPoint.y,
            sPoint.x + startWidth,
            sPoint.y + startHeight,
            iStart)

        val pauseStr = resources.getString(R.string.pause_string)
        val iPause = getBitmapFromResourceWithText(R.drawable.pause, pauseStr, Color.YELLOW)
        pauseRect.set(sPoint.x,
            sPoint.y,
            sPoint.x + startWidth,
            sPoint.y + startHeight,
            iPause)

        val resumeStr = resources.getString(R.string.resume_string)
        val iResume = getBitmapFromResourceWithText(R.drawable.resume, resumeStr, Color.BLUE)
        resumeRect.set(sPoint.x,
            sPoint.y,
            sPoint.x + startWidth,
            sPoint.y + startHeight,
            iResume)

        val iRightArrow =
            getBitmapFromResourceWithText(R.drawable.rightarrow, "", Color.RED) // no string
        sPoint.set(gameViewWidth - rightArrowWidth - biasX, gameViewHeight - rightArrowHeight - biasY)
        rightArrowRect.set(
            sPoint.x,
            sPoint.y,
            sPoint.x + rightArrowWidth,
            sPoint.y + rightArrowHeight,
            iRightArrow
        )

        val gapRatio = if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
            1f/10f else 1f/6f
        bottomY = gameViewHeight - bannerHeight - startHeight - (gameViewHeight * gapRatio).toInt()
        bottomY = (bottomY / ballSize) * ballSize

        val beginStr = resources.getString(R.string.begin_string)
        val iBegin = getBitmapFromResourceWithText(R.drawable.begin, beginStr, Color.BLUE)
        sPoint.set((gameViewWidth - beginWidth) / 2, (bottomY - beginHeight) / 2)
        beginRect.set(sPoint.x,
            sPoint.y,
            sPoint.x + beginWidth,
            sPoint.y + beginHeight,
            iBegin )

        val gameOverStr = resources.getString(R.string.gameOver_string)
        val iGameOver = getBitmapFromResourceWithText(R.drawable.begin, gameOverStr, Color.RED)
        gameOverRect.set(sPoint.x,
            sPoint.y,
            sPoint.x + beginWidth,
            sPoint.y + beginHeight,
            iGameOver)

        shootArrow = Triangle(mColor = Color.RED)
        val iBall = BitmapFactory.decodeResource(resources, R.drawable.ball)
        bouncyBall = BouncyBall(ballSize = ballSize, bitmap = iBall)
        // val bannerX = gameViewWidth / 2 //  the coordinate (x-axis) of the banner
        // val bannerY = bottomY + (bannerHeight / 2)
        val iBanner = BitmapFactory.decodeResource(resources, R.drawable.banner)
        banner = Banner(bannerWidth = bannerWidth, bannerHeight = bannerHeight, bitmap = iBanner)
        initBallAndBanner()
    }

    private fun initBallAndBanner() {
        // initialize the coordinates of the ball
        // (ballX,ballY) is the center of the circle
        bouncyBall?.let {
            it.ballX = gameViewWidth / 2
            it.ballY = bottomY - it.ballRadius
            val triWidth = it.ballRadius.toFloat() * 2f
            shootArrow.x = it.ballX.toFloat()
            shootArrow.y = it.ballY.toFloat() - triWidth
            shootArrow.width = triWidth
            shootArrow.height = triWidth
        }
        // initialize the coordinates of the banner
        // (bannerX,bannerY) is the center of banner
        banner?.let {
            it.bannerX = gameViewWidth / 2
            it.bannerY = bottomY + it.bannerHeight / 2
        }
    }

    override fun onDraw(canvas: Canvas) {
        LogUtil.d(TAG, "onDraw")
        doDraw(canvas)
    }

    fun doDraw(canvas: Canvas) {
        LogUtil.d(TAG, "doDraw")
        // clear the background
        backgroundRect.draw(canvas)
        if (status == START_STATUS) {
            shootArrow.drawTriangle(canvas)
        }
        drawBanner(canvas)
        drawBouncyBall(canvas)
        // draw obstacles
        obstacleThreads?.let {
            for (obstacleThread in it) {
                obstacleThread.drawObstacle(canvas)
            }
        }
        leftArrowRect.draw(canvas)
        rightArrowRect.draw(canvas)
        // verifying score and status
        ballGoThread?.let {
            score = it.score
            status = it.status
        }
        // draw score, action bar is on the main UI thread not in the game view
        mainActivity.runOnUiThread {
            val scoreStr = StringBuilder(score.toString() + "")
            val loop = 3 - scoreStr.length
            for (i in 0..<loop) {
                scoreStr.insert(0, "0")
            }
            var tempScore = scoreStr[2].code - '0'.code
            scoreImage0?.setImageBitmap(iScore[tempScore])
            tempScore = scoreStr[1].code - '0'.code
            scoreImage1?.setImageBitmap(iScore[tempScore])
            tempScore = scoreStr[0].code - '0'.code
            scoreImage2?.setImageBitmap(iScore[tempScore])
        }
        if (status == START_STATUS) {
            beginRect.draw(canvas)
            startRect.draw(canvas)
            // direction of bouncy ball
            bouncyBall?.direction = 3   // 0 or 3
        } else {
            if (isPausedByUser) {
                resumeRect.draw(canvas)
            } else {
                pauseRect.draw(canvas)
            }
            if ((status >= SECOND_STAGE) && (status <= FINAL_STAGE)) {
                // stage 1 to stage 4
                // 1 obstacle for stage 2, 2 obstacles for stage 3, 3 obstacles for stage 4(final stage)
                obstacleThreads?.let {
                    val obsSize = it.size
                    val numOfObstacles = status - 1
                    if (obsSize < numOfObstacles) {
                        for (i in obsSize..<numOfObstacles) {
                            val obstacleThread = ObstacleThread(this, i + 1)
                            it.addElement(obstacleThread)
                            obstacleThread.start()
                        }
                    }
                }
            } else if ((status == FAILED_STATUS) || (status == FINISHED_STATUS)) {
                //  game over
                ballGoThread?.setKeepRunning(false) // stop running the BallGoThread, added on 2017-11-07
                gameViewDrawThread?.setKeepRunning(false) // added on 2017-11-07
                obstacleThreads?.let {
                    for (obstacleThread in it) {
                        obstacleThread.setKeepRunning(false)
                    }
                    it.clear()
                }
                gameOverRect.draw(canvas)
                mainActivity.let { act ->
                    act.lifecycleScope.launch(Dispatchers.IO) {
                        // store currentScore as a score in database
                        // no more sending score to the cloud
                        scoreSQLiteDB.addScore("", score)
                        scoreSQLiteDB.deleteAllAfterTop10()
                        highestScore = scoreSQLiteDB.readHighestScore()
                        delay(3000)
                        withContext(Dispatchers.Main) {
                            releaseSync()
                            renewGame() // no showing for ad
                            highestTextView?.text = highestScore.toString()
                        }
                    }
                }
            } else {
                // first stage, do nothing
                // LogUtil.d(TAG, "doDraw.first stage, do nothing")
            }
        }
    }

    private fun drawBanner(canvas: Canvas) {
        banner?.apply {
            val ax = bannerX - bannerWidth / 2
            val ay = bannerY - bannerHeight / 2
            val rect2 = Rect(ax, ay, ax + bannerWidth, ay + bannerHeight)
            draw(canvas, rect2)
        }
    }

    private fun drawBouncyBall(canvas: Canvas) {
        bouncyBall?.let { bBall ->
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

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val posX = event.x.toInt()
        val posY = event.y.toInt()
        LogUtil.d(TAG, "onTouchEvent.x = $posX")
        val action = event.action
        when (action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_MOVE ->
                if (status == START_STATUS) {
                    val bBal = bouncyBall ?: return false
                    LogUtil.d(TAG, "onTouchEvent.posX = $posX")
                    val dg = atan((event.x - mOldPosX) / gameViewWidth)
                    LogUtil.d(TAG, "onTouchEvent.dg = $dg")
                    mThreeP?.let {
                        val centerX = bBal.ballX.toFloat()
                        val centerY = bBal.ballY.toFloat()
                        val tempThree = it.rotate(dg, centerX, centerY)
                        val ax = tempThree.topX - shootArrow.x
                        val by = tempThree.topY - shootArrow.y
                        val slope = abs(ax / by)
                        val angle = atan(slope.toDouble())
                        LogUtil.d(TAG, "onTouchEvent.angle = $angle")
                        val maxAngle = PI / (180f / 70f)    // 70 degree
                        if (angle < maxAngle) {
                            shootArrow.threeP = tempThree
                            drawGameScreen()
                        }
                    }
                } else {
                    if ((status >= FIRST_STAGE) && (status < FINISHED_STATUS)) {
                        val bn = banner ?: return false
                        if (!isPausedByUser) {
                            // if ((y >= (bottomY - 20)) && (y <= (bottomY + 20 + bn.bannerHeight))) {
                            // Y - coordinate is inside the area and add 20 extra pixels
                            bn.bannerX = posX
                            // }
                        }
                    }
                }
            MotionEvent.ACTION_BUTTON_PRESS, MotionEvent.ACTION_DOWN -> if (status == START_STATUS) {
                LogUtil.d(TAG, "onTouchEvent.MotionEvent.ACTION_DOWN")
                // start button to continue
                mThreeP = shootArrow.threeP.copy()
                mOldPosX = event.x
                if (startRect.contains(posX, posY)) {
                    // start button was pressed
                    // start playing
                    // status = firstStageStatus;   // moved to ballGoThread.run()
                    // start running the threads
                    ballGoThread?.start()
                    gameViewDrawThread?.start()
                }
            } else if ((status >= FIRST_STAGE) && (status < FINISHED_STATUS)) {
                // in playing status
                if (startRect.contains(posX, posY)) {
                    if (!isPausedByUser) {  // not in pause status
                        // pause button was pressed
                        synchronized(gameLock) {
                            isPausedByUser = true
                        }
                        // draw resume bitmap using redraw the whole screen of game view
                        drawGameScreen()
                    } else {
                        // resume button was pressed
                        synchronized(gameLock) {
                            isPausedByUser = false
                            gameLock.notifyAll()
                        }
                        // draw pause bitmap using redraw the whole screen of game view
                        // drawGameScreen();// no need to do it because it will be done in doDraw() when threads start running
                    }
                }
                if (!isPausedByUser) {
                    LogUtil.d(TAG, "onTouchEvent.MotionEvent.ACTION_DOWN")
                    // int xSpeed = 6; // 6 pixels  original
                    val xSpeed = 10 //  10 pixels
                    // not in pause status
                    if (rightArrowRect.contains(posX, posY)) {
                        // for new function, right arrow
                        // then move the banner to right
                        buttonHoldThread?.setBannerMoveSpeed(xSpeed)
                        buttonHoldThread?.setIsButtonHold(true)
                    } else if (leftArrowRect.contains(posX, posY)) {
                        // for new function left arrow
                        // then move the banner to left
                        buttonHoldThread?.setBannerMoveSpeed(-xSpeed)
                        buttonHoldThread?.setIsButtonHold(true)
                    } else {
                        // nothing will happen
                    }
                }
            } else {
                // in failed or finished status
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                buttonHoldThread?.setIsButtonHold(false)
            else -> {}
        }

        // return super.onTouchEvent(event);
        return true // must return true
    }

    override fun surfaceChanged(
        holder: SurfaceHolder,
        format: Int,
        width: Int,
        height: Int
    ) {
        LogUtil.d(TAG, "surfaceChanged")
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        // Draw the first screen when surface view has been created
        LogUtil.d(TAG, "surfaceCreated.width = $width")
        LogUtil.d(TAG, "surfaceCreated.height = $height")
        LogUtil.d(TAG, "surfaceCreated.isGameJustCreated = $isGameJustCreated")
        if (isGameJustCreated) {
            gameViewWidth = width
            gameViewHeight = height

            // the followings were moved from constructor
            initBitmapAndModels()
            // obstacleThreads must be created before ballGoThread
            obstacleThreads = Vector<ObstacleThread>()
            // ballGoThread must be created before other threads except obstacleThreads
            ballGoThread = BallGoThread(this)
            gameViewDrawThread = GameViewDrawThread(this)
            buttonHoldThread = ButtonHoldThread(this)
            buttonHoldThread!!.start()
            isGameJustCreated = false
        }
        drawGameScreen()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        // destroy and release the process
        LogUtil.d(TAG, "SurfaceView being destroyed")
    }

    fun gameBecomeInvisible() {
        synchronized(mainLock) {
            isGameVisible = false
        }
    }

    fun gameBecomeVisible() {
        synchronized(mainLock) {
            isGameVisible = true
            mainLock.notifyAll()
        }
    }

    fun releaseSync() {
        if (!isGameVisible) {
            // in pause status
            synchronized(mainLock) {
                isGameVisible = true
                mainLock.notifyAll()
            }
        }

        if (isPausedByUser) {
            // GameView in pause status
            synchronized(gameLock) {
                isPausedByUser = false
                gameLock.notifyAll()
            }
        }
    }

    fun stopThreads() {    // executed when user failed or won
        LogUtil.d(TAG, "stopThreads")
        ballGoThread?.flag = false // stop moving
        var retry: Boolean
        gameViewDrawThread?.let {
            it.setKeepRunning(false)
            retry = true
            while (retry) {
                try {
                    LogUtil.d(TAG, "stopThreads.gameViewDrawThread.Join()")
                    it.join()
                    retry = false
                } catch (ex: InterruptedException) {
                    LogUtil.e(TAG, "stopThreads.gameViewDrawThread.InterruptedException", ex)
                } // continue processing until the thread ends
            }
        }
        LogUtil.d(TAG, "stopThreads.gameViewDrawThread.stopped")

        obstacleThreads?.let {
            for (obstacleThread in it) {
                if (obstacleThread != null) {
                    obstacleThread.setKeepRunning(false)
                    retry = true
                    while (retry) {
                        try {
                            LogUtil.d(TAG, "stopThreads.obstacleThread.Join()")
                            obstacleThread.join()
                            retry = false
                        } catch (ex: InterruptedException) {
                            LogUtil.e(TAG, "stopThreads.obstacleThreads.InterruptedException", ex)
                        } // continue processing until the thread ends
                    }
                }
            }
            it.clear()
        }
        LogUtil.d(TAG, "stopThreads.obstacleThreads.stopped")

        buttonHoldThread?.let {
            it.setIsButtonHold(false)
            it.setKeepRunning(false)
            retry = true
            while (retry) {
                try {
                    LogUtil.d(TAG, "stopThreads.buttonHoldThread.Join()")
                    it.join()
                    retry = false
                } catch (ex: InterruptedException) {
                    LogUtil.e(TAG, "stopThreads.buttonHoldThread.InterruptedException", ex)
                } // continue processing until the thread ends
            }
        }
        LogUtil.d(TAG, "stopThreads.buttonHoldThread.stopped")

        ballGoThread?.let {
            it.setKeepRunning(false)
            retry = true
            while (retry) {
                try {
                    LogUtil.d(TAG, "stopThreads.ballGoThread.Join()")
                    it.join()
                    retry = false
                } catch (ex: InterruptedException) {
                    LogUtil.e(TAG, "stopThreads.ballGoThread.InterruptedException", ex)
                } // continue processing until the thread ends
            }
        }
        LogUtil.d(TAG, "stopThreads.ballGoThread.stopped")
    }

    private fun renewGame() {
        gameViewDrawThread?.setKeepRunning(false)
        ballGoThread?.setKeepRunning(false)
        obstacleThreads?.let {
            for (obstacleThread in it) {
                obstacleThread.setKeepRunning(false)
            }
            it.clear()
        }

        // stopThreads()
        initBallAndBanner()
        score = 0
        status = START_STATUS
        ballGoThread = BallGoThread(this)
        gameViewDrawThread = GameViewDrawThread(this)

        drawGameScreen()
    }

    private fun getBitmapFromResourceWithText(
        resultId: Int,
        caption: String,
        textColor: Int
    ): Bitmap {
        val textVector = Vector<String>()
        var indexBegin = 0
        var indexEnd = 0
        while (indexEnd >= 0) {
            indexEnd = caption.indexOf('\n', indexBegin)
            if (indexEnd >= 0) {
                val temp = caption.substring(indexBegin, indexEnd)
                textVector.addElement(temp)
                indexBegin = indexEnd + 1 // skip char '\n'
            } else {
                // indexEnd = -1
                textVector.addElement(caption.substring(indexBegin))
            }
        }

        var bm: Bitmap?
        val options = BitmapFactory.Options()
        options.inMutable = true
        bm = BitmapFactory.decodeResource(resources, resultId, options)
        val canvas = Canvas(bm)
        // draw start button
        val paint = Paint()
        paint.setColor(textColor)
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD))
        paint.textAlign = Paint.Align.CENTER
        paint.isAntiAlias = true
        var fontSize = 40.0f
        paint.textSize = fontSize
        val bounds = Rect()
        paint.getTextBounds(caption, 0, caption.length, bounds)
        val realTextWidth = canvas.width - fontSize
        fontSize = fontSize * realTextWidth / bounds.width()
        paint.textSize = fontSize

        // for align.CENTER
        val fm = Paint.FontMetrics()
        paint.getFontMetrics(fm)

        // canvas.drawText(caption, canvas.getWidth()/2, canvas.getHeight()/2 - (fm.ascent+fm.descent)/2, paint);
        val lenVector = textVector.size
        val areaWidthPerRow = (canvas.height - (fm.ascent + fm.descent)) / lenVector.toFloat()
        var centerPos = areaWidthPerRow / 2.0f

        for (i in 0..<lenVector) {
            val temp = textVector.elementAt(i)
            // canvas.drawText(temp, leftPos, topPos, paint);
            canvas.drawText(temp, (canvas.width / 2).toFloat(), centerPos, paint)
            centerPos += areaWidthPerRow
        }

        return bm
    }

    fun drawGameScreen() {
        // Draw the screen of the game view
        surfaceHolder?.let {
            var canvas: Canvas? = null
            try {
                canvas = it.lockCanvas(null)
                if (canvas != null) {
                    synchronized(it) {
                        doDraw(canvas)
                    }
                } else {
                    LogUtil.e(TAG, "Canvas is null.")
                }
            } catch (e: Exception) {
                LogUtil.e(TAG, "drawGameScreen.Exception: ", e)
            } finally {
                if (canvas != null) {
                    it.unlockCanvasAndPost(canvas)
                }
            }
        }
    }
}
