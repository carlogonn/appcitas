package com.appcitas.app.service

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.appcitas.app.util.Constants
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdMobService @Inject constructor() {

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null

    // Load banner ad
    fun loadBannerAd(adView: AdView, adUnitId: String = Constants.ADMOB_BANNER_HOME) {
        adView.adUnitId = adUnitId
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    // Load interstitial ad
    fun loadInterstitialAd(context: Context, adUnitId: String = Constants.ADMOB_INTERSTITIAL) {
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                }
            }
        )
    }

    // Show interstitial ad
    fun showInterstitialAd(activity: Activity, onDismissed: () -> Unit = {}) {
        interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                loadInterstitialAd(activity)
                onDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                interstitialAd = null
                onDismissed()
            }
        }

        interstitialAd?.show(activity)
    }

    // Load rewarded ad
    fun loadRewardedAd(context: Context, adUnitId: String = Constants.ADMOB_REWARDED) {
        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(
            context,
            adUnitId,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                }
            }
        )
    }

    // Show rewarded ad
    fun showRewardedAd(
        activity: Activity,
        onRewarded: () -> Unit,
        onDismissed: () -> Unit = {}
    ) {
        rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                loadRewardedAd(activity)
                onDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                rewardedAd = null
                onDismissed()
            }
        }

        rewardedAd?.show(activity) { rewardItem ->
            onRewarded()
        }
    }

    // Check if ads are available
    fun isInterstitialAdReady(): Boolean = interstitialAd != null
    fun isRewardedAdReady(): Boolean = rewardedAd != null
}
