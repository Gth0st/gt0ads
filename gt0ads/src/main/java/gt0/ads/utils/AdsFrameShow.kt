package gt0.ads.utils

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import gt0.ads.R
import gt0.ads.api_configurations.NativeAdConfiguration


class AdsFrameShow {
    var nativeAdConfigurationConfig: NativeAdConfiguration = NativeAdConfiguration()

    fun showNative(frameLayout: FrameLayout, small: Boolean, nativeAd: NativeAd?) {
        try {
            val nativeAdView = LayoutInflater.from(frameLayout.context)
                .inflate(
                    if (small) R.layout.ad_layout_small else R.layout.ad_layoujt,
                    null
                ) as CardView
            nativeAdView.setCardBackgroundColor(
                Color.parseColor(
                    nativeAdConfigurationConfig?.nativeAdBackgroundHex
                )
            )
            nativeAdView.findViewById<TextView>(R.id.ad_call_to_action).typeface =
                nativeAdConfigurationConfig.font
            nativeAdView.findViewById<TextView>(R.id.ad_body).typeface =
                nativeAdConfigurationConfig.font
            nativeAdView.findViewById<TextView>(R.id.ad_headline).typeface =
                nativeAdConfigurationConfig.font
            //set text color
            nativeAdView.findViewById<TextView>(R.id.ad_body)
                .setTextColor(Color.parseColor(nativeAdConfigurationConfig.headlineColorHex))
            nativeAdView.findViewById<TextView>(R.id.ad_headline)
                .setTextColor(Color.parseColor(nativeAdConfigurationConfig.headlineColorHex))
            //set btn color
            nativeAdView.findViewById<Button>(R.id.ad_call_to_action)?.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor(nativeAdConfigurationConfig.actionButtonHex))
            nativeAdView.findViewById<Button>(R.id.ad_call_to_action)
                ?.setTextColor(Color.parseColor(nativeAdConfigurationConfig.actionButtonTextHex))
            mapUnifiedNativeAdToLayout(nativeAd!!, nativeAdView)
            frameLayout.removeAllViews()
            frameLayout.addView(nativeAdView)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            frameLayout.visibility = GONE
        }
    }

    private fun mapUnifiedNativeAdToLayout(adFromGoogle: NativeAd, card: CardView) {
        val adView: NativeAdView =
            card.findViewById(R.id.unifiedNativeAdView)
        val mediaView: MediaView =
            adView.findViewById(R.id.ad_media)
        adView.mediaView = mediaView
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_icon)
        (adView.headlineView as TextView).text = adFromGoogle.headline
        if (adFromGoogle.body == null) {
            adView.bodyView?.visibility = View.GONE
        } else {
            (adView.bodyView as TextView).text = adFromGoogle.body
        }
        if (adFromGoogle.callToAction == null) {
            adView.callToActionView?.visibility = View.GONE
        } else {
            (adView.callToActionView as Button).text = adFromGoogle.callToAction
        }
        if (adFromGoogle.icon == null) {
            adView.iconView?.visibility = View.GONE
        } else {
            (adView.iconView as ImageView).setImageDrawable(adFromGoogle.icon?.drawable)
        }
        adView.setNativeAd(adFromGoogle)
    }

}
