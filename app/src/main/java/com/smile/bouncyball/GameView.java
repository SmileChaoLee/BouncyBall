package com.smile.bouncyball;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.support.v7.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smile.bouncyball.Service.GlobalTop10IntentService;
import com.smile.bouncyball.Service.LocalTop10IntentService;
import com.smile.bouncyball.models.Banner;
import com.smile.bouncyball.models.BouncyBall;
import com.smile.smilepublicclasseslibrary.player_record_rest.PlayerRecordRest;
import com.smile.smilepublicclasseslibrary.showing_instertitial_ads_utility.ShowingInterstitialAdsUtil;

import org.json.JSONObject;

import java.util.Vector;

public class GameView extends SurfaceView implements SurfaceHolder.Callback{

    private final String TAG = "BouncyBall.GameVew";
    // public properties
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

    // private properties
    private static final float ballSizeRatio = 1.0f/18.f;
    private static final float hintWidthRatio   = 1.0f/1.5f;
    private static final float hintHeightRatio = 1.0f/8.0f;
    private static final float buttonWidthRatio = 1.0f/4.0f;
    private static final float buttonHeightRatio = 1.0f/12.0f;
    private static final float scoreWidthRatio  = 1.0f/12.0f;
    private static final float scoreHeightRatio = 1.0f/20.0f;
    private static final float bannerWidthRatio  = 1.0f/4.0f;
    private static final float bannerHeightRatio = 1.0f/10.0f;

    private static final float bannerAdsHeightRatio = 0.05f;
    private int bannerAdsWidth = 0;
    private int bannerAdsHeight = 0;

    private MainActivity mainActivity = null;		//Activity
    private int beginWidth  = 100;           // width of the hint
    private int beginHeight = 20;         // height of the hint
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
    private Rect startRect  = new Rect(0,0,0,0);   // rectangle area for start game
    private Rect rightArrowRect   = new Rect(0,0,0,0);   // rectangle area for right arrow
    private Rect leftArrowRect = new Rect(0,0,0,0);   // rectangle area for left arrow
    
    private Bitmap iback;// background picture
    private Bitmap ibanner;// banner picture
    private Bitmap ibegin;//  begin picture
    private Bitmap ileftarrow;  // left arrow picture
    private Bitmap istart;   // start picture
    private Bitmap ipause;   // pause picture
    private Bitmap iresume;  // resume picture
    private Bitmap irightarrow;    // right arrow picture
    private Bitmap[] iscore = new Bitmap[10];// score pictures (pictures for numbers)

    private TextView stageName;
    private ImageView scoreImage0;
    private ImageView scoreImage1;
    private ImageView scoreImage2;

    // string resources
    private String[] stageLevels ;
    private String startStr = "";
    private String pauseStr = "";
    private String resumeStr = "";
    private String beginStr = "";
    private String gameOverStr = "";
    private String winStr = "";
    private String nameStr = new String("");
    private String cancelStr = new String("");
    private String submitStr = new String("");
    private String noStr = new String("");
    private String yesStr = new String("");

    private SurfaceHolder surfaceHolder = null;
    private int synchronizeTime = 70;

    private int status = startStatus;
    private int score=0;     //  score that user got

    private int gameViewWidth  = 0;
    private int gameViewHeight = 0;

    private BouncyBall bouncyBall = null;
    private Banner banner = null;

    private BallGoThread ballGoThread = null;			//BallGoThread
    private GameViewDrawThread gameViewDrawThread = null;
    private Vector<ObstacleThread> obstacleThreads = null;

    private boolean dialogFinished = false;

