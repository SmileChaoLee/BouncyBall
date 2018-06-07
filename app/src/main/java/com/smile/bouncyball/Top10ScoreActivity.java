package com.smile.bouncyball;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;


public class Top10ScoreActivity extends AppCompatActivity {

    private static final String TAG = "Top10ScoreActivity";
    private ArrayList<String> top10Players = new ArrayList<String>();
    private ArrayList<Integer> top10Scores = new ArrayList<Integer>();
    private ArrayList<Integer> medalImageIds = new ArrayList<Integer>();
    private ListView listView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            // hide action bar
            ActionBar actionBar = getSupportActionBar();
            actionBar.hide();
        } catch (Exception ex) {
            Log.d(TAG, "Unable to start this Activity.");
            ex.printStackTrace();
            finish();
        }

        setContentView(R.layout.activity_top_10_score);

        Button okButton = (Button)findViewById(R.id.top10OkButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            top10Players = extras.getStringArrayList("Top10Players");
            top10Scores = extras.getIntegerArrayList("Top10Scores");
        }

        medalImageIds.add(R.drawable.gold_medal);
        medalImageIds.add(R.drawable.silver_medal);
        medalImageIds.add(R.drawable.bronze_medal);
        medalImageIds.add(R.drawable.copper_medal);
        medalImageIds.add(R.drawable.olympics_image);
        medalImageIds.add(R.drawable.olympics_image);
        medalImageIds.add(R.drawable.olympics_image);
        medalImageIds.add(R.drawable.olympics_image);
        medalImageIds.add(R.drawable.olympics_image);
        medalImageIds.add(R.drawable.olympics_image);

        listView = findViewById(R.id.top10ListView);
        listView.setAdapter(new myListAdapter(this, R.layout.top_10_score_list_item, top10Players, top10Scores, medalImageIds));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });
    }

    private class myListAdapter extends ArrayAdapter {

        private int layoutId;
        private ArrayList<String> players;
        private ArrayList<Integer> scores;
        private ArrayList<Integer> medals;

        public myListAdapter(Context context, int layoutId, ArrayList<String> players, ArrayList<Integer> scores, ArrayList<Integer> medals) {
            super(context, layoutId, players);

            this.layoutId = layoutId;

            if (players == null) {
                this.players = new ArrayList<String>();
            } else {
                this.players = players;
            }

            if (scores == null) {
                this.scores = new ArrayList<Integer>();
            } else {
                this.scores = scores;
            }

            if (medals == null) {
                this.medals = new ArrayList<Integer>();
            } else {
                this.medals = medals;
            }
        }

        @Nullable
        @Override
        public Object getItem(int position) {
            return super.getItem(position);
        }

        @Override
        public int getPosition(@Nullable Object item) {
            return super.getPosition(item);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            View view = getLayoutInflater().inflate(layoutId, parent,false);

            if (getCount() == 0) {
                return view;
            }

            int listViewHeight = parent.getHeight();
            int itemHeight = listViewHeight / 4;    // 4 items for one screen
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.height = itemHeight;
            // view.setLayoutParams(layoutParams);  // no needed

            TextView pTextView = view.findViewById(R.id.playerTextView);
            TextView sTextView = view.findViewById(R.id.scoreTextView);
            ImageView medalImage = view.findViewById(R.id.medalImage);

            pTextView.setText(players.get(position));
            sTextView.setText(String.valueOf(scores.get(position)));
            medalImage.setImageResource(medals.get(position));


            return view;
        }
    }
}
