package com.smile.bouncyball;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Looper;
import android.provider.CalendarContract;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.Handler;

import com.smile.bouncyball.models.Banner;
import com.smile.bouncyball.models.BouncyBall;

import java.util.Vector;

public class GameView extends SurfaceView implements SurfaceHolder.Callback{

    public static final int failedStatus = -1;
    public static final int startStatus = 0;
    public static final int firstStageStatus = 1;
    public static final int secondStageStatus = 2;
    public static final int finalStageStatus = 3;
    public static final int finishedStatus = 4;

    public static final int BouncyBall_RIGHT_TOP = 0; // going to right top
    public static final int BouncyBall_LEFT_TOP = 3; // going to left top
    public static final int BouncyBall_RIGHT_BOTTOM = 1; // going to right bottom
    public static final int BouncyBall_LEFT_BOTTOM = 2; // going to left bottom

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
    private int replayWidth = 100;
    private int replayHeight = 20;
    private int startWidth = 100;
    private int startHeight = 20;
    private int quitWidth = 100;
    private int quitHeight = 20;
    private int scoreWidth = 32;
    private int scoreHeight = 32;
    private int bottomY = 0;            // the coordinate of Y-axis hitting the banner;

    private Rect ibeginRect = new Rect(0,0,0,0);   // rectangle area for hint to start
    private Rect igameoverRect = new Rect(0,0,0,0);   // rectangle area for message for game over
    private Rect iwinRect = new Rect(0,0,0,0);   // rectangle area for message for winning
    private Rect startRect  = new Rect(0,0,0,0);   // rectangle area for start game
    private Rect quitRect   = new Rect(0,0,0,0);   // rectangle area for quit game
    private Rect replayRect = new Rect(0,0,0,0);   // rectangle area for replay game
    
    private Bitmap iback;// background picture
    private Bitmap ibanner;// banner picture
    private Bitmap ibegin;//  begin picture
    private Bitmap igameover;// game over picrture
    private Bitmap iwin;// winning picture
    private Bitmap ireplay;  // replay picture
    private Bitmap istart;   // start picture
    private Bitmap iquit;    // quit picture
    private Bitmap[] iscore = new Bitmap[10];// score pictures (pictures for numbers)

    private float bannerWidthRatio  = 1.0f/5.0f;
    private float bannerHeightRatio = 1.0f/15.0f;
    private SurfaceHolder surfaceHolder = null;
    private int synchronizeTime = 70;

    private int highest = 999;  // maximum value of the number that banner is to be hit to make user win
    // -1-> failed and game over, 0->waiting to start, 1->first stage (playing), 2->second stage (playing)
    // 3->final stage (playing), 4-finished the game
    private int[] stageScore = {0,5,10,15};    // 50 hits for each stage
    private int status = startStatus;
    private int score=0;     //  score that user got

    private int screenWidth  = 0;
    private int screenHeight = 0;

    private BouncyBall bouncyBall = null;
    private Banner banner = null;

    // private TimeThread timeThread = null;		    //TimeThread
    private BallGoThread ballGoThread = null;			//BallGoThread
    private GameViewDrawThread gameViewDrawThread = null;
    private Vector<ObstacleThread> obstacleThreads = null;

