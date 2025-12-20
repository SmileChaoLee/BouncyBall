package com.smile.bouncyball

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.smile.bouncyball.tools.LogUtil
import com.smile.smilelibraries.player_record_rest.httpUrl.PlayerRecordRest
import com.smile.smilelibraries.scoresqlite.ScoreSQLite
import com.smile.smilelibraries.utilities.ScreenUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Top10ScoreActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "Top10ScoreActivity"
    }

    private var top10Players = ArrayList<String>()
    private var top10Scores = ArrayList<Int>()
    private var textFontSize = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.d(TAG, "onCreate")
        val actionBar = supportActionBar
        actionBar?.hide()
        textFontSize = ScreenUtil.getPxTextFontSizeNeeded(this)

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_top10_score)

        val titleTextView = findViewById<TextView>(R.id.top10TitleTextView)
        ScreenUtil.resizeTextSize(titleTextView, textFontSize)
        titleTextView.text = getString(R.string.local_top_10_score_title)

        val okButton = findViewById<Button>(R.id.top10OkButton)
        ScreenUtil.resizeTextSize(okButton, textFontSize)
        okButton.setOnClickListener { returnToPrevious() }

        val listView = findViewById<ListView>(R.id.top10ListView)

        onBackPressedDispatcher.addCallback(
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    LogUtil.d(TAG, "handleOnBackPressed")
                    returnToPrevious()
                }
            })

        lifecycleScope.launch(Dispatchers.IO) {
            val scoreSQLiteDB = ScoreSQLite(this@Top10ScoreActivity,
                BouncyBallApp.DATABASE_NAME)
            val players = PlayerRecordRest.GetLocalTop10(scoreSQLiteDB)
            val size = players.size
            LogUtil.d(TAG, "onCreate.lifecycleScope.size = $size")
            top10Players.clear()
            top10Scores.clear()
            for (p in players) {
                p.score?.let {
                    val name = if (p.playerName.isNullOrEmpty()) "No Name" else p.playerName!!
                    top10Players.add(name)
                    top10Scores.add(it)
                }
            }
            withContext(Dispatchers.Main) {
                listView.setAdapter(
                    MyListAdapter(
                        this@Top10ScoreActivity,
                        R.layout.top10_score_list_item,
                        top10Players,
                        top10Scores
                    )
                )
            }
        }
    }

    private fun returnToPrevious() {
        val returnIntent = Intent()
        setResult(RESULT_OK, returnIntent) // can bundle some data to previous activity
        // setResult(Activity.RESULT_OK);   // no bundle data
        finish()
    }

    private inner class MyListAdapter(
        context: Context, // changed name to MyListAdapter from myListAdapter
        private val layoutId: Int,
        private val players: ArrayList<String>,
        private val scores: ArrayList<Int>
    ) : ArrayAdapter<Any>(context,layoutId, players) {

        override fun getItem(position: Int): Any? {
            return super.getItem(position)
        }

        override fun getPosition(item: Any?): Int {
            return super.getPosition(item)
        }

        @SuppressLint("ViewHolder")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = layoutInflater.inflate(layoutId, parent, false)

            if (count == 0) {
                return view
            }

            val listViewHeight = parent.height
            var itemNum = 10
            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                itemNum = 6
            }
            val itemHeight = listViewHeight / itemNum // items for one screen
            val layoutParams = view.layoutParams
            layoutParams.height = itemHeight

            // view.setLayoutParams(layoutParams);  // no needed
            val pTextView = view.findViewById<TextView>(R.id.playerTextView)
            ScreenUtil.resizeTextSize(pTextView, textFontSize)
            val sTextView = view.findViewById<TextView>(R.id.scoreTextView)
            ScreenUtil.resizeTextSize(sTextView, textFontSize)

            pTextView.text = players[position]
            sTextView.text = scores[position].toString()

            return view
        }
    }
}
