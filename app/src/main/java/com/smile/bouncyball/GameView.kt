package com.smile.bouncyball

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
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
import com.smile.bouncyball.threads.BallGoThread
import com.smile.bouncyball.threads.ButtonHoldThread
import com.smile.bouncyball.threads.GameViewDrawThread
import com.smile.bouncyball.threads.ObstacleThread
import com.smile.smilelibraries.scoresqlite.ScoreSQLite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("ViewConstructor")
class GameView(private val mainActivity: MainActivity)
    : SurfaceView(mainActivity),
    SurfaceHolder.Callback {
    companion object {
        private const val TAG = "BouncyBall.GameVew"
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
        private const val BANNER_HEIGHT_RATIO = 1.0f / 10.0f
    }


    @JvmField
    val mainLock = Object()
    @JvmField
    val gameLock = Object() // for synchronizing
    @JvmField
    val synchronizeTime = 70
    @JvmField
    var isGameVisible = true
    @JvmField
    var isPausedByUser = false
    // for running a thread when arrow button (left arrow or right arrow) is held
    var buttonHoldThread: ButtonHoldThread? = null
    // private var beginWidth = 100 // width of the hint
    // private var beginHeight = 20 // height of the hint
    // private var leftArrowWidth = 100
    // private var leftArrowHeight = 20
    // private var startWidth = 100
    // private var startHeight = 20
    // private var rightArrowWidth = 100
    // private var rightArrowHeight = 20
    // private var scoreWidth = 32
    // private var scoreHeight = 32
    // the coordinate of Y-axis hitting the banner
    var bottomY: Int = 0
        private set
    private val iBeginRect = Rect(0, 0, 0, 0) // rectangle area for hint to start
    private val iGOverRect = Rect(0, 0, 0, 0) // rectangle area for gae over
    private val startRect = Rect(0, 0, 0, 0) // rectangle area for start game
    private val rightArrowRect = Rect(0, 0, 0, 0) // rectangle area for right arrow
    private val leftArrowRect = Rect(0, 0, 0, 0) // rectangle area for left arrow
    private var iBack: Bitmap? = null // background picture
    private var iBanner: Bitmap? = null // banner picture
    private var iBegin: Bitmap? = null //  begin picture
    private var iGameOver: Bitmap? = null //  game over picture
    private var iLeftArrow: Bitmap? = null // left arrow picture
    private var iStart: Bitmap? = null // start picture
    private var iPause: Bitmap? = null // pause picture
    private var iResume: Bitmap? = null // resume picture
    private var iRightArrow: Bitmap? = null // right arrow picture
    private val iScore = arrayOfNulls<Bitmap>(10) // score pictures (pictures for numbers)

    private var highestTextView: TextView? = null
    private var scoreImage0: ImageView? = null
    private var scoreImage1: ImageView? = null
    private var scoreImage2: ImageView? = null

    // string resources
    // private var stageLevels: Array<String>? = null
    private var startStr = ""
    private var pauseStr = ""
    private var resumeStr = ""
    private var beginStr = ""
    private var gameOverStr = ""
    var surfaceHolder: SurfaceHolder? = null
        private set

    private var status = START_STATUS
    private var score = 0 //  score that user got
    private var highestScore = 0

    var gameViewWidth: Int = 0
        private set
    var gameViewHeight: Int = 0
        private set

    var bouncyBall: BouncyBall? = null
        private set
    var banner: Banner? = null
        private set

    var ballGoThread: BallGoThread? = null //BallGoThread
        private set
    private var gameViewDrawThread: GameViewDrawThread? = null
    var obstacleThreads: Vector<ObstacleThread>? = null
        private set

    private val scoreSQLiteDB = ScoreSQLite(mainActivity,
        BouncyBallApp.DATABASE_NAME)

    init {
        val textFontSize = ScreenUtil.getPxTextFontSizeNeeded(mainActivity)
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
                ScreenUtil.resizeTextSize(highestTextView, textFontSize * 1.5f)
                scoreImage0 = abV.findViewById(R.id.scoreView0)
                scoreImage1 = abV.findViewById(R.id.scoreView1)
                scoreImage2 = abV.findViewById(R.id.scoreView2)
                // stageLevels = resources.getStringArray(R.array.stageLevels)
            }
        }

        startStr = resources.getString(R.string.start_string)
        pauseStr = resources.getString(R.string.pause_string)
        resumeStr = resources.getString(R.string.resume_string)
        beginStr = resources.getString(R.string.begin_string)
        gameOverStr = resources.getString(R.string.gameOver_string)

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

    fun initBitmapAndModels() {
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

        iBack = BitmapFactory.decodeResource(resources, R.drawable.back)
        if (iBack == null) {
            LogUtil.d(TAG, "iBack is null.")
        }
        val iBall = BitmapFactory.decodeResource(resources, R.drawable.ball) // ball picture
        iBanner = BitmapFactory.decodeResource(resources, R.drawable.banner)
        // iBegin = BitmapFactory.decodeResource(resources, R.drawable.begin)

        val bannerWidth = (gameViewWidth.toFloat() * BANNER_WIDTH_RATIO).toInt() // width of the banner
        // width of the banner
        val bannerHeight = (gameViewHeight.toFloat() * BANNER_HEIGHT_RATIO).toInt() // height of the banner
        // height of the banner
        val ballSize = (gameViewHeight.toFloat() * BALL_SIZE_RATIO).toInt() // size of the ball
        // size of the ball
        val ballRadius = ballSize / 2
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
        // val scoreWidth = (gameViewWidth.toFloat() * SORE_WIDTH_RATIO).toInt()
        // val scoreHeight = (gameViewHeight.toFloat() * SCORE_HEIGHT_RATIO).toInt()

        iLeftArrow =
            getBitmapFromResourceWithText(R.drawable.leftarrow, "", Color.BLUE) // no string
        iStart = getBitmapFromResourceWithText(R.drawable.start, startStr, Color.BLUE)
        iPause = getBitmapFromResourceWithText(R.drawable.pause, pauseStr, Color.YELLOW)
        iResume = getBitmapFromResourceWithText(R.drawable.resume, resumeStr, Color.BLUE)
        iRightArrow =
            getBitmapFromResourceWithText(R.drawable.rightarrow, "", Color.RED) // no string

        iBegin = getBitmapFromResourceWithText(R.drawable.begin, beginStr, Color.BLUE)
        iGameOver = getBitmapFromResourceWithText(R.drawable.begin, gameOverStr, Color.RED)

        val biasX = 10
        val biasY = 10

        val sPoint = Point(biasX, gameViewHeight - leftArrowHeight - biasY)
        leftArrowRect.set(sPoint.x, sPoint.y, sPoint.x + leftArrowWidth, sPoint.y + leftArrowHeight)
        sPoint.set((gameViewWidth - startWidth) / 2, gameViewHeight - startHeight - biasY)
        startRect.set(sPoint.x, sPoint.y, sPoint.x + startWidth, sPoint.y + startHeight)
        sPoint.set(
            gameViewWidth - rightArrowWidth - biasX,
            gameViewHeight - rightArrowHeight - biasY
        )
        rightArrowRect.set(
            sPoint.x,
            sPoint.y,
            sPoint.x + rightArrowWidth,
            sPoint.y + rightArrowHeight
        )

        bottomY = gameViewHeight - bannerHeight - startHeight - gameViewHeight / 20

        val numB = (bottomY / ballSize) // removed on 2018-07-08
        bottomY = numB * ballSize

        sPoint.set((gameViewWidth - beginWidth) / 2, (bottomY - beginHeight) / 2)
        iBeginRect.set(sPoint.x, sPoint.y, sPoint.x + beginWidth, sPoint.y + beginHeight)
        iGOverRect.set(sPoint.x, sPoint.y, sPoint.x + beginWidth, sPoint.y + beginHeight)

        val ballSpan = ballRadius
        // initialize the coordinates of the ball and the banner
        val ballX: Int = gameViewWidth / 2 //  coordinate (x-axis) of the ball
        val ballY = bottomY - ballRadius
        bouncyBall = BouncyBall(ballX, ballY, ballSize, ballSpan, iBall)

        val bannerX: Int = gameViewWidth / 2 //  the coordinate (x-axis) of the banner
        val bannerY = bottomY + (bannerHeight / 2)
        banner = Banner(bannerX, bannerY, bannerWidth, bannerHeight, iBanner)
    }

    fun initBallAndBanner() {
        // initialize the coordinates of the ball
        // (ballX,ballY) is the center of the circle

        bouncyBall?.apply {
            ballX = gameViewWidth / 2
            ballY = bottomY - ballRadius
        }

        // initialize the coordinates of the banner
        // (bannerX,bannerY) is the center of banner
        banner?.apply {
            bannerX = gameViewWidth / 2
            bannerY = bottomY + bannerHeight / 2
        }
    }

    override fun onDraw(canvas: Canvas) {
        LogUtil.d(TAG, "onDraw")
        doDraw(canvas)
    }

    fun doDraw(canvas: Canvas) {
        LogUtil.d(TAG, "doDraw")
        // clear the background
        val sPoint = Point(0, 0)
        val rect2 = Rect(0, 0, 0, 0)
        val rectF = RectF(0f, 0f, gameViewWidth.toFloat(), gameViewHeight.toFloat())
        iBack?.let {
            canvas.drawBitmap(it, null, rectF, null)
        }

        // draw the banner
        banner?.apply {
            sPoint.set(
                bannerX - bannerWidth / 2,
                bannerY - bannerHeight / 2
            )
            rect2.set(
                sPoint.x,
                sPoint.y,
                sPoint.x + bannerWidth,
                sPoint.y + bannerHeight
            )
        }
        iBanner?.let {
            canvas.drawBitmap(it, null, rect2, null)
        }

        //
        ballGoThread?.drawBouncyBall(canvas)

        // draw obstacles
        obstacleThreads?.let {
            for (obstacleThread in it) {
                obstacleThread.drawObstacle(canvas)
            }
        }

        // draw left arrow button
        iLeftArrow?.let {
            canvas.drawBitmap(it, null, leftArrowRect, null)
        }

        //

        // draw right Arrow button
        iRightArrow?.let {
            canvas.drawBitmap(it, null, rightArrowRect, null)
        }

        //

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
            // draw the hint of beginning
            iBegin?.let {
                canvas.drawBitmap(it, null, iBeginRect, null)
            }
            // start button
            iStart?.let {
                canvas.drawBitmap(it, null, startRect, null)
            }
        } else {
            if (isPausedByUser) {
                // under pause status. show resume button
                iResume?.let {
                    canvas.drawBitmap(it, null, startRect, null)
                }
            } else {
                // under playing status, show pause button
                iPause?.let {
                    canvas.drawBitmap(it, null, startRect, null)
                }
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
                iGameOver?.let {
                    LogUtil.d(TAG, "doDraw.iGameOver.iGOverRect")
                    canvas.drawBitmap(it, null, iGOverRect, null)
                }
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

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x.toInt()
        val y = event.y.toInt()
        LogUtil.d(TAG, "onTouchEvent.x = $x")
        val action = event.action
        when (action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_MOVE ->
                if ((status >= FIRST_STAGE) && (status < FINISHED_STATUS)) {
                    val bn = banner?: return false
                    if (!isPausedByUser) {
                        // if ((y >= (bottomY - 20)) && (y <= (bottomY + 20 + bn.bannerHeight))) {
                            // Y - coordinate is inside the area and add 20 extra pixels
                            bn.bannerX = x
                        // }
                    }
                }
            MotionEvent.ACTION_BUTTON_PRESS, MotionEvent.ACTION_DOWN -> if (status == START_STATUS) {
                LogUtil.d(TAG, "onTouchEvent.MotionEvent.ACTION_DOWN")
                // start button to continue
                if (startRect.contains(x, y)) {
                    // start button was pressed
                    // start playing
                    // status = firstStageStatus;   // moved to ballGoThread.run()
                    // start running the threads
                    ballGoThread?.start()
                    gameViewDrawThread?.start()
                }
            } else if ((status >= FIRST_STAGE) && (status < FINISHED_STATUS)) {
                // in playing status
                if (startRect.contains(x, y)) {
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
                    if (rightArrowRect.contains(x, y)) {
                        // for new function, right arrow
                        // then move the banner to right
                        buttonHoldThread?.setBannerMoveSpeed(xSpeed)
                        buttonHoldThread?.setIsButtonHold(true)
                    } else if (leftArrowRect.contains(x, y)) {
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

        //
        drawGameScreen()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        // destroy and release the process
        LogUtil.d(TAG, "SurfaceView being destroyed")
    }

    fun newGame() {
        renewGame()
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

    private fun drawGameScreen() {
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
