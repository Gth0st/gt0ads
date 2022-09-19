package gt0.ads.utils

import android.app.Application
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.UnityAds
import kotlinx.coroutines.*

class AdServeLoadHelper(private val application: Application) {
    private var isAdmob: Boolean = true
    private var interAttempts = 0
    private var rewardAttempts = 0
    private var scope = CoroutineScope(Dispatchers.Main + Job())

    fun setIsAdmob(admob: Boolean) {
        isAdmob = admob
    }

    fun loadInter(adUnit: String, interResult: (inter: InterstitialAd?) -> Unit) {
        InterstitialAd.load(
            application,
            adUnit,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    super.onAdFailedToLoad(error)
                    interResult.invoke(null)
                    scope.launch {
                        delay(3000)
                        if (interAttempts >= 3) {
                            return@launch
                        }
                        interAttempts += 1
                        loadInter(adUnit, interResult)
                    }
                }

                override fun onAdLoaded(inter: InterstitialAd) {
                    super.onAdLoaded(inter)
                    interAttempts = 0
                    interResult.invoke(inter)
                }
            })
    }

    fun loadReward(adUnit: String, rewardResult: (reward: RewardedInterstitialAd?) -> Unit) {
        RewardedInterstitialAd.load(
            application,
            adUnit,
            AdRequest.Builder().build(),
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(p0: RewardedInterstitialAd) {
                    super.onAdLoaded(p0)
                    rewardAttempts = 0
                    rewardResult.invoke(p0)
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    rewardResult.invoke(null)
                    scope.launch {
                        delay(2000)
                        if (rewardAttempts >= 3) {
                            return@launch
                        }
                        rewardAttempts += 1
                        loadReward(adUnit, rewardResult)
                    }

                }
            })
    }

    fun loadNativeAd(adUnit: String, nativeAdResult: (nativeAd: NativeAd?) -> Unit) {
        AdLoader.Builder(application, adUnit)
            .forNativeAd {
                nativeAdResult.invoke(it)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    nativeAdResult.invoke(null)
                }
            }).build().loadAd(AdRequest.Builder().build())
    }

    fun loadInterUnity(adUnit: String, interResult: () -> Unit){
        UnityAds.load(adUnit, object : IUnityAdsLoadListener {
            override fun onUnityAdsAdLoaded(placementId: String?) {
                interAttempts = 0
                interResult.invoke()
            }

            override fun onUnityAdsFailedToLoad(
                placementId: String?,
                error: UnityAds.UnityAdsLoadError?,
                message: String?
            ) {
                scope.launch {
                    delay(3000)
                    if (interAttempts >= 3) {
                        return@launch
                    }
                    interAttempts += 1
                    loadInterUnity(adUnit, interResult)
                }
            }
        })
    }

    fun loadRewardUnity(adUnit: String, function: () -> Unit) {
        UnityAds.load(adUnit, object : IUnityAdsLoadListener {
            override fun onUnityAdsAdLoaded(placementId: String?) {
                rewardAttempts = 0
                function.invoke()
            }

            override fun onUnityAdsFailedToLoad(
                placementId: String?,
                error: UnityAds.UnityAdsLoadError?,
                message: String?
            ) {
                scope.launch {
                    delay(3000)
                    if (rewardAttempts >= 3) {
                        return@launch
                    }
                    rewardAttempts += 1
                    loadRewardUnity(adUnit, function)
                }
            }
        })
    }
}