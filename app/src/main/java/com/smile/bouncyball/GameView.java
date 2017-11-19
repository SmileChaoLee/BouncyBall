package com.smile.bouncyball;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.os.Handler;

import com.smile.bouncyball.models.Banner;
import com.smile.bouncyball.models.BouncyBall;

import java.util.Vector;

public class GameView extends SurfaceView implements SurfaceHolder.Callback{

    public static final int failedStatus = -1;
    public static final int startStatus = 0;
    public static final int firstStageStatus = 1;
    public static final int secondStageStatus = 2;
    public static final int thirdStageStatus = 3;
    public static final int finalStageStatus = 4;
    public static final int finishedStatus = 5;

    public static final int BouncyBall_RIGHT_TOP = 0; // going to right top
    public static final int BouncyBall_LEFT_TOP = 3; // going to left top
    public static final int BouncyBall_RIGHT_BOTTOM = 1; // going to right bottom
    public static final int BouncyBall_LEFT_BOTTOM = 2; // going to left bottom

    public Handler gameViewHandler = null;  // for synchronizing
    public boolean gameViewPause = false;   // for synchronizing

    // for running a thread when arrow button (left arrow or right arrow) is held
    public ButtonHoldThread buttonHoldThread = null;

    final private float ballSizeRatio = 1.0f/18.f;
    final private float hintWidthRatio   = 1.0f/1.5f;
    final private float hintHeightRatio = 1.0f/8.0f;
    final private float buttonWidthRatio = 1.0f/4.0f;
    final private float buttonHeightRatio = 1.0f/12.0f;
    final private float scoreWidthRatio  = 1.0f/12.0f;
    final private float scoreHeightRatio = 1.0f/20.0f;

    private MainActivity activity = null;		//Activity

    private int backCols=0;
    private int backRows=0;
    private int backSize=16;          // the size of background
    private int beginWidth  = 100;           // width of the hint
    private int beginHeight = 20;         // height of the hint
    private int gameoverWidth = 100;
    private int gameoverHeight = 20;
    private int winWidth = 100;
    private int winHeight = 20;
    private int leftArrowWidth = 100;
    private int leftArrowHeight = 20;
    private int startWidth = 100;
    private int startHeight = 20;
    private int rightArrowWidth = 100;
    private int rightArrowHeight = 20;
    private int scoreWidth = 32;
    private int scoreHeight = 32;
    private int bottomY = 0;            // the coordinate of Y-axis hitting the banner;

    private Rect ibeginRect = new Rect(0,0,0,0);   // rectangle area for hint to start
    private Rect igameoverRect = new Rect(0,0,0,0);   // rectangle area for message for game over
    private Rect iwinRect = new Rect(0,0,0,0);   // rectangle area for message for winning
    private Rect startRect  = new Rect(0,0,0,0);   // rectangle area for start game
    private Rect rightArrowRect   = new Rect(0,0,0,0);   // rectangle area for right arrow
    private Rect leftArrowRect = new Rect(0,0,0,0);   // rectangle area for left arrow
    
    private Bitmap iback;// background picture
    private Bitmap ibanner;// banner picture
    private Bitmap ibegin;//  begin picture
    private Bitmap igameover;// game over picrture
    private Bitmap iwin;// winning picture
    private Bitmap ileftarrow;  // left arrow picture
    private Bitmap istart;   // start picture
    private Bitmap ipause;   // pause picture
    private Bitmap iresume;  // resume picture
    private Bitmap irightarrow;    // right arrow picture
    private Bitmap[] iscore = new Bitmap[10];// score pictures (pictures for numbers)

    private float bannerWidthRatio  = 1.0f/4.0f;
    private float bannerHeightRatio = 1.0f/10.0f;
    private SurfaceHolder surfaceHolder = null;
    private int synchronizeTime = 70;

    private int highest = 999;  // maximum value of the number that banner is to be hit to make user win
    // -1-> failed and game over, 0->waiting to start, 1->first stage (playing), 2->second stage (playing)
    // 3->final stage (playing), 4-finished the game
    private int[] stageScore = {0, 20, 60, 120,200};    // hits for each stage
    private int status = startStatus;
    private int score=0;     //  score that user got

    private int screenWidth  = 0;
    private int screenHeight = 0;

    private BouncyBall bouncyBall = null;
    private Banner banner = null;

