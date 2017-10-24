package com.smile.bouncyball;

import java.nio.charset.MalformedInputException;

public class BallGoThread extends Thread{
    private MainActivity activity=null;
	private GameView gView=null;
    private int ballRadius=0;
    private int sleepSpan=80;
    private boolean flag=true;

	public BallGoThread(GameView gView) {
		this.gView = gView;
        this.activity = gView.getActivity();
        this.ballRadius = gView.ballRadius;
        this.sleepSpan  = gView.synchronizeTime;
	}
    
	public void run(){
        int tempX=0;
        int tempY=0;
		while(flag) {

            synchronized (activity.gameHandler) {
                while (activity.gamePause) {
                    try {
                        activity.gameHandler.wait();
                    } catch (InterruptedException e) {}
                }
            }

            tempX = gView.ballX;
            tempY = gView.ballY;
			switch(gView.direction) {
                case 0:
                    // going to right top
                    gView.ballX = gView.ballX + gView.ballSpan;
                    gView.ballY = gView.ballY - gView.ballSpan;

                    if ((gView.ballX+ballRadius)>gView.screenWidth) {
                        if ( (tempX>(gView.screenWidth-ballRadius)) && (tempX<gView.screenWidth) ) {
                            gView.ballX = gView.screenWidth - ballRadius;
                        } else {
                            // hit the right wall
                            gView.direction = 3;
                        }
                    } else if ((gView.ballY-ballRadius)<0) {
                        if ( (tempY<ballRadius) && (tempY>0) ){
                            gView.ballY = ballRadius;
                        } else {
                            // hit the top wall
                            gView.direction = 1;
                        }
                    }
                    break;
                case 3:
                    // going to left top
                    gView.ballX = gView.ballX - gView.ballSpan;
                    gView.ballY = gView.ballY - gView.ballSpan;
                    if ((gView.ballX - ballRadius)<0) {
                        if ( (tempX<ballRadius) && (tempX>0) ) {
                            gView.ballX = ballRadius;
                        } else {
                            // hit the left wall
                            gView.direction = 0;
                        }
				    } else if ((gView.ballY-ballRadius)<0) {
                        if ( (tempY<ballRadius) && (tempY>0) ){
                            gView.ballY = ballRadius;
                        } else {
                            // hit the top wall
                            gView.direction = 2;
                        }
				    }
				    break;
				case 1:
				    // going to right bottom
				    gView.ballX = gView.ballX + gView.ballSpan;
				    gView.ballY = gView.ballY + gView.ballSpan;
				  
				    if ((gView.ballY+ballRadius)>gView.bottomY) {
                        if ( (tempY>(gView.bottomY-ballRadius)) && (tempY<gView.bottomY) ) {
                            gView.ballY = gView.bottomY - ballRadius;
                        } else {
                            // hit the bottom wall
                            checkCollision(1);
                        }
				    } else if ((gView.ballX+ballRadius)>gView.screenWidth) {
                        if ( (tempX>(gView.screenWidth-ballRadius)) && (tempX<gView.screenWidth) ) {
                            gView.ballX = gView.screenWidth - ballRadius;
                        } else {
                            //hit the right wall
                            gView.direction = 2;
                        }
				    }
				    break;
				case 2:
				    // going to left bottom
				    gView.ballX = gView.ballX - gView.ballSpan;
				    gView.ballY = gView.ballY + gView.ballSpan;
				  
				    if ((gView.ballY+ballRadius)>gView.bottomY) {
                        if ( (tempY>(gView.bottomY-ballRadius)) && (tempY<gView.bottomY) )  {
                            gView.ballY = gView.bottomY - ballRadius;
                        } else {
                            // hit the bottom wall
                            checkCollision(2);
                        }
				    } else if ((gView.ballX-ballRadius)<0) {
                        if ( (tempX<ballRadius) && (tempX>0) ) {
                            gView.ballX = ballRadius;
                        } else {
                            // hit the left wall
                            gView.direction = 1;
                        }
				    }
				    break;
			}			
			try{Thread.sleep(sleepSpan);}
			catch(Exception e){e.printStackTrace();}			
		}
	}

	public void checkCollision(int direction) {
        int bannerX1,bannerX2;
        bannerX1 = gView.bannerX - gView.bannerWidth/2;
        bannerX2 = gView.bannerX + gView.bannerWidth/2;

        // (gView.bannerX,gView.bannerY) is the center of the banner
	  	if ( ((gView.ballX+ballRadius)>=bannerX1) && ((gView.ballX-ballRadius)<=bannerX2) ) {
            // hit the banner
            switch(direction){
                case 1:
                    gView.direction = 0;
                    break;
                case 2:
                    gView.direction = 3;
                    break;
	  		}
	  	} else {
	  	    // do not hit the banner
	  	    // fail
	  		gView.timeThread.flag   = false;
	  		gView.ballGoThread.flag = false;
	  		gView.status = 2;
	  	}		
	}

    public boolean getFlag() {
        return this.flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }
}