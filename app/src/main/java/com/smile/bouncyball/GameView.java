package com.smile.bouncyball;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.util.Random;

public class GameView extends SurfaceView implements SurfaceHolder.Callback{

    private MainActivity activity=null;		//Activity

    private int backCols=0;
    private int backRows=0;

    private int backSize=16;          // the size of background
	private int ballSize=16;          // size of the ball
    private float ballSizeRatio = 1.0f/18.f;

    private int beginWidth  = 100;           // width of the hint
    private int beginHeight = 20;         // height of the hint
    private int gameoverWidth = 100;
    private int gameoverHeight = 20;
    private int winWidth = 100;
    private int winHeight = 20;
    private float hintWidthRatio   = 1.0f/2.5f;
    private float hintHeightRatio = 1.0f/12.0f;
    private int replayWidth = 100;
    private int replayHeight = 20;
    private int startWidth = 100;
    private int startHeight = 20;
    private int quitWidth = 100;
    private int quitHeight = 20;
    private float buttonWidthRatio = 1.0f/4.0f;
    private float buttonHeightRatio = 1.0f/15.0f;
    private int scoreWidth = 32;
    private int scoreHeight = 32;
    private float scoreWidthRatio  = 1.0f/12.0f;
    private float scoreHeightRatio = 1.0f/20.0f;

    private Rect startRect  = new Rect(0,0,0,0);   // rectangle area for start game
    private Rect quitRect   = new Rect(0,0,0,0);   // rectangle area for quit game
    private Rect replayRect = new Rect(0,0,0,0);   // rectangle area for replay game
    
    private Bitmap iback;// background picture
    private Bitmap[] iscore=new Bitmap[10];// score pictures (pictures for numbers)
    private Bitmap iball;// ball picture
    private Bitmap ibanner;// banner picture
    private Bitmap ibegin;//  begin picture
    private Bitmap igameover;// game over picrture
    private Bitmap iwin;// winning picture
    private Bitmap ireplay;  // replay picture
    private Bitmap istart;   // start picture
    private Bitmap iquit;    // quit picture

    // default access controller
    private Random random = new Random(System.currentTimeMillis());

    TimeThread timeThread=null;				//TimeThread
    BallGoThread ballGoThread=null;			//BallGoThread
    GameViewDrawThread gameViewDrawThread=null;

    int synchronizeTime = 80;
    int screenWidth  = 640;    // width of the screen, 320px
    int screenHeight = 960;    // height of the screen, 480px
    int status=0;    //  0-start waiting  1-processing   2-game over  3-win the game
    int score=0;     //  score that user got
    int direction=0; //  the direction of the ball
    int ballX;       //  coordinate (x-axis) of the ball
    int ballY;       //  coordinate (y-axis) of the ball
    int bannerX;     //  the coordinate (x-axis) of the banner
    int bannerY;     //  the coordinate (y-axis) of the banner
    int bannerWidth=40;       // width of the banner
    int bannerHeight=6;       // height of the banner
    private float bannerWidthRatio  = 1.0f/5.0f;
    private float bannerHeightRatio = 1.0f/15.0f;
    int ballSpan=8;           // speed of the ball
    int ballRadius=ballSize/2;
    int bottomY=0;            // the coordinate of Y-axis hitting the banner;

    SurfaceHolder surfaceHolder=null;

	public GameView(MainActivity activity) {
		super(activity);

        surfaceHolder = getHolder();
		surfaceHolder.addCallback(this); // register the interface
		this.activity = activity;

        // screenWidth  = size.x;
        // screenHeight = size.y - 100;
        this.screenWidth  = activity.screenWidth;
        this.screenHeight = activity.screenHeight;

        initBitmap();

		// gameViewDrawThread = new GameViewDrawThread(this);
        System.out.println("GameView-->Constructor\n");
	}

	public void initBitmap(){

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

		ireplay = BitmapFactory.decodeResource(getResources(), R.drawable.replay);
        istart = BitmapFactory.decodeResource(getResources(), R.drawable.start);
        iquit = BitmapFactory.decodeResource(getResources(), R.drawable.quit);

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

        int biasX = 10;

        Point sPoint = new Point(biasX,screenHeight-replayHeight);
        replayRect.set(sPoint.x, sPoint.y, sPoint.x + replayWidth, sPoint.y + replayHeight);

        sPoint.set((screenWidth-startWidth)/2,screenHeight-startHeight);
        startRect.set(sPoint.x, sPoint.y, sPoint.x + quitWidth, sPoint.y + quitHeight);

        sPoint.set(screenWidth - quitWidth - biasX, screenHeight - quitHeight);
        quitRect.set(sPoint.x, sPoint.y, sPoint.x + quitWidth, sPoint.y + quitHeight);

        bottomY = screenHeight - bannerHeight - quitHeight - screenHeight/20;

        int numB = (int)(bottomY/ballSize);
        bottomY = numB * ballSize;

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
		initBallAndBanner();
	}

	public void initBallAndBanner(){
		// initialize the coordinates of the ball
        // (ballX,ballY) is the center of the circle
        ballX = screenWidth/2;
		ballY = bottomY - ballRadius;

		// initialize the coordinates of the banner
        // (bannerX,bannerY) is the center of banner
		bannerX = screenWidth/2;
		bannerY = bottomY + (bannerHeight/2);
	}