    private BallGoThread ballGoThread = null;			//BallGoThread
    private GameViewDrawThread gameViewDrawThread = null;
    private Vector<ObstacleThread> obstacleThreads = null;

	public GameView(MainActivity activity) {
		super(activity);

        // Display d = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

        gameViewHandler = new Handler(Looper.getMainLooper());  // for synchronizing
        gameViewPause = false;   // for synchronizing

        surfaceHolder = getHolder();
		surfaceHolder.addCallback(this); // register the interface

		this.activity = activity;
        this.screenWidth  = activity.getScreenWidth();
        this.screenHeight = activity.getScreenHeight();
        this.synchronizeTime = 70;

        status = startStatus;    // waiting to start

        initBitmapAndModels();

        // setWillNotDraw(false);  // added on 2017-11-07 to activate onDraw() of SurfaceView
        // if set it to false, then the drawing might have to go through by onDraw() all the time

        setWillNotDraw(true);   // added on 2017-11-07 for just in case, the default is false

        // obstacleThreads must be created before ballGoThread
        obstacleThreads = new Vector<ObstacleThread>();
        // ballGoThread must be created before other threads except obstacleThreads
        ballGoThread = new BallGoThread(this);
        gameViewDrawThread = new GameViewDrawThread(this);
        buttonHoldThread = new ButtonHoldThread(this);
        buttonHoldThread.start();

        System.out.println("GameView-->Constructor\n");
	}

	public SurfaceHolder getSurfaceHolder() {
	    return this.surfaceHolder;
    }

