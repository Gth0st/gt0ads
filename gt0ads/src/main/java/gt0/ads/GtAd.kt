package gt0.ads

import android.app.Activity
import android.widget.FrameLayout
import gt0.ads.interfaces.OnAdvertisingCoreInitialized
import gt0.ads.utils.AdsUnitUtil
import gt0.ads.utils.AdsFrameShow
import gt0.ads.network.AdUnitsServe

interface GtAd {
    val adsFrameShow : AdsFrameShow
    val adsUnitUtil : AdsUnitUtil?
    suspend fun showDelayedInter(activity: Activity, onAdClosed: () -> Unit)
    suspend fun setAdCoreListener(callback : OnAdvertisingCoreInitialized)
    suspend fun showInterstitial(activity: Activity, onAdClosed: () -> Unit)
    suspend fun showRewardedAd(activity: Activity, onRewardClosed: (rewarded: Boolean) -> Unit)
    suspend fun showFrameAd(activity: Activity, frameLayout: FrameLayout)
    suspend fun setDefaultUnits(strJson: String)
    suspend fun removeNativeAd()
    suspend fun initGt0Ads(
        id: String,
        action: () -> Unit,
        isPrem: Boolean = false
    )

    var adUnitsServe: AdUnitsServe?
}