	public GameView(MainActivity activity) {
		super(activity);
        // Display d = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

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

        // timeThread   = new TimeThread(this);
        ballGoThread = new BallGoThread(this);
        gameViewDrawThread = new GameViewDrawThread(this);
        obstacleThreads = new Vector<ObstacleThread>();

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
        replayWidth = (int)((float)screenWidth   * buttonWidthRatio);
        replayHeight = (int)((float)screenHeight * buttonHeightRatio);
        startWidth = (int)((float)screenWidth   * buttonWidthRatio);
        startHeight = (int)((float)screenHeight * buttonHeightRatio);
        quitWidth = (int)((float)screenWidth   * buttonWidthRatio);
        quitHeight = (int)((float)screenHeight * buttonHeightRatio);
        scoreWidth = (int)((float)screenWidth   * scoreWidthRatio);
        scoreHeight = (int)((float)screenHeight * scoreHeightRatio);

        ireplay = getBitmapFromResourceWithText(R.drawable.replay, activity.replayStr,Color.BLUE);
        istart = getBitmapFromResourceWithText(R.drawable.start, activity.startStr,Color.BLUE);
        iquit = getBitmapFromResourceWithText(R.drawable.quit, activity.quitStr,Color.RED);

        ibegin = getBitmapFromResourceWithText(R.drawable.begin, activity.beginStr,Color.BLUE);
        igameover = getBitmapFromResourceWithText(R.drawable.gameover, activity.gameoverStr,Color.BLUE);
        iwin = getBitmapFromResourceWithText(R.drawable.win, activity.winStr,Color.BLUE);

        int biasX = 10;
        int biasY = 10;

        Point sPoint = new Point(biasX,screenHeight - replayHeight - biasY);
        replayRect.set(sPoint.x, sPoint.y, sPoint.x + replayWidth, sPoint.y + replayHeight);
        sPoint.set((screenWidth-startWidth)/2,screenHeight - startHeight - biasY);
        startRect.set(sPoint.x, sPoint.y, sPoint.x + startWidth, sPoint.y + startHeight);
        sPoint.set(screenWidth - quitWidth - biasX, screenHeight - quitHeight - biasY);
        quitRect.set(sPoint.x, sPoint.y, sPoint.x + quitWidth, sPoint.y + quitHeight);

        bottomY = screenHeight - bannerHeight - quitHeight - screenHeight/20;

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

        // draw replay button
        canvas.drawBitmap(ireplay ,null ,replayRect ,null);
        //
    	// draw start button
        canvas.drawBitmap(istart, null, startRect,null);
        //
        // draw quit button
        canvas.drawBitmap(iquit, null, quitRect,null);
        //

    	// draw the hint of beginning
    	if(status == startStatus){
            // sPoint.set((screenWidth-beginWidth)/2,(bottomY-beginHeight)/2);
            // rect2.set(sPoint.x,sPoint.y,sPoint.x+beginWidth,sPoint.y+beginHeight);
    		canvas.drawBitmap(ibegin, null, ibeginRect, null);
    	} else {
            // verifying status and score
            score = ballGoThread.getScore();
            if (ballGoThread.getHitBanner()) {
                if (score < highest) {
                    if (status < finalStageStatus) {
                        if (score >= stageScore[status]) {
                            status++;
                            if (status > finalStageStatus) {
                                // max stage is 3 (stage no is greater 3
                                status = finalStageStatus;
                            }
                        }
                        int obsSize = obstacleThreads.size();
                        if (status == secondStageStatus) {
                            // one obstacle for second stage
                            int numOfObstacles = 1;
                            if (obsSize < numOfObstacles) {
                                for (int i = obsSize; i < numOfObstacles; i++) {
                                    ObstacleThread obstacleThread = new ObstacleThread(this);
                                    obstacleThreads.addElement(obstacleThread);
                                    obstacleThread.start();
                                }
                                ballGoThread.setSleepSpan(ballGoThread.getSleepSpan() - 10);
                            }
                        } else if (status == finalStageStatus) {
                            // two obstacles for third stage (now is final stage)
                            int numOfObstacles = 2;
                            if (obsSize < numOfObstacles) {
                                for (int i = obsSize; i < numOfObstacles; i++) {
                                    ObstacleThread obstacleThread = new ObstacleThread(this);
                                    obstacleThreads.addElement(obstacleThread);
                                    obstacleThread.start();
                                }
                                ballGoThread.setSleepSpan(ballGoThread.getSleepSpan() - 10);
                            }
                        }
                    }
                } else {
                    // reached highest score then finished
                    status = finishedStatus;   // reach the hiest score and stop the game
                }
            } else {
                // did not hit the banner
                status = failedStatus;
            }

            if (status == failedStatus){
                // draw the hint of fail
                canvas.drawBitmap(igameover, null, igameoverRect, null);
                // timeThread.setFlag(false);  // stop TimeThread, added on 2017-11-07
                ballGoThread.setFlag(false);  // stop running the BallGoThread, added on 2017-11-07
                gameViewDrawThread.setFlag(false);  // added on 2017-11-07
                for (ObstacleThread obstacleThread:obstacleThreads) {
                    obstacleThread.setFlag(false);
                }
                obstacleThreads.clear();
            } else if (status == finishedStatus) {
                // draw the picture of winning
                canvas.drawBitmap(iwin, null, iwinRect, null);
                // timeThread.setFlag(false);  // stop TimeThread, added on 2017-11-07
                ballGoThread.setFlag(false);  // stop running the BallGoThread, added on 2017-11-07
                gameViewDrawThread.setFlag(false);  // added on 2017-11-07
                for (ObstacleThread obstacleThread:obstacleThreads) {
                    obstacleThread.setFlag(false);
                }
                obstacleThreads.clear();
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

        int x=0,y=0;

        /*
        int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_MOVE:
                // Log.d(MainActivity.Constants.LOG,"MotionEvent.ACTION_MOVE");
                break;
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_UP:
                break;
            default:
                // get the coordinates of point touched
                break;
        }
        */

        x = (int) event.getX();
        y = (int) event.getY();

		if(quitRect.contains(x,y)) {
		    // quit button was pressed
            // quit the game

            if (ballGoThread != null) {
                ballGoThread.setKeepRunning(false);
            }

            boolean retry = true;
            if (gameViewDrawThread != null) {
                gameViewDrawThread.setFlag(false);
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
                    obstacleThread.setFlag(false);
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

            if (ballGoThread != null) {
                ballGoThread.setFlag(false);
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

            /*
            if (timeThread != null) {
                retry = true;
                // this.timeThread.flag = false; // removed on 2017-11-07
                timeThread.setFlag(false);
                while (retry) {
                    try {
                        timeThread.join();
                        System.out.println("timeThread.Join().......\n");
                        retry = false;
                    } catch (InterruptedException e) {
                    }// continue processing until the thread ends
                }
            }
            */

            // System.exit(0);
            activity.finish();
		}

		if(status == startStatus){
            // waiting status, press start button to continue
            // set value to status
            if(startRect.contains(x,y)) {
                // start button was pressed
                // start playing
                status = firstStageStatus;

                // timeThread.start();
                ballGoThread.start();
                // start running the threads
                gameViewDrawThread.start();
            }
		} else if ( (status >= firstStageStatus) && (status < finishedStatus) ) {
            // if under game, move the banner
            // move the banner
            banner.setBannerX(x);
        } else if(status==failedStatus||status==finishedStatus) {
            // if fail or win
			if (replayRect.contains(x,y)) {
			    // press the replay button
                // replay the game
			    replay();
			}    		
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
    public int getStatus() {
	    return this.status;
    }
    public int getScore() {
	    return this.score;
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

	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

	public void surfaceCreated(SurfaceHolder holder) {
        // Draw the first screen when surface view has been created
        drawBeginGameScreen();
    }

	public void surfaceDestroyed(SurfaceHolder holder) {
	    // destroy and release the process
        System.out.println("SurfaceView being destroyed");
	}

    private void replay(){
        if(status==failedStatus||status==finishedStatus){
            // initialize the coordinates of the ball and the banner
            initBallAndBanner();
            score = 0;
            status = startStatus;

            // timeThread   = new TimeThread(this);
            ballGoThread = new BallGoThread(this);
            gameViewDrawThread = new GameViewDrawThread(this);

            drawBeginGameScreen();
        }
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

    private void drawBeginGameScreen() {

        // Draw the first screen of the game view
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
