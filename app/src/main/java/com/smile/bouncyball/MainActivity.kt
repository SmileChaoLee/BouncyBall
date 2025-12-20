package com.smile.bouncyball

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.smile.bouncyball.tools.LogUtil
import com.smile.smilelibraries.show_interstitial_ads.ShowInterstitial
import com.smile.smilelibraries.utilities.ScreenUtil

class MainActivity : AppCompatActivity() {

    companion object  {
        private const val TAG = "MainActivity"
    }

    private lateinit var gameView: GameView
    private lateinit var topScoreLauncher: ActivityResultLauncher<Intent>

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogUtil.d(TAG, "onCreate")

        BouncyBallApp.InterstitialAd = ShowInterstitial(
            this, BouncyBallApp.facebookAds,
            BouncyBallApp.googleInterstitialAd
        )
        if (resources.configuration.orientation != Configuration.ORIENTATION_PORTRAIT) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        setContentView(R.layout.activity_main)

        gameView = GameView(this@MainActivity) // create a gameView
        val gameLayout = findViewById<LinearLayout>(R.id.layoutForGameView)
        gameLayout.addView(gameView)

        if (BouncyBallApp.googleAdMobBannerID.isNotEmpty()) {
            val bannerLinearLayout = findViewById<LinearLayout>(R.id.linearlayout_for_ads_in_myActivity)
            val bannerAdView = AdView(this@MainActivity)

            val bannerLp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            bannerLp.gravity = Gravity.CENTER
            bannerAdView.setLayoutParams(bannerLp)
            bannerAdView.setAdSize(AdSize.BANNER)
            bannerAdView.adUnitId = BouncyBallApp.googleAdMobBannerID
            bannerLinearLayout.addView(bannerAdView)
            val adRequest = AdRequest.Builder().build()
            bannerAdView.loadAd(adRequest)
        }

        topScoreLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->
            LogUtil.i(TAG, "topScoreLauncher.result = $result")
        }

        onBackPressedDispatcher.addCallback(
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    LogUtil.d(TAG, "handleOnBackPressed")
                    quitGame()
                }
            })
    }

    override fun onStart() {
        super.onStart()
        LogUtil.d(TAG, "onStart")
    }


    override fun onResume() {
        super.onResume()
        LogUtil.d(TAG, "onResume")
        gameView.gameBecomeVisible()
    }

    override fun onPause() {
        super.onPause()
        LogUtil.d(TAG, "onPause")
        gameView.gameBecomeInvisible()
    }

    override fun onStop() {
        LogUtil.d(TAG, "onStop")
        super.onStop()
    }

    override fun onRestart() {
        super.onRestart()
        LogUtil.d(TAG, "onRestart")
    }

    override fun onDestroy() {
        LogUtil.d(TAG, "onDestroy")
        finishApplication()

        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        val wrapper: Context = ContextThemeWrapper(this@MainActivity,
            R.style.menu_text_style)
        // according to the above explanations, the following statement will fit every situation
        val fontScale = ScreenUtil.getPxFontScale(this@MainActivity)
        ScreenUtil.resizeMenuTextIconSize(wrapper, menu, fontScale)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        if (id == R.id.local_top_10) {
            Intent(
                this@MainActivity,
                Top10ScoreActivity::class.java
            ).also {
                topScoreLauncher.launch(it)
            }
            return true
        }

        if (id == R.id.newGame) {
            gameView.releaseSync()
            gameView.newGame()
            return true
        }

        if (id == R.id.quitGame) {
            quitGame()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    fun quitGame() {
        LogUtil.i(TAG, "quitGame")
        finish()
    }

    private fun finishApplication() {
        // release resources and threads
        gameView.releaseSync()
        gameView.stopThreads()
    }
}
