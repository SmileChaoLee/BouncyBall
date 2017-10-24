package com.smile.bouncyball;

public class TimeThread extends Thread{
	private int highest = 90;  // not fail after 90 seconds (1.5 minutes)
    private MainActivity activity=null;
	private GameView gameView=null;
	boolean flag=true;

	public TimeThread(GameView gameView) {
		this.gameView = gameView;
        this.activity = gameView.getActivity();
	}

	public void run() {
		while(flag) {

            synchronized (activity.gameHandler) {
                while (activity.gamePause) {
                    try {
                        activity.gameHandler.wait();
                    } catch (InterruptedException e) {}
                }
            }

            gameView.score++;
			if(gameView.score==highest) {
                // reach the highest, win the game
				gameView.status = 3;
				gameView.timeThread.flag   = false;  // stop running the TimeThread
				gameView.ballGoThread.setFlag(false);  // stop running the BallGoThread
			}
			try {
				Thread.sleep(1000);  // one second for one score
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
}