	public void replay(){
		if(status==2||status==3){	
	    	// initialize the coordinates of the ball and the banner
			initBallAndBanner();
			score=0;
			status=0;
		}	
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


        // rect2.set(0,0,screenWidth,screenHeight);
        canvas.drawBitmap(iback, null, rect2, null);
    	
    	// draw the score
    	String scoreStr = score+"";
    	int loop = 3 - scoreStr.length();
    	for(int i=0;i<loop;i++){
    		scoreStr = "0" + scoreStr;
    	}

        int scoreGap = 20;
    	int startX = screenWidth-(scoreWidth+scoreGap)*3-10;
        int startY = scoreHeight;
        int tempScore = 0;
        int sX = 0;

    	for(int i=0;i<3;i++){
    		tempScore=scoreStr.charAt(i)-'0';
            sX = startX+i*(scoreWidth+scoreGap);
            rect2.set(sX,startY,sX+scoreWidth,startY+scoreHeight);
    		canvas.drawBitmap(iscore[tempScore], null, rect2, null);
    	}
    	
    	// draw the ball
        int tempX = ballX - ballRadius;
        if (tempX<0) {
            tempX = 0;
            ballX = tempX + ballRadius;
        }
        int tempY = ballY - ballRadius;
        if (tempY<0) {
            tempY = 0;
            ballY = tempY + ballRadius;
        }
        sPoint.set(tempX,tempY);

        tempX = sPoint.x + ballSize;
        if (tempX>screenWidth) {
            tempX = screenWidth;
            sPoint.x = tempX - ballSize;
            ballX = tempX - ballRadius;
        }

        tempY = sPoint.y + ballSize;
        if (tempY>bottomY) {
            tempY = bottomY;
            sPoint.y = tempY - ballSize;
            ballY = tempY - ballRadius;
        }

        rect2.set(sPoint.x,sPoint.y,tempX,tempY);
    	canvas.drawBitmap(iball, null ,rect2, null);
    	
    	// draw the banner
        sPoint.set(bannerX-bannerWidth/2,bannerY-bannerHeight/2);
        rect2.set(sPoint.x,sPoint.y,sPoint.x+bannerWidth,sPoint.y+bannerHeight);
    	canvas.drawBitmap(ibanner, null ,rect2, null);
    	
    	// draw the hint of beginning
    	if(status==0){
            sPoint.set((screenWidth-beginWidth)/2,(bottomY-beginHeight)/2);
            rect2.set(sPoint.x,sPoint.y,sPoint.x+beginWidth,sPoint.y+beginHeight);
    		canvas.drawBitmap(ibegin, null, rect2, null);
    	}

     	// draw the hint of fail
    	if(status==2){
            sPoint.set((screenWidth-gameoverWidth)/2,(bottomY-gameoverHeight)/2);
            rect2.set(sPoint.x,sPoint.y,sPoint.x+gameoverWidth,sPoint.y+gameoverHeight);
    		canvas.drawBitmap(igameover, null, rect2, null);
    	}  
    	
    	// draw the picture of winning
     	if(status==3){
            sPoint.set((screenWidth-winWidth)/2,(bottomY-winHeight)/2);
            rect2.set(sPoint.x,sPoint.y,sPoint.x+winWidth,sPoint.y+winHeight);
            canvas.drawBitmap(iwin, null, rect2, null);
    	}

        // draw the replay picture
        canvas.drawBitmap(ireplay ,null ,replayRect ,null);

        // draw the button of start button
        canvas.drawBitmap(istart, null, startRect,null);

        // draw the quit picture
     	canvas.drawBitmap(iquit, null, quitRect,null);
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

            boolean retry = true;
            if (gameViewDrawThread != null) {
                gameViewDrawThread.setFlag(false);
                while (retry) {
                    try {
                        gameViewDrawThread.join();
                        System.out.println("gameViewDrawThread.Join()........\n");
                        retry = false;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }// continue processing until the thread ends
                }
            }

            if (ballGoThread != null) {
                retry = true;
                ballGoThread.setFlag(false);
                while (retry) {
                    try {
                        ballGoThread.join();
                        System.out.println("ballGoThread.Join().......\n");
                        retry = false;
                    } catch (InterruptedException e) {
                    }// continue processing until the thread ends
                }
            }

            if (timeThread != null) {
                retry = true;
                this.timeThread.flag = false;
                while (retry) {
                    try {
                        timeThread.join();
                        System.out.println("timeThread.Join().......\n");
                        retry = false;
                    } catch (InterruptedException e) {
                    }// continue processing until the thread ends
                }
            }

            // System.exit(0);
            activity.finish();
		}

		if(status == 0){
            // waiting status, press start button to continue
            // set value to status
            if(startRect.contains(x,y)) {
                // start button was pressed
                // start playing
                status = 1;
                // Random random = new Random(now.toMillis(false));
                direction = random.nextInt(2)*3;  //   0 or 1  multiple 3 ------>0 or 3

                timeThread   = new TimeThread(this);
                ballGoThread = new BallGoThread(this);
                timeThread.start();
                ballGoThread.start();
            }
		} else if(status == 1) {
            // if under game, move the banner
            // move the banner
            bannerX = x;
        } else if(status==2||status==3) {
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
	
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

	public void surfaceCreated(SurfaceHolder holder) {
        if (gameViewDrawThread==null) {
            gameViewDrawThread = new GameViewDrawThread(this);
            gameViewDrawThread.setFlag(true);
            gameViewDrawThread.start();
        } else {
            gameViewDrawThread.setFlag(true);
        }
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
	    // destroy and release the process
        System.out.println("SurfaceView being destroyed");

        /*
        boolean retry = true;
        this.gameViewDrawThread.flag = false;

        while (retry) {
            try {
            	gameViewDrawThread.join();
                System.out.println("Join().......\n");
                retry = false;
            } 
            catch (InterruptedException e) {}// continue processing until the thread ends
        }
        */
	}
}
