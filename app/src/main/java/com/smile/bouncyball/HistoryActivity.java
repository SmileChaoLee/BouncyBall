package com.smile.bouncyball;

import android.app.ListActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class HistoryActivity extends ListActivity {

    private static final String TAG = "HistoryActivity";
    private String[] queryResult = new String[] {"","","","","","","","","",""};
    private int total = 0;
    private int multiply = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // View historyView = getLayoutInflater().inflate(R.layout.activity_history,null);
        // final ListView listView = findViewById(android.R.id.list);

        Button okButton = (Button)findViewById(R.id.historyOkButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        String[] itemNo = new String[] {"1 ","2 ","3 ","4 ","5 ","6 ","7 ","8 ","9 ","10"};

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            queryResult = extras.getStringArray("resultStr");
        }

        for (int i=0 ; i<queryResult.length ; i++) {
            queryResult[i] = itemNo[i] + " " + queryResult[i];
        }

        setListAdapter(new mListAdapter(queryResult));

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

    private class mListAdapter extends BaseAdapter {

        private String text1[] ;  // or private String[] text1,text2;

        public mListAdapter() {
            this.text1 = new String[] {"No initialization"};
        }

        public mListAdapter(String[] text1) {
            this.text1 = text1;
        }

        @Override
        public int getCount() {
            return this.text1.length;
        }

        @Override
        public Object getItem(int position) {
            return text1[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup container) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.history_list_items, container, false);
            }

            // int listViewHeight = getListView().getHeight();
            int listViewHeight = container.getHeight();
            int itemHeight = listViewHeight / (getCount()+1);

            TextView vText1;
            vText1 = (TextView) convertView.findViewById(R.id.text1);
            vText1.setText(this.text1[position]);
            vText1.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            // vText1.setHeight(itemHeight);
            // or
            // ViewGroup.LayoutParams params = convertView.getLayoutParams();
            // params.height = itemHeight; // set height for height

            // Because the list item contains multiple touch targets, you should not override
            // onListItemClick. Instead, set a click listener for each target individually.

            return convertView;
        }
    }
}