	public GameView(MainActivity mainActivity) {
		super(mainActivity);

        this.mainActivity = mainActivity;

        ActionBar actionBar = mainActivity.getSupportActionBar();
        // actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);    // enable customized action bar
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.LTGRAY));
        actionBar.setCustomView(R.layout.action_bar_layout);
        View actionBarView = actionBar.getCustomView();

        stageName = (TextView)actionBarView.findViewById(R.id.stageName);
        scoreImage0 = (ImageView)actionBarView.findViewById(R.id.scoreView0);
        scoreImage1 = (ImageView)actionBarView.findViewById(R.id.scoreView1);
        scoreImage2 = (ImageView)actionBarView.findViewById(R.id.scoreView2);

        stageLevels = getResources().getStringArray(R.array.stageLevels);
        stageName.setText(stageLevels[0]);   // start from stage 1

        startStr = getResources().getString(R.string.start_string);
        pauseStr = getResources().getString(R.string.pause_string);
        resumeStr = getResources().getString(R.string.resume_string);
        beginStr = getResources().getString(R.string.begin_string);
        gameOverStr = getResources().getString(R.string.gameOver_string);
        winStr = getResources().getString(R.string.win_string);
        nameStr = getResources().getString(R.string.nameStr);
        submitStr = getResources().getString(R.string.submitStr);
        cancelStr = getResources().getString(R.string.cancelStr);
        noStr = getResources().getString(R.string.noStr);
        yesStr = getResources().getString(R.string.yesStr);

        gameViewHandler = new Handler(Looper.getMainLooper());  // for synchronizing
        gameViewPause = false;   // for synchronizing

        surfaceHolder = getHolder();
		surfaceHolder.addCallback(this); // register the interface
        setZOrderOnTop(true);
        surfaceHolder.setFormat(PixelFormat.TRANSLUCENT);

        int highestScore = BouncyBallApp.ScoreSQLiteDB.readHighestScore();

        status = startStatus;    // waiting to start

        // setWillNotDraw(false);  // added on 2017-11-07 to activate onDraw() of SurfaceView
        // if set it to false, then the drawing might have to go through by onDraw() all the time
        setWillNotDraw(true);   // added on 2017-11-07 for just in case, the default is false

        // the followings were moved to surfaceCreated()
        /*
        initBitmapAndModels();
        // obstacleThreads must be created before ballGoThread
        obstacleThreads = new Vector<ObstacleThread>();
        // ballGoThread must be created before other threads except obstacleThreads
        ballGoThread = new BallGoThread(this);
        gameViewDrawThread = new GameViewDrawThread(this);
        buttonHoldThread = new ButtonHoldThread(this);
        buttonHoldThread.start();
        */

        System.out.println("GameView-->Constructor\n");
	}

	public SurfaceHolder getSurfaceHolder() {
	    return this.surfaceHolder;
    }

	public void initBitmapAndModels(){

	    Log.d(TAG, "initBitmapAndModels() is called");

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
        if (iback == null) {
            System.out.println("iback is null.");
        }
		iball = BitmapFactory.decodeResource(getResources(), R.drawable.ball);
		ibanner = BitmapFactory.decodeResource(getResources(), R.drawable.banner);
		ibegin = BitmapFactory.decodeResource(getResources(), R.drawable.begin);

        bannerWidth = (int)((float)gameViewWidth   * bannerWidthRatio);         // width of the banner
        bannerHeight = (int)((float)gameViewHeight * bannerHeightRatio);      // height of the banner
        ballSize   = (int)((float)gameViewHeight * ballSizeRatio);            // size of the ball
        ballRadius = ballSize/2;
        beginWidth  = (int)((float)gameViewWidth  * hintWidthRatio);          // width of the hint
        beginHeight = (int)((float)gameViewHeight * hintHeightRatio);        // height of the hint
        leftArrowWidth = (int)((float)gameViewWidth   * buttonWidthRatio);
        leftArrowHeight = (int)((float)gameViewHeight * buttonHeightRatio * 1.5); // 1.5 * normal button
        startWidth = (int)((float)gameViewWidth   * buttonWidthRatio);
        startHeight = (int)((float)gameViewHeight * buttonHeightRatio);
        rightArrowWidth = (int)((float)gameViewWidth   * buttonWidthRatio);
        rightArrowHeight = (int)((float)gameViewHeight * buttonHeightRatio * 1.5); // 1.5 * normal button
        scoreWidth = (int)((float)gameViewWidth   * scoreWidthRatio);
        scoreHeight = (int)((float)gameViewHeight * scoreHeightRatio);

        ileftarrow = getBitmapFromResourceWithText(R.drawable.leftarrow, "",Color.BLUE);    // no string
        istart = getBitmapFromResourceWithText(R.drawable.start, startStr,Color.BLUE);
        ipause = getBitmapFromResourceWithText(R.drawable.pause, pauseStr,Color.YELLOW);
        iresume = getBitmapFromResourceWithText(R.drawable.resume, resumeStr,Color.BLUE);
        irightarrow = getBitmapFromResourceWithText(R.drawable.rightarrow, "",Color.RED);   // no string

        ibegin = getBitmapFromResourceWithText(R.drawable.begin, beginStr,Color.BLUE);

        int biasX = 10;
        int biasY = 10;

        Point sPoint = new Point(biasX,gameViewHeight - leftArrowHeight - biasY);
        leftArrowRect.set(sPoint.x, sPoint.y, sPoint.x + leftArrowWidth, sPoint.y + leftArrowHeight);
        sPoint.set((gameViewWidth-startWidth)/2,gameViewHeight - startHeight - biasY);
        startRect.set(sPoint.x, sPoint.y, sPoint.x + startWidth, sPoint.y + startHeight);
        sPoint.set(gameViewWidth - rightArrowWidth - biasX, gameViewHeight - rightArrowHeight - biasY);
        rightArrowRect.set(sPoint.x, sPoint.y, sPoint.x + rightArrowWidth, sPoint.y + rightArrowHeight);

        bottomY = gameViewHeight - bannerHeight - startHeight - gameViewHeight/20;

        int numB = (int)(bottomY/ballSize);  // removed on 2018-07-08
        bottomY = numB * ballSize;

        sPoint.set((gameViewWidth - beginWidth)/2,(bottomY - beginHeight)/2);
        ibeginRect.set(sPoint.x,sPoint.y,sPoint.x + beginWidth,sPoint.y + beginHeight);

        ballSpan = ballRadius ;
		// initialize the coordinates of the ball and the banner
        ballX = gameViewWidth / 2;
        ballY = bottomY - ballRadius;
        bouncyBall = new BouncyBall(ballX, ballY, ballSize, ballSpan, iball);

        bannerX = gameViewWidth / 2;
        bannerY = bottomY + (bannerHeight/2);
        banner = new Banner(bannerX, bannerY, bannerWidth, bannerHeight, ibanner);
	}

	public void initBallAndBanner(){
		// initialize the coordinates of the ball
        // (ballX,ballY) is the center of the circle

        bouncyBall.setBallX(gameViewWidth / 2);
        bouncyBall.setBallY(bottomY - bouncyBall.getBallRadius());

		// initialize the coordinates of the banner
        // (bannerX,bannerY) is the center of banner

		banner.setBannerX(gameViewWidth / 2);
		banner.setBannerY(bottomY + banner.getBannerHeight()/2);
	}

    @Override
    public void onDraw(Canvas canvas) {
	    System.out.println("onDraw() running.");
        doDraw(canvas);
    }

	public void doDraw(Canvas canvas) {

    	// clear the background

        Point sPoint = new Point(0,0);
        Rect rect2 = new Rect(0,0,0,0);
    	//
        RectF rectf = new RectF(0,0, gameViewWidth, gameViewHeight);
        canvas.drawBitmap(iback,null, rectf, null);

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

        // draw score, action bar is on the main UI thread not in the game view
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if ( (status >= startStatus) && (status <= finalStageStatus) ) {
                    stageName.setText(stageLevels[status]);
                }

                String scoreStr = score + "";
                int loop = 3 - scoreStr.length();
                for(int i=0;i<loop;i++){
                    scoreStr = "0" + scoreStr;
                }
                int tempScore = scoreStr.charAt(2)-'0';
                scoreImage0.setImageBitmap(iscore[tempScore]);
                tempScore = scoreStr.charAt(1)-'0';
                scoreImage1.setImageBitmap(iscore[tempScore]);
                tempScore = scoreStr.charAt(0)-'0';
                scoreImage2.setImageBitmap(iscore[tempScore]);
            }
        });

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
            if ( (status >= secondStageStatus) && (status <= finalStageStatus) ) {
                // stage 1 to stage 4
                // 1 obstacle for stage 2, 2 obstacles for stage 3, 3 obstacles for stage 4(final stage)
                int obsSize = obstacleThreads.size();
                int numOfObstacles = 0;
                numOfObstacles = status - 1;
                if (obsSize < numOfObstacles) {
                    for (int i = obsSize; i < numOfObstacles; i++) {
                        ObstacleThread obstacleThread = new ObstacleThread(this, i + 1);
                        obstacleThreads.addElement(obstacleThread);
                        obstacleThread.start();
                    }
                }
            } else if ( (status == failedStatus) || (status == finishedStatus) ) {
                ballGoThread.setKeepRunning(false);  // stop running the BallGoThread, added on 2017-11-07
                gameViewDrawThread.setKeepRunning(false);  // added on 2017-11-07
                for (ObstacleThread obstacleThread : obstacleThreads) {
                    obstacleThread.setKeepRunning(false);
                }
                obstacleThreads.clear();

                //  game over
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final TextView tv = new TextView(mainActivity);
                        tv.setTextSize(40);
                        tv.setTextColor(Color.BLUE);
                        tv.setTypeface(Typeface.DEFAULT);
                        if (status == failedStatus) {
                            // failed
                            tv.setText(gameOverStr);
                        } else {
                            // won
                            tv.setText(winStr);
                        }
                        tv.setGravity(Gravity.CENTER);
                        AlertDialog alertDialog = new  AlertDialog.Builder(mainActivity).create();
                        alertDialog.setTitle(null);
                        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        alertDialog.setCancelable(false);
                        alertDialog.setView(tv);
                        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, noStr, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                recordScore(score,false);   // quit game
                            }
                        });
                        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, yesStr, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                recordScore(score,true);   //   replay the game
                            }
                        });

                        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                            @Override
                            public void onShow(DialogInterface dialog) {
                                setDialogStyle(dialog);
                            }
                        });
                        alertDialog.show();
                    }
                });
                //

            } else {
    	        // first stage, do nothing
            }
        }
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
                        if ((y >= (bottomY - 20)) && (y <= (bottomY + 20 + banner.getBannerHeight()))) {
                            // Y - coordinate is inside the area and add 20 extra pixels
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
                        // int xSpeed = 6; // 6 pixels  original
                        int xSpeed = 8; //  8 pixels
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

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	    System.out.println("surfaceChanged() is called");
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // Draw the first screen when surface view has been created
        System.out.println("surfaceCreated is called --> getWidth() = " + getWidth());
        System.out.println("surfaceCreated is called --> getHeight() = " + getHeight());

        gameViewWidth = getWidth();
        gameViewHeight = getHeight();

        // the followings were moved from constructor
        initBitmapAndModels();
        // obstacleThreads must be created before ballGoThread
        obstacleThreads = new Vector<ObstacleThread>();
        // ballGoThread must be created before other threads except obstacleThreads
        ballGoThread = new BallGoThread(this);
        gameViewDrawThread = new GameViewDrawThread(this);
        buttonHoldThread = new ButtonHoldThread(this);
        buttonHoldThread.start();
        //

        drawGameScreen();
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // destroy and release the process
        System.out.println("SurfaceView being destroyed");
    }

    public MainActivity getMainActivity() {
        return this.mainActivity;
    }
    public int getSynchronizeTime() {
	    return this.synchronizeTime;
    }
    public int getGameViewWidth() {
	    return this.gameViewWidth;
    }
    public int getGameViewHeight() {
	    return this.gameViewHeight;
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
        Log.i(TAG, "Showing Ad from AdMob or Facebook");
        if (BouncyBallApp.InterstitialAd != null) {
            int entryPoint = 0; //  no used
            ShowingInterstitialAdsUtil.ShowAdAsyncTask showAdsAsyncTask =
                    BouncyBallApp.InterstitialAd.new ShowAdAsyncTask(mainActivity
                            , entryPoint
                            , new ShowingInterstitialAdsUtil.AfterDismissFunctionOfShowAd() {
                        @Override
                        public void executeAfterDismissAds(int endPoint) {
                            renewGame();
                        }
                    });
            showAdsAsyncTask.execute();
        }
    }

    public void releaseSynchronizings() {
        if (mainActivity.gamePause) {
            // in pause status
            synchronized (mainActivity.activityHandler) {
                mainActivity.gamePause = false;
                mainActivity.activityHandler.notifyAll();
            }
        }

        if (gameViewPause) {
            // GameView in pause status
            synchronized (gameViewHandler) {
                gameViewPause = false;
                gameViewHandler.notifyAll();
            }
        }
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

        if (obstacleThreads != null) {
            for (ObstacleThread obstacleThread : obstacleThreads) {
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
        }

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

    private void renewGame() {
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

    private void setDialogStyle(DialogInterface dialog) {
        AlertDialog dlg = (AlertDialog)dialog;
        
        dlg.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        dlg.getWindow().setDimAmount(0.0f); // no dim for background screen

        dlg.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT);
        dlg.getWindow().setBackgroundDrawableResource(R.drawable.dialogbackground);

        float fontSize = 20;
        Button nBtn = dlg.getButton(DialogInterface.BUTTON_NEGATIVE);
        nBtn.setTextSize(fontSize);
        nBtn.setTypeface(Typeface.DEFAULT_BOLD);
        nBtn.setTextColor(Color.RED);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams)nBtn.getLayoutParams();
        layoutParams.weight = 10;
        nBtn.setLayoutParams(layoutParams);

        Button pBtn = dlg.getButton(DialogInterface.BUTTON_POSITIVE);
        pBtn.setTextSize(fontSize);
        pBtn.setTypeface(Typeface.DEFAULT_BOLD);
        pBtn.setTextColor(Color.rgb(0x00,0x64,0x00));
        pBtn.setLayoutParams(layoutParams);
    }

    private void recordScore(final int currentScore, final boolean replayYn) {
        //    record currentScore as a score in database
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final EditText et = new EditText(mainActivity);
                et.setTextSize(24);
                // et.setHeight(200);
                et.setTextColor(Color.BLUE);
                // et.setBackground(new ColorDrawable(Color.TRANSPARENT));
                // et.setBackgroundColor(Color.TRANSPARENT);
                et.setHint(nameStr);
                et.setGravity(Gravity.CENTER);
                AlertDialog alertD = new AlertDialog.Builder(mainActivity).create();
                alertD.setTitle(null);
                alertD.requestWindowFeature(Window.FEATURE_NO_TITLE);
                alertD.setCancelable(false);
                alertD.setView(et);
                alertD.setButton(DialogInterface.BUTTON_NEGATIVE, cancelStr, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (replayYn) {
                            releaseSynchronizings();
                            renewGame();    // no showing for ad
                        } else {
                            mainActivity.quitGame();
                        }
                    }
                });
                alertD.setButton(DialogInterface.BUTTON_POSITIVE, submitStr, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        // use thread to add a record to database (remote database on AWS-EC2)
                        Thread restThread = new Thread() {
                            @Override
                            public void run() {
                                try {
                                    String webUrl = new String(BouncyBallApp.REST_Website + "/AddOneRecordREST");   // ASP.NET Cor
                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put("PlayerName", et.getText().toString());
                                    jsonObject.put("Score", score);
                                    jsonObject.put("GameId", BouncyBallApp.GameId);
                                    PlayerRecordRest.addOneRecord(webUrl, jsonObject);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    Log.d(TAG, "Failed to add one record to Playerscore table.");
                                }
                            }
                        };
                        restThread.start();

                        BouncyBallApp.ScoreSQLiteDB.addScore(et.getText().toString(), currentScore);

                        if (replayYn) {
                            releaseSynchronizings();
                            renewGame();    // no showing for ad
                        } else {
                            mainActivity.quitGame();
                        }
                    }
                });
                alertD.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        setDialogStyle(dialog);
                    }
                });
                alertD.show();
            }
        });
    }
}
