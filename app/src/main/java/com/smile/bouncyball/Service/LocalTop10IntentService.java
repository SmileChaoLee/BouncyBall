package com.smile.bouncyball.Service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.smile.bouncyball.BouncyBallApp;
import com.smile.smilelibraries.models.Player;
import com.smile.smilelibraries.player_record_rest.httpUrl.PlayerRecordRest;

import java.util.ArrayList;

public class LocalTop10IntentService extends IntentService {
    public final static String Action_Name = "LocalTop10IntentService";

    public LocalTop10IntentService() {
        super(Action_Name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        System.out.println("LocalTop10IntentService --> onHandleIntent() is called.");

        ArrayList<String> playerNames = new ArrayList<>();
        ArrayList<Integer> playerScores = new ArrayList<>();

        ArrayList<Player> players = PlayerRecordRest.GetLocalTop10(BouncyBallApp.ScoreSQLiteDB);
        if (players.isEmpty()) {
            players = new ArrayList<>();
        }
        for (Player p : players) {
            playerNames.add(p.getPlayerName());
            playerScores.add(p.getScore());
        }

        Intent notificationIntent = new Intent(Action_Name);
        Bundle extras = new Bundle();
        extras.putStringArrayList("PlayerNames", playerNames);
        extras.putIntegerArrayList("PlayerScores", playerScores);
        notificationIntent.putExtras(extras);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        localBroadcastManager.sendBroadcast(notificationIntent);
        System.out.println("LocalTop10IntentService sent result");
    }
}