	public void initBitmapAndModels(){

        int ballSize = 16;          // size of the ball
        int ballRadius = ballSize/2;
        int ballX;       //  coordinate (x-axis) of the ball
        int ballY;       //  coordinate (y-axis) of the ball
        int ballSpan = 8;  // speed of the ball
        Bitmap iball;   // ball picture

        int bannerX;     //  the coordinate (x-axis) of the banner
        int bannerY;     //  the coordinate (y-axis) of the banner
        int bannerWidth=40;       // width of the banner
        int bannerHeight=6;       // height of the banner

		iscore[0] = BitmapFactory.decodeResource(getResources(), R.drawable.d0);
		iscore[1] = BitmapFactory.decodeResource(getResources(), R.drawable.d1);
		iscore[2] = BitmapFactory.decodeResource(getResources(), R.drawable.d2);
		iscore[3] = BitmapFactory.decodeResource(getResources(), R.drawable.d3);
		iscore[4] = BitmapFactory.decodeResource(getResources(), R.drawable.d4);
		iscore[5] = BitmapFactory.decodeResource(getResources(), R.drawable.d5);
		iscore[6] = BitmapFactory.decodeResource(getResources(), R.drawable.d6);
		iscore[7] = BitmapFactory.decodeResource(getResources(), R.drawable.d7);
		iscore[8] = BitmapFactory.decodeResource(getResources(), R.drawable.d8);
		iscore[9] = BitmapFactory.decodeResource(getResources(), R.drawable.d9);

        iback = BitmapFactory.decodeResource(getResources(), R.drawable.back);
		iball = BitmapFactory.decodeResource(getResources(), R.drawable.ball);
		ibanner = BitmapFactory.decodeResource(getResources(), R.drawable.banner);
		ibegin = BitmapFactory.decodeResource(getResources(), R.drawable.begin);
		igameover = BitmapFactory.decodeResource(getResources(), R.drawable.gameover);
		iwin = BitmapFactory.decodeResource(getResources(), R.drawable.win);

        bannerWidth = (int)((float)screenWidth   * bannerWidthRatio);         // width of the banner
        bannerHeight = (int)((float)screenHeight * bannerHeightRatio);      // height of the banner
        ballSize   = (int)((float)screenHeight * ballSizeRatio);            // size of the ball
        ballRadius = ballSize/2;
        beginWidth  = (int)((float)screenWidth  * hintWidthRatio);          // width of the hint
        beginHeight = (int)((float)screenHeight * hintHeightRatio);        // height of the hint
        gameoverWidth = (int)((float)screenWidth   * hintWidthRatio);
        gameoverHeight = (int)((float)screenHeight * hintHeightRatio);
        winWidth = (int)((float)screenWidth   * hintWidthRatio);
        winHeight = (int)((float)screenHeight * hintHeightRatio);
        leftArrowWidth = (int)((float)screenWidth   * buttonWidthRatio);
        leftArrowHeight = (int)((float)screenHeight * buttonHeightRatio * 1.5); // 1.5 * normal button
        startWidth = (int)((float)screenWidth   * buttonWidthRatio);
        startHeight = (int)((float)screenHeight * buttonHeightRatio);
        rightArrowWidth = (int)((float)screenWidth   * buttonWidthRatio);
        rightArrowHeight = (int)((float)screenHeight * buttonHeightRatio * 1.5); // 1.5 * normal button
        scoreWidth = (int)((float)screenWidth   * scoreWidthRatio);
        scoreHeight = (int)((float)screenHeight * scoreHeightRatio);

        ileftarrow = getBitmapFromResourceWithText(R.drawable.leftarrow, "",Color.BLUE);    // no string
        istart = getBitmapFromResourceWithText(R.drawable.start, activity.startStr,Color.BLUE);
        ipause = getBitmapFromResourceWithText(R.drawable.pause, activity.pauseStr,Color.YELLOW);
        iresume = getBitmapFromResourceWithText(R.drawable.resume, activity.resumeStr,Color.BLUE);
        irightarrow = getBitmapFromResourceWithText(R.drawable.rightarrow, "",Color.RED);   // no string

        ibegin = getBitmapFromResourceWithText(R.drawable.begin, activity.beginStr,Color.BLUE);
        igameover = getBitmapFromResourceWithText(R.drawable.gameover, activity.gameOverStr,Color.BLUE);
        iwin = getBitmapFromResourceWithText(R.drawable.win, activity.winStr,Color.BLUE);

        int biasX = 10;
        int biasY = 5;

        Point sPoint = new Point(biasX,screenHeight - leftArrowHeight - biasY);
        leftArrowRect.set(sPoint.x, sPoint.y, sPoint.x + leftArrowWidth, sPoint.y + leftArrowHeight);
        sPoint.set((screenWidth-startWidth)/2,screenHeight - startHeight - biasY);
        startRect.set(sPoint.x, sPoint.y, sPoint.x + startWidth, sPoint.y + startHeight);
        sPoint.set(screenWidth - rightArrowWidth - biasX, screenHeight - rightArrowHeight - biasY);
        rightArrowRect.set(sPoint.x, sPoint.y, sPoint.x + rightArrowWidth, sPoint.y + rightArrowHeight);

        // bottomY = screenHeight - bannerHeight - rightArrowHeight - screenHeight/20;
        bottomY = screenHeight - bannerHeight - startHeight - screenHeight/20;

        int numB = (int)(bottomY/ballSize);
        bottomY = numB * ballSize;

        sPoint.set((screenWidth - beginWidth)/2,(bottomY - beginHeight)/2);
        ibeginRect.set(sPoint.x,sPoint.y,sPoint.x + beginWidth,sPoint.y + beginHeight);
        sPoint.set((screenWidth - gameoverWidth)/2,(bottomY - gameoverHeight)/2);
        igameoverRect.set(sPoint.x,sPoint.y,sPoint.x + gameoverWidth,sPoint.y + gameoverHeight);
        sPoint.set((screenWidth - winWidth)/2,(bottomY - winHeight)/2);
        iwinRect.set(sPoint.x,sPoint.y,sPoint.x + winWidth,sPoint.y + winHeight);

        backCols = screenWidth/backSize;   // number of columns
        if (screenWidth%backSize!=0) {
            backCols++;
        }
        backRows = screenHeight/backSize; // number of rows
        if (screenHeight%backSize!=0) {
            backRows++;
        }

        ballSpan = ballRadius ;
		// initialize the coordinates of the ball and the banner
        ballX = screenWidth / 2;
        ballY = bottomY - ballRadius;
        bouncyBall = new BouncyBall(ballX, ballY, ballSize, ballSpan, iball);

        bannerX = screenWidth / 2;
        bannerY = bottomY + (bannerHeight/2);
        banner = new Banner(bannerX, bannerY, bannerWidth, bannerHeight, ibanner);

		// initBallAndBanner();
	}

	public void initBallAndBanner(){
		// initialize the coordinates of the ball
        // (ballX,ballY) is the center of the circle

        bouncyBall.setBallX(screenWidth / 2);
        bouncyBall.setBallY(bottomY - bouncyBall.getBallRadius());

		// initialize the coordinates of the banner
        // (bannerX,bannerY) is the center of banner

		banner.setBannerX(screenWidth / 2);
		banner.setBannerY(bottomY + banner.getBannerHeight()/2);
	}

