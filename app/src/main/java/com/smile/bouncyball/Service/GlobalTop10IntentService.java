package com.smile.bouncyball.Service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.smile.smilelibraries.player_record_rest.PlayerRecordRest;

import java.util.ArrayList;

public class GlobalTop10IntentService extends IntentService {

    public final static String Action_Name = "GlobalTop10IntentService";

    public GlobalTop10IntentService() {
        super(Action_Name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        System.out.println("GlobalTop10IntentService --> onHandleIntent() is called.");

        ArrayList<String> playerNames = new ArrayList<>();
        ArrayList<Integer> playerScores = new ArrayList<>();

        String webUrl = intent.getStringExtra("WebUrl");

        String status = PlayerRecordRest.GetGlobalTop10Scores(webUrl, playerNames, playerScores);

        Intent notificationIntent = new Intent(Action_Name);
        Bundle notificationExtras = new Bundle();
        notificationExtras.putStringArrayList("PlayerNames", playerNames);
        notificationExtras.putIntegerArrayList("PlayerScores", playerScores);
        notificationIntent.putExtras(notificationExtras);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        localBroadcastManager.sendBroadcast(notificationIntent);
        System.out.println("GlobalTop10IntentService sent result");
    }
}
