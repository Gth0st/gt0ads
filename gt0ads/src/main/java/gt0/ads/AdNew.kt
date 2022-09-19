package gt0.ads

import android.app.Activity
import android.widget.FrameLayout
import gt0.ads.callbacks.OnReInit
import gt0.ads.helper.AdUnitsHelper
import gt0.ads.helper.FrameAds
import gt0.ads.net.UnitsRequest

interface AdNew {
    val frameAds : FrameAds
    val adUnitsHelper : AdUnitsHelper?
    suspend fun showSplashInter(activity: Activity, onAdClosed: () -> Unit)
    suspend fun setupGlobalInitListener(callback : OnReInit)
    suspend fun showInter(activity: Activity, onAdClosed: () -> Unit)
    suspend fun showReward(activity: Activity, onRewardClosed: (rewarded: Boolean) -> Unit)
    suspend fun showAdInFrame(activity: Activity, frameLayout: FrameLayout)
    suspend fun setupDefaultAdUnits(strJson: String)
    suspend fun destroyNativeAd()
    suspend fun init(
        projectId: String,
        action: () -> Unit,
        premiumUser: Boolean = false
    )

    var unitsRequest: UnitsRequest?
}