    @Override
    public void onDraw(Canvas canvas) {
        doDraw(canvas);
    }

	public void doDraw(Canvas canvas) {

    	// clear the background
        Point sPoint = new Point(0,0);
        Rect rect2 = new Rect(0,0,0,0);
        for(int i=0;i<backRows;i++){
    		for(int j=0;j<backCols;j++) {
                rect2.set(backSize*j,backSize*i,backSize*(j+1),backSize*(i+1));
    			canvas.drawBitmap(iback, null, rect2, null);
    		}
    	}
    	//

    	// draw the banner
        sPoint.set(banner.getBannerX()-banner.getBannerWidth()/2,banner.getBannerY()-banner.getBannerHeight()/2);
        rect2.set(sPoint.x,sPoint.y,sPoint.x+banner.getBannerWidth(),sPoint.y+banner.getBannerHeight());
    	canvas.drawBitmap(ibanner, null ,rect2, null);
    	//

        ballGoThread.drawBouncyBall(canvas);

        // draw obstacles
        for (ObstacleThread obstacleThread:obstacleThreads) {
            obstacleThread.drawObstacle(canvas);
        }

        // draw left arrow button
        canvas.drawBitmap(ileftarrow ,null ,leftArrowRect ,null);
        //

        // draw right Arrow button
        canvas.drawBitmap(irightarrow, null, rightArrowRect,null);
        //

        // verifying score and status
        score = ballGoThread.getScore();
        status = ballGoThread.getStatus();

    	if(status == startStatus){
            // draw the hint of beginning
    		canvas.drawBitmap(ibegin, null, ibeginRect, null);
            // start button
            canvas.drawBitmap(istart, null, startRect, null);
    	} else {
    	    if (gameViewPause) {
    	        // under pause status. show resume button
                canvas.drawBitmap(iresume, null, startRect, null);
            } else {
                // under playing status, show pause button
                canvas.drawBitmap(ipause, null, startRect, null);
            }

            int obsSize = obstacleThreads.size();
            int numOfObstacles = 0;
    	    if (status == failedStatus) {
                // draw the hint of fail
                canvas.drawBitmap(igameover, null, igameoverRect, null);
                ballGoThread.setKeepRunning(false);  // stop running the BallGoThread, added on 2017-11-07
                gameViewDrawThread.setKeepRunning(false);  // added on 2017-11-07
                for (ObstacleThread obstacleThread : obstacleThreads) {
                    obstacleThread.setKeepRunning(false);
                }
                obstacleThreads.clear();
            } else if (status == finishedStatus) {
                // draw the picture of winning
                canvas.drawBitmap(iwin, null, iwinRect, null);
                ballGoThread.setKeepRunning(false);  // stop running the BallGoThread, added on 2017-11-07
                gameViewDrawThread.setKeepRunning(false);  // added on 2017-11-07
                for (ObstacleThread obstacleThread:obstacleThreads) {
                    obstacleThread.setKeepRunning(false);
                }
                obstacleThreads.clear();
            } else if ( (status >= secondStageStatus) && (status <= finalStageStatus) ) {
    	        // stage 1 to stage 4
                // 1 obstacle for stage 2, 2 obstacles for stage 3, 3 obstacles for stage 4(final stage)
                numOfObstacles = status - 1;
                if (obsSize < numOfObstacles) {
                    for (int i = obsSize; i < numOfObstacles; i++) {
                        ObstacleThread obstacleThread = new ObstacleThread(this, i+1);
                        obstacleThreads.addElement(obstacleThread);
                        obstacleThread.start();
                    }
                }
            } else {
    	        // first stage, do nothing
            }
        }

        // draw score, action bar is on the main UI thread
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if ( (status >= startStatus) && (status <= finalStageStatus) ) {
                    activity.stageName.setText(activity.stageLevels[status]);
                }

                String scoreStr = score + "";
                int loop = 3 - scoreStr.length();
                for(int i=0;i<loop;i++){
                    scoreStr = "0" + scoreStr;
                }
                int tempScore = scoreStr.charAt(2)-'0';
                activity.scoreImage0.setImageBitmap(iscore[tempScore]);
                tempScore = scoreStr.charAt(1)-'0';
                activity.scoreImage1.setImageBitmap(iscore[tempScore]);
                tempScore = scoreStr.charAt(0)-'0';
                activity.scoreImage2.setImageBitmap(iscore[tempScore]);
            }
        });
        //
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

        int x = (int) event.getX();
        int y = (int) event.getY();

        int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_MOVE:
                // Log.d(MainActivity.Constants.LOG,"MotionEvent.ACTION_MOVE");
                if ( (status >= firstStageStatus) && (status < finishedStatus) ) {
                    if (!gameViewPause) {
                        if ((y >= bottomY) && (y <= (bottomY + banner.getBannerHeight()))) {
                            // Y - coordinate is inside the area
                            banner.setBannerX(x);
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_BUTTON_PRESS:
            case MotionEvent.ACTION_DOWN:
                if(status == startStatus) {
                    // start button to continue
                    if (startRect.contains(x, y)) {
                        // start button was pressed
                        // start playing
                        // status = firstStageStatus;   // moved to ballGoThread.run()
                        // start running the threads
                        ballGoThread.start();
                        gameViewDrawThread.start();
                    }
                } else if ( (status >= firstStageStatus) && (status < finishedStatus) ) {
                    // in playing status
                    if (startRect.contains(x, y)) {
                        if (!gameViewPause) {  // not in pause status
                            // pause button was pressed
                            synchronized (gameViewHandler) {
                                gameViewPause = true;
                            }
                            // draw resume bitmap using redraw the whole screen of game view
                            drawGameScreen();
                        } else {
                            // resume button was pressed
                            synchronized (gameViewHandler) {
                                gameViewPause = false;
                                gameViewHandler.notifyAll();
                            }
                            // draw pause bitmap using redraw the whole screen of game view
                            // drawGameScreen();// no need to do it because it will be done in doDraw() when threads start running
                        }
                    }
                    if (!gameViewPause) {
                        int xSpeed = 6; // 6 pixels
                        int bannerX = banner.getBannerX();
                        // not in pause status
                        if (rightArrowRect.contains(x, y)) {
                            // for new function, right arrow
                            // then move the banner to right
                            buttonHoldThread.setBannerMoveSpeed(xSpeed);
                            buttonHoldThread.setIsButtonHold(true);
                        } else if (leftArrowRect.contains(x, y)) {
                            // for new function left arrow
                            // then move the banner to left
                            buttonHoldThread.setBannerMoveSpeed(-xSpeed);
                            buttonHoldThread.setIsButtonHold(true);
                        } else {
                            // nothing will happen
                        }
                    }
                } else {
                    // in failed or finished status
                }

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                buttonHoldThread.setIsButtonHold(false);
                break;
            default:
                // get the coordinates of point touched
                break;
        }

		// return super.onTouchEvent(event);
        return true;   // must return true
	}

    public MainActivity getActivity() {
        return this.activity;
    }
    public int getSynchronizeTime() {
	    return this.synchronizeTime;
    }
    public int getScreenWidth() {
	    return this.screenWidth;
    }
    public int getScreenHeight() {
	    return this.screenHeight;
    }
    public int getBottomY() {
	    return this.bottomY;
    }
    public BouncyBall getBouncyBall() {
	    return this.bouncyBall;
    }
    public Banner getBanner() {
	    return this.banner;
    }
    public BallGoThread getBallGoThread() {
	    return this.ballGoThread;
    }
    public Vector<ObstacleThread> getObstacleThreads() {
	    return this.obstacleThreads;
    }
    public void newGame(){
        // if(status==failedStatus||status==finishedStatus){
        // initialize the coordinates of the ball and the banner

        if (gameViewDrawThread != null) {
            gameViewDrawThread.setKeepRunning(false);
        }
        if (ballGoThread != null) {
            ballGoThread.setKeepRunning(false);
        }
        if (obstacleThreads != null) {
            for (ObstacleThread obstacleThread:obstacleThreads) {
                obstacleThread.setKeepRunning(false);
            }
            obstacleThreads.clear();
        }

        initBallAndBanner();
        score = 0;
        status = startStatus;
        ballGoThread = new BallGoThread(this);
        gameViewDrawThread = new GameViewDrawThread(this);

        drawGameScreen();
        // }
    }
    public void stopThreads() {    // executed when user failed or won
        if (ballGoThread != null) {
            ballGoThread.setFlag(false);    // stop moving
        }

        boolean retry = true;
        if (gameViewDrawThread != null) {
            gameViewDrawThread.setKeepRunning(false);
            retry = true;
            while (retry) {
                try {
                    gameViewDrawThread.join();
                    System.out.println("gameViewDrawThread.Join()........\n");
                    retry = false;
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }// continue processing until the thread ends
            }
        }

        for (ObstacleThread obstacleThread:obstacleThreads) {
            if (obstacleThread != null) {
                obstacleThread.setKeepRunning(false);
                retry = true;
                while (retry) {
                    try {
                        obstacleThread.join();
                        System.out.println("obstacleThread.Join()........\n");
                        retry = false;
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }// continue processing until the thread ends
                }
            }
        }
        obstacleThreads.clear();

        if (buttonHoldThread != null) {
            buttonHoldThread.setIsButtonHold(false);
            buttonHoldThread.setKeepRunning(false);
            retry = true;
            while (retry) {
                try {
                    buttonHoldThread.join();
                    System.out.println("buttonHoldThread.Join()........\n");
                    retry = false;
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }// continue processing until the thread ends
            }
        }

        if (ballGoThread != null) {
            ballGoThread.setKeepRunning(false);
            retry = true;
            while (retry) {
                try {
                    ballGoThread.join();
                    System.out.println("ballGoThread.Join().......\n");
                    retry = false;
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }// continue processing until the thread ends
            }
        }
    }

	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

	public void surfaceCreated(SurfaceHolder holder) {
        // Draw the first screen when surface view has been created
        drawGameScreen();
    }

	public void surfaceDestroyed(SurfaceHolder holder) {
	    // destroy and release the process
        System.out.println("SurfaceView being destroyed");
	}

	private Bitmap getBitmapFromResourceWithText(int resultId, String caption, int textColor) {

        Vector<String> textVector = new Vector<String>();
        int indexBegin = 0;
        int indexEnd = 0;
        while (indexEnd >= 0) {
            indexEnd = caption.indexOf('\n',indexBegin);
            if (indexEnd >= 0 ) {
                String temp = caption.substring(indexBegin, indexEnd);
                textVector.addElement(temp);
                indexBegin = indexEnd + 1;  // skip char '\n'
            } else {
                // indexEnd = -1
                textVector.addElement(caption.substring(indexBegin));
            }
            // System.out.println(caption + " of indexEnd = " + indexEnd);
        }

	    Bitmap bm = null;
	    BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inMutable = true;
        bm = BitmapFactory.decodeResource(getResources(), resultId, options);
        Canvas canvas = new Canvas(bm);
        // draw start button
        Paint paint = new Paint();
        paint.setColor(textColor);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setAntiAlias(true);
        float fontSize = 40.0f;
        paint.setTextSize(fontSize);
        Rect bounds = new Rect();
        paint.getTextBounds(caption,0,caption.length(),bounds);
        float realTextWidth = canvas.getWidth() - fontSize;
        fontSize = fontSize * realTextWidth / bounds.width();
        paint.setTextSize(fontSize);

        // for align.CENTER
        Paint.FontMetrics fm = new Paint.FontMetrics();
        paint.getFontMetrics(fm);
        // canvas.drawText(caption, canvas.getWidth()/2, canvas.getHeight()/2 - (fm.ascent+fm.descent)/2, paint);

        int lenVector = textVector.size();
        float areaWidthPerRow = (canvas.getHeight() - (fm.ascent+fm.descent)) / (float)lenVector;
        float centerPos = areaWidthPerRow/2.0f;

        for (int i=0; i<lenVector; i++) {
            String temp = textVector.elementAt(i);
            // canvas.drawText(temp, leftPos, topPos, paint);
            canvas.drawText(temp, canvas.getWidth()/2, centerPos, paint);
            centerPos += areaWidthPerRow;
        }

	    return bm;
    }

    private void drawGameScreen() {

        // Draw the screen of the game view
        Canvas canvas = null;
        try {
            canvas = surfaceHolder.lockCanvas(null);
            if (canvas != null) {
                synchronized (surfaceHolder) {
                    doDraw(canvas);
                }
            } else {
                System.out.println("Canvas is null.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (canvas != null) {
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }
}
