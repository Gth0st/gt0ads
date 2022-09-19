package gt0.ads

import android.app.Activity
import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.ConnectivityManager
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.google.gson.Gson
import com.unity3d.ads.*
import com.unity3d.ads.UnityAds.UnityAdsInitializationError
import com.unity3d.services.banners.BannerView
import com.unity3d.services.banners.UnityBannerSize
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import gt0.ads.api_configurations.AdUnitsConfiguration
import gt0.ads.api_configurations.NativeAdConfiguration
import javax.security.auth.callback.Callback
import kotlin.math.roundToInt


class StaticNativeAd {
    companion object {

        fun showNative(
            frameLayout: FrameLayout, adUnit: String, nativeAdConfigurationConfig: NativeAdConfiguration
        ) {
            if (nativeAdConfigurationConfig.nativeAdBackgroundHex == null)
                return
            try {
                val adLoader = AdLoader.Builder(frameLayout.context, adUnit)
                    .forNativeAd { nativeAd: NativeAd ->
                        try {
                            val unifiedNativeAdView = LayoutInflater.from(frameLayout.context)
                                .inflate(
                                    R.layout.ad_layoujt,
                                    null
                                ) as CardView
                            unifiedNativeAdView.setCardBackgroundColor(
                                Color.parseColor(
                                    nativeAdConfigurationConfig.nativeAdBackgroundHex
                                )
                            )
                            unifiedNativeAdView.findViewById<TextView>(R.id.ad_call_to_action).typeface =
                                nativeAdConfigurationConfig.font
                            unifiedNativeAdView.findViewById<TextView>(R.id.ad_body).typeface =
                                nativeAdConfigurationConfig.font
                            unifiedNativeAdView.findViewById<TextView>(R.id.ad_headline).typeface =
                                nativeAdConfigurationConfig.font
                            //set text color
                            unifiedNativeAdView.findViewById<TextView>(R.id.ad_body)
                                .setTextColor(Color.parseColor(nativeAdConfigurationConfig.headlineColorHex))
                            unifiedNativeAdView.findViewById<TextView>(R.id.ad_headline)
                                .setTextColor(Color.parseColor(nativeAdConfigurationConfig.headlineColorHex))
                            //set btn color
                            unifiedNativeAdView.findViewById<Button>(R.id.ad_call_to_action).backgroundTintList =
                                ColorStateList.valueOf(Color.parseColor(nativeAdConfigurationConfig.actionButtonHex))
                            unifiedNativeAdView.findViewById<Button>(R.id.ad_call_to_action)
                                .setTextColor(Color.parseColor(nativeAdConfigurationConfig.actionButtonTextHex))
                            mapUnifiedNativeAdToLayout(nativeAd, unifiedNativeAdView)
                            frameLayout.removeAllViews()
                            frameLayout.addView(unifiedNativeAdView)
                        } catch (e: java.lang.Exception) {
                            frameLayout.visibility = View.GONE
                            e.printStackTrace()
                        }
                    }
                    .withAdListener(object : AdListener() {
                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            super.onAdFailedToLoad(loadAdError)
                            frameLayout.visibility = View.GONE
                            Log.d("AdInfo", "Native failed to load $loadAdError")
                        }

                        override fun onAdClosed() {}
                        override fun onAdOpened() {}
                        override fun onAdClicked() {}
                    })
                    .build()
                adLoader.loadAd(AdRequest.Builder().build())
            } catch (e: java.lang.Exception) {
                frameLayout.visibility = View.GONE
                e.printStackTrace()
            }
        }

        private fun mapUnifiedNativeAdToLayout(adFromGoogle: NativeAd, card: CardView) {
            val myAdView: NativeAdView =
                card.findViewById(R.id.unifiedNativeAdView)
            val mediaView: MediaView =
                myAdView.findViewById(R.id.ad_media)
            myAdView.mediaView = mediaView
            myAdView.headlineView = myAdView.findViewById(R.id.ad_headline)
            myAdView.bodyView = myAdView.findViewById(R.id.ad_body)
            myAdView.callToActionView = myAdView.findViewById(R.id.ad_call_to_action)
            myAdView.iconView = myAdView.findViewById(R.id.ad_icon)
            (myAdView.headlineView as TextView).text = adFromGoogle.headline
            if (adFromGoogle.body == null) {
                myAdView.bodyView?.visibility = View.GONE
            } else {
                (myAdView.bodyView as TextView).text = adFromGoogle.body
            }
            if (adFromGoogle.callToAction == null) {
                myAdView.callToActionView?.visibility = View.GONE
            } else {
                (myAdView.callToActionView as Button).text = adFromGoogle.callToAction
            }
            if (adFromGoogle.icon == null) {
                myAdView.iconView?.visibility = View.GONE
            } else {
                (myAdView.iconView as ImageView).setImageDrawable(adFromGoogle.icon?.drawable)
            }
            myAdView.setNativeAd(adFromGoogle)
        }
    }

}
