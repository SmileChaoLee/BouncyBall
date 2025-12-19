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
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import com.smile.bouncyball.models.Banner
import com.smile.bouncyball.models.BouncyBall
import com.smile.bouncyball.tools.LogUtil
import com.smile.smilelibraries.utilities.ScreenUtil
import java.util.Vector
import androidx.core.graphics.drawable.toDrawable

@SuppressLint("ViewConstructor")
class GameView(mainActivity: MainActivity, textFontSize: Float)
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
        private const val SORE_WIDTH_RATIO = 1.0f / 12.0f
        private const val SCORE_HEIGHT_RATIO = 1.0f / 20.0f
        private const val BANNER_WIDTH_RATIO = 1.0f / 4.0f
        private const val BANNER_HEIGHT_RATIO = 1.0f / 10.0f
    }
    @JvmField
    var gameViewPause: Boolean = false // for synchronizing
    // for running a thread when arrow button (left arrow or right arrow) is held
    var buttonHoldThread: ButtonHoldThread? = null
    var mainActivity: MainActivity? = null //Activity
        private set
    private var beginWidth = 100 // width of the hint
    private var beginHeight = 20 // height of the hint
    private var leftArrowWidth = 100
    private var leftArrowHeight = 20
    private var startWidth = 100
    private var startHeight = 20
    private var rightArrowWidth = 100
    private var rightArrowHeight = 20
    private var scoreWidth = 32
    private var scoreHeight = 32
    var bottomY: Int = 0 // the coordinate of Y-axis hitting the banner;
        private set
    private val iBeginRect = Rect(0, 0, 0, 0) // rectangle area for hint to start
    private val startRect = Rect(0, 0, 0, 0) // rectangle area for start game
    private val rightArrowRect = Rect(0, 0, 0, 0) // rectangle area for right arrow
    private val leftArrowRect = Rect(0, 0, 0, 0) // rectangle area for left arrow
    private var iBack: Bitmap? = null // background picture
    private var iBanner: Bitmap? = null // banner picture
    private var iBegin: Bitmap? = null //  begin picture
    private var iLeftArrow: Bitmap? = null // left arrow picture
    private var iStart: Bitmap? = null // start picture
    private var iPause: Bitmap? = null // pause picture
    private var iResume: Bitmap? = null // resume picture
    private var iRightArrow: Bitmap? = null // right arrow picture
    private val iScore = arrayOfNulls<Bitmap>(10) // score pictures (pictures for numbers)

    private var stageName: TextView? = null
    private var scoreImage0: ImageView? = null
    private var scoreImage1: ImageView? = null
    private var scoreImage2: ImageView? = null

    // string resources
    private var stageLevels: Array<String>? = null
    private var startStr = ""
    private var pauseStr = ""
    private var resumeStr = ""
    private var beginStr = ""
    private var gameOverStr = ""
    private var winStr = ""
    var surfaceHolder: SurfaceHolder? = null
        private set
    @JvmField
    val synchronizeTime: Int = 70

    private var status: Int = START_STATUS
    private var score = 0 //  score that user got

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

    private val textFontSize: Float

    @JvmField
    val mainLock = Object()
    @JvmField
    val gameLock = Object() // for synchronizing

    init {
        this.mainActivity = mainActivity
        this.textFontSize = textFontSize

        val actionBar = mainActivity.supportActionBar
        // actionBar.setDisplayShowTitleEnabled(false);
        actionBar?.let { abIt ->
            abIt.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
            abIt.setDisplayShowCustomEnabled(true) // enable customized action bar
            abIt.setBackgroundDrawable(Color.LTGRAY.toDrawable())
            abIt.setCustomView(R.layout.action_bar_layout)
            val actionBarView = abIt.customView
            actionBarView.let { abV ->
                stageName = abV.findViewById<View?>(R.id.stageName) as TextView
                ScreenUtil.resizeTextSize(stageName, textFontSize)
                scoreImage0 = abV.findViewById(R.id.scoreView0)
                scoreImage1 = abV.findViewById(R.id.scoreView1)
                scoreImage2 = abV.findViewById(R.id.scoreView2)
                stageLevels = resources.getStringArray(R.array.stageLevels)
                stageName?.text = stageLevels?.get(0) // start from stage 1
            }
        }

        startStr = resources.getString(R.string.start_string)
        pauseStr = resources.getString(R.string.pause_string)
        resumeStr = resources.getString(R.string.resume_string)
        beginStr = resources.getString(R.string.begin_string)
        gameOverStr = resources.getString(R.string.gameOver_string)
        winStr = resources.getString(R.string.win_string)

        gameViewPause = false // for synchronizing

        setZOrderOnTop(true)
        surfaceHolder = holder
        surfaceHolder?.let { holder ->
            holder.addCallback(this) // register the interface
            holder.setFormat(PixelFormat.TRANSLUCENT)
        }

        val highestScore = BouncyBallApp.ScoreSQLiteDB.readHighestScore()

        status = START_STATUS // waiting to start

        // setWillNotDraw(false);  // added on 2017-11-07 to activate onDraw() of SurfaceView
        // if set it to false, then the drawing might have to go through by onDraw() all the time
        setWillNotDraw(true) // added on 2017-11-07 for just in case, the default is false

        LogUtil.d(TAG, "GameView created")
    }

    fun initBitmapAndModels() {
        Log.d(TAG, "initBitmapAndModels() is called")
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
        iBegin = BitmapFactory.decodeResource(resources, R.drawable.begin)

        val bannerWidth = (gameViewWidth.toFloat() * BANNER_WIDTH_RATIO).toInt() // width of the banner
        // width of the banner
        val bannerHeight = (gameViewHeight.toFloat() * BANNER_HEIGHT_RATIO).toInt() // height of the banner
        // height of the banner
        val ballSize = (gameViewHeight.toFloat() * BALL_SIZE_RATIO).toInt() // size of the ball
        // size of the ball
        val ballRadius = ballSize / 2
        beginWidth = (gameViewWidth.toFloat() * HINT_WIDTH_RATIO).toInt() // width of the hint
        beginHeight = (gameViewHeight.toFloat() * HINT_HEIGHT_RATIO).toInt() // height of the hint
        leftArrowWidth = (gameViewWidth.toFloat() * BUTTON_WIDTH_RATIO).toInt()
        leftArrowHeight =
            (gameViewHeight.toFloat() * BUTTON_HEIGHT_RATIO * 1.5).toInt() // 1.5 * normal button
        startWidth = (gameViewWidth.toFloat() * BUTTON_WIDTH_RATIO).toInt()
        startHeight = (gameViewHeight.toFloat() * BUTTON_HEIGHT_RATIO).toInt()
        rightArrowWidth = (gameViewWidth.toFloat() * BUTTON_WIDTH_RATIO).toInt()
        rightArrowHeight =
            (gameViewHeight.toFloat() * BUTTON_HEIGHT_RATIO * 1.5).toInt() // 1.5 * normal button
        scoreWidth = (gameViewWidth.toFloat() * SORE_WIDTH_RATIO).toInt()
        scoreHeight = (gameViewHeight.toFloat() * SCORE_HEIGHT_RATIO).toInt()

        iLeftArrow =
            getBitmapFromResourceWithText(R.drawable.leftarrow, "", Color.BLUE) // no string
        iStart = getBitmapFromResourceWithText(R.drawable.start, startStr, Color.BLUE)
        iPause = getBitmapFromResourceWithText(R.drawable.pause, pauseStr, Color.YELLOW)
        iResume = getBitmapFromResourceWithText(R.drawable.resume, resumeStr, Color.BLUE)
        iRightArrow =
            getBitmapFromResourceWithText(R.drawable.rightarrow, "", Color.RED) // no string

        iBegin = getBitmapFromResourceWithText(R.drawable.begin, beginStr, Color.BLUE)

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

    public override fun onDraw(canvas: Canvas) {
        LogUtil.d(TAG, "onDraw")
        doDraw(canvas)
    }

    fun doDraw(canvas: Canvas) {
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
        mainActivity?.runOnUiThread {
            if ((status >= START_STATUS) && (status <= FINAL_STAGE)) {
                stageName?.text = stageLevels?.get(status)
            }
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
            if (gameViewPause) {
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
                ballGoThread?.setKeepRunning(false) // stop running the BallGoThread, added on 2017-11-07
                gameViewDrawThread?.setKeepRunning(false) // added on 2017-11-07
                obstacleThreads?.let {
                    for (obstacleThread in it) {
                        obstacleThread.setKeepRunning(false)
                    }
                    it.clear()
                }

                //  game over
                mainActivity?.runOnUiThread {
                    val tv = TextView(mainActivity)
                    // tv.setTextSize(40);
                    ScreenUtil.resizeTextSize(tv, textFontSize)
                    tv.setTextColor(Color.BLUE)
                    tv.setTypeface(Typeface.DEFAULT)
                    if (status == FAILED_STATUS) {
                        // failed
                        tv.text = gameOverStr
                    } else {
                        // won
                        tv.text = winStr
                    }
                    tv.setGravity(Gravity.CENTER)
                    // wait for time to restart a new game
                    recordScore(score)
                }
            } else {
                // first stage, do nothing
                LogUtil.d(TAG, "doDraw.first stage, do nothing")
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x.toInt()
        val y = event.y.toInt()

        val action = event.action
        when (action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_MOVE ->
                if ((status >= FIRST_STAGE) && (status < FINISHED_STATUS)) {
                    val bn = banner?: return false
                    if (!gameViewPause) {
                        if ((y >= (bottomY - 20)) && (y <= (bottomY + 20 + bn.bannerHeight))) {
                            // Y - coordinate is inside the area and add 20 extra pixels
                            bn.bannerX = x
                        }
                    }
                }

            MotionEvent.ACTION_BUTTON_PRESS, MotionEvent.ACTION_DOWN -> if (status == START_STATUS) {
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
                    if (!gameViewPause) {  // not in pause status
                        // pause button was pressed
                        synchronized(gameLock) {
                            gameViewPause = true
                        }
                        // draw resume bitmap using redraw the whole screen of game view
                        drawGameScreen()
                    } else {
                        // resume button was pressed
                        synchronized(gameLock) {
                            gameViewPause = false
                            gameLock.notifyAll()
                        }
                        // draw pause bitmap using redraw the whole screen of game view
                        // drawGameScreen();// no need to do it because it will be done in doDraw() when threads start running
                    }
                }
                if (!gameViewPause) {
                    // int xSpeed = 6; // 6 pixels  original
                    val xSpeed = 8 //  8 pixels
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
        println("SurfaceView being destroyed")
    }

    fun newGame() {
        renewGame()
    }

    fun releaseSync() {
        mainActivity?.let {
            if (it.gamePause) {
                // in pause status
                synchronized(mainLock) {
                    it.gamePause = false
                    mainLock.notifyAll()
                }
            }
        }

        if (gameViewPause) {
            // GameView in pause status
            synchronized(gameLock) {
                gameViewPause = false
                gameLock.notifyAll()
            }
        }
    }

    fun stopThreads() {    // executed when user failed or won
        ballGoThread?.flag = false // stop moving

        var retry: Boolean
        gameViewDrawThread?.let {
            it.setKeepRunning(false)
            retry = true
            while (retry) {
                try {
                    it.join()
                    LogUtil.d(TAG, "stopThreads.gameViewDrawThread.Join()")
                    retry = false
                } catch (ex: InterruptedException) {
                    LogUtil.e(TAG, "stopThreads.gameViewDrawThread.InterruptedException", ex)
                } // continue processing until the thread ends
            }
        }

        obstacleThreads?.let {
            for (obstacleThread in it) {
                if (obstacleThread != null) {
                    obstacleThread.setKeepRunning(false)
                    retry = true
                    while (retry) {
                        try {
                            obstacleThread.join()
                            LogUtil.d(TAG, "stopThreads.obstacleThread.Join()")
                            retry = false
                        } catch (ex: InterruptedException) {
                            LogUtil.e(TAG, "stopThreads.obstacleThreads.InterruptedException", ex)
                        } // continue processing until the thread ends
                    }
                }
            }
            it.clear()
        }

        buttonHoldThread?.let {
            it.setIsButtonHold(false)
            it.setKeepRunning(false)
            retry = true
            while (retry) {
                try {
                    it.join()
                    LogUtil.d(TAG, "stopThreads.buttonHoldThread.Join()")
                    retry = false
                } catch (ex: InterruptedException) {
                    LogUtil.e(TAG, "stopThreads.buttonHoldThread.InterruptedException", ex)
                } // continue processing until the thread ends
            }
        }

        ballGoThread?.let {
            it.setKeepRunning(false)
            retry = true
            while (retry) {
                try {
                    it.join()
                    LogUtil.d(TAG, "stopThreads.ballGoThread.Join()")
                    retry = false
                } catch (ex: InterruptedException) {
                    ex.printStackTrace()
                    LogUtil.e(TAG, "stopThreads.ballGoThread.InterruptedException", ex)
                } // continue processing until the thread ends
            }
        }
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
                e.printStackTrace()
            } finally {
                if (canvas != null) {
                    it.unlockCanvasAndPost(canvas)
                }
            }
        }
    }

    private fun recordScore(currentScore: Int) {
        // record currentScore as a score in database
        // no more sending score to the cloud
        BouncyBallApp.ScoreSQLiteDB.addScore("", currentScore)
        BouncyBallApp.ScoreSQLiteDB.deleteAllAfterTop10() // only keep the top 10
        releaseSync()
        renewGame() // no showing for ad
    }
}
