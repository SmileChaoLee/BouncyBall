package com.smile.bouncyball;

import android.app.ListActivity;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ScoreHistoryActivity extends ListActivity {

    private static final String TAG = "ScoreHistoryActivity";
    private ArrayList<String> queryResult = null;
    private int total = 0;
    private int multiply = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN ,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_score_history);

        Button okButton = (Button)findViewById(R.id.historyOkButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            queryResult = new ArrayList<String>();
        } else {
            queryResult = extras.getStringArrayList("resultStr");
        }

        ArrayAdapter<String> myArrayAdapter = new ArrayAdapter<String>(this, R.layout.score_history_list_item, R.id.itemText, queryResult);
        setListAdapter(myArrayAdapter);

        // examples for thread synchronization
        // the following is about synchronize two threads. added on 2017-11-11
        final Handler handler = new Handler(Looper.getMainLooper());
        final Thread a = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (handler) {
                    for (int i=1; i<=10; i++) {
                        total += i;
                    }
                    System.out.println("a-> total = " + total);
                    handler.notify();
                }
            }
        });

        final Thread b = new Thread(new Runnable() {
            @Override
            public void run() {
                a.start();
                synchronized (handler) {
                    try {
                        handler.wait();
                        System.out.println("b-> total = " + total);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        b.start();
        //

        // the following is about synchronize two threads. added on 2018-05-20
        final Thread c = new Thread() {
            @Override
            public void run() {
                synchronized (this) {
                    for (int i=1; i<=10; i++) {
                        multiply *= i;
                    }
                    System.out.println("c-> multiply = " + multiply);
                    notify();
                }
            }
        };

        Thread d = new Thread() {
            @Override
            public void run() {
                c.start();
                synchronized (c) {
                    try {
                        c.wait();
                        System.out.println("d-> multiply = " + multiply);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        };

        d.start();
        //

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Toast.makeText(this, (String)getListView().getItemAtPosition(position), Toast.LENGTH_SHORT).show();
    }
}
