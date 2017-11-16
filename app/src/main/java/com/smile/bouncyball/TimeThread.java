package com.smile.bouncyball;

public class TimeThread extends Thread{
	private int highest = 90;  // not fail after 90 seconds (1.5 minutes)
    private MainActivity activity = null;
	private GameView gameView = null;
	private boolean flag = true;

	private int status = 0;
	private int score = 0;

	public TimeThread(GameView gameView) {
		this.gameView = gameView;
        this.activity = gameView.getActivity();
        this.status = gameView.getStatus();
        this.score = gameView.getScore();
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

            score++;
			if(score >= highest) {
                // reach the highest, win the game
				status = 3;
				// gameView.timeThread.flag = false;  // stop running the TimeThread. Removed on 2017-11-07
				// flag = false;	// removed on 2017-11-07
				// gameView.ballGoThread.setFlag(false);  // stop running the BallGoThread, removed on 2017-11-07
			}
			try {
				Thread.sleep(1000);  // one second for one score
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}
}