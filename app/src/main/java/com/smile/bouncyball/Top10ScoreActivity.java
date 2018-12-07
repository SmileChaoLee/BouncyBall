package com.smile.bouncyball;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;


public class Top10ScoreActivity extends AppCompatActivity {

    private static final String TAG = "Top10ScoreActivity";
    private String top10TitleName = "";
    private ArrayList<String> top10Players = new ArrayList<String>();
    private ArrayList<Integer> top10Scores = new ArrayList<Integer>();
    private ArrayList<Integer> medalImageIds = new ArrayList<Integer>();
    private float textFontSize;
    private ListView listView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        textFontSize = 30;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            top10TitleName = extras.getString("Top10TitleName");
            top10Players = extras.getStringArrayList("Top10Players");
            top10Scores = extras.getIntegerArrayList("Top10Scores");
            textFontSize = extras.getFloat("TextFontSize");
        }

        if (textFontSize == 50) {
            // not a cell phone, it is a tablet
            setTheme(R.style.AppThemeTextSize50);
        } else {
            setTheme(R.style.AppThemeTextSize30);
        }

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_top10_score);

        TextView titleTextView = findViewById(R.id.top10TitleTextView);
        titleTextView.setTextSize(textFontSize);
        titleTextView.setText(top10TitleName);

        Button okButton = (Button)findViewById(R.id.top10OkButton);
        // okButton.setTextSize(textFontSize);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnToPrevious();
            }
        });

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
        listView.setAdapter(new myListAdapter(this, R.layout.top10_score_list_item, top10Players, top10Scores, medalImageIds));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        returnToPrevious();
    }

    private void returnToPrevious() {
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_OK, returnIntent);    // can bundle some data to previous activity
        // setResult(Activity.RESULT_OK);   // no bundle data
        finish();
    }

    private class myListAdapter extends ArrayAdapter {  // changed name to MyListAdapter from myListAdapter

        private int layoutId;
        private ArrayList<String> players;
        private ArrayList<Integer> scores;
        private ArrayList<Integer> medals;

        @SuppressWarnings("unchecked")
        myListAdapter(Context context, int layoutId, ArrayList<String> players, ArrayList<Integer> scores, ArrayList<Integer> medals) {
            super(context, layoutId, players);

            this.layoutId = layoutId;

            if (players == null) {
                this.players = new ArrayList<>();
            } else {
                this.players = players;
            }

            if (scores == null) {
                this.scores = new ArrayList<>();
            } else {
                this.scores = scores;
            }

            if (medals == null) {
                this.medals = new ArrayList<>();
            } else {
                this.medals = medals;
            }
        }

        @Nullable
        @Override
        public Object getItem(int position) {
            return super.getItem(position);
        }

        @SuppressWarnings("unchecked")
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
            int itemNum = 4;
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                itemNum = 3;
            }
            int itemHeight = listViewHeight / itemNum;    // items for one screen
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.height = itemHeight;
            // view.setLayoutParams(layoutParams);  // no needed

            TextView pTextView = view.findViewById(R.id.playerTextView);
            pTextView.setTextSize(textFontSize);
            TextView sTextView = view.findViewById(R.id.scoreTextView);
            sTextView.setTextSize(textFontSize);
            ImageView medalImage = view.findViewById(R.id.medalImage);

            pTextView.setText(players.get(position));
            sTextView.setText(String.valueOf(scores.get(position)));
            medalImage.setImageResource(medals.get(position));

            return view;
        }
    }
}
