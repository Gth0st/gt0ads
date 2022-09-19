package gt0.ads

import android.app.Activity
import android.app.Application
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.gson.Gson
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import com.unity3d.ads.UnityAdsShowOptions
import com.unity3d.services.banners.BannerView
import com.unity3d.services.banners.UnityBannerSize
import kotlinx.coroutines.*
import gt0.ads.interfaces.OnAdvertisingCoreInitialized
import gt0.ads.api_configurations.AdUnitsConfiguration
import gt0.ads.utils.*
import gt0.ads.enums.AdUnitType
import gt0.ads.network.AdUnitsServe
import java.lang.Exception


class GtGtAdImpl(private val app: Application) : GtAd, OnAdvertisingCoreInitialized {
    private var isPrem: Boolean = false
    override var adsUnitUtil: AdsUnitUtil? = null
    override var adUnitsServe: AdUnitsServe? = null
    private val loadHelper = AdServeLoadHelper(app)
    override val adsFrameShow = AdsFrameShow()
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private var lInterstitial: InterstitialAd? = null
    private var lRewardedAd: RewardedInterstitialAd? = null
    private var lNativeAd: NativeAd? = null
    private var lAdView: AdView? = null
    private var coreCallback: OnAdvertisingCoreInitialized? = null


    init {
        adUnitsServe = AdUnitsServe()
        adsUnitUtil = AdsUnitUtil(app, this, adUnitsServe ?: AdUnitsServe())
    }

    override suspend fun setDefaultUnits(strJson: String) {
        if (strJson.isNotEmpty()) {
            try {
                val units = Gson().fromJson(strJson, AdUnitsConfiguration::class.java)
                adsUnitUtil?.defaultAdUnits(units)
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("GT0ADS_ERROR", "Failed to set default ad units  ${e.message}")
            }
        } else {
            Log.e("GT0ADS_ERROR", "Failed to set default ad units. JSON Is empty")
        }
    }


    override suspend fun initGt0Ads(
        id: String,
        action: () -> Unit,
        isPrem: Boolean
    ) {
        this.isPrem = isPrem
        adUnitsServe?.setAdUnitsUrl(id)
        adUnitsServe?.req { adUnit ->
            if (adUnit != null) {
                Log.d("GT0ADS", "GT0ADS REQUESTED AD UNITS ${System.currentTimeMillis()}")
                adsUnitUtil?.serveAdUnits(adUnit)
            }
            adsUnitUtil?.initAd(action)
        }

    }

    override suspend fun showDelayedInter(activity: Activity, onAdClosed: () -> Unit) {
        if (isPrem) {
            delay(2000)
            onAdClosed.invoke()
            return
        }
        adsUnitUtil?.checkAdUnit(AdUnitType.INTER) {
            if (it) {
                if (adsUnitUtil?.serverProviderIsAdmob() == true) {
                    scope.launch(Dispatchers.Main) {
                        val job = Job()
                        launch(job) {
                            delay(7000)
                            onAdClosed.invoke()
                            job.cancel()
                            Log.d("GT0ADS", "JOB CANCEL TIMEOUT")
                        }
                        launch(job) {
                            loadHelper.loadInter(adsUnitUtil?.adUnit(AdUnitType.INTER)
                                .toString()) { inter ->
                                if (inter == null) {
                                    Log.d("GT0ADS", "SPLASH INTER IS NULL")
                                    scope.launch {
                                        delay(3000)
                                        onAdClosed.invoke()
                                    }
                                } else {
                                    scope.launch {
                                        inter.fullScreenContentCallback =
                                            object : FullScreenContentCallback() {
                                                override fun onAdDismissedFullScreenContent() {
                                                    super.onAdDismissedFullScreenContent()
                                                    inter.fullScreenContentCallback = null
                                                    onAdClosed.invoke()
                                                }

                                                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                                                    super.onAdFailedToShowFullScreenContent(p0)
                                                    inter.fullScreenContentCallback = null
                                                }
                                            }
                                        inter.show(activity)
                                    }
                                }
                                Log.d("GT0ADS", "JOB CANCEL LOADED INTER")
                                job.cancel()
                            }
                        }
                    }
                } else {
                    scope.launch {
                        delay(5000)
                        UnityAds.show(activity,
                            adsUnitUtil?.adUnit(AdUnitType.INTER),
                            UnityAdsShowOptions(),
                            object : IUnityAdsShowListener {
                                override fun onUnityAdsShowFailure(
                                    placementId: String?,
                                    error: UnityAds.UnityAdsShowError?,
                                    message: String?
                                ) {
                                    onAdClosed.invoke()
                                    loadInterstitialUnity()
                                }

                                override fun onUnityAdsShowStart(placementId: String?) {}
                                override fun onUnityAdsShowClick(placementId: String?) {}
                                override fun onUnityAdsShowComplete(
                                    placementId: String?,
                                    state: UnityAds.UnityAdsShowCompletionState?
                                ) {
                                    loadInterstitialUnity()
                                    onAdClosed.invoke()
                                }

                            }
                        )
                    }
                }
            } else {
                Log.e("GT0ADS", "AD UNIT INTER INCORRECT")
                scope.launch {
                    delay(2000)
                    onAdClosed.invoke()
                }
            }
        }
    }

    override suspend fun setAdCoreListener(callback: OnAdvertisingCoreInitialized) {
        this.coreCallback = callback
        Log.d("GT0ADS", "Global callback configured")
    }

    override suspend fun showInterstitial(activity: Activity, onAdClosed: () -> Unit) {
        if (isPrem) {
            onAdClosed.invoke()
            return
        }
        adsUnitUtil?.checkAdUnit(AdUnitType.INTER) {
            if (it) {
                if (adsUnitUtil?.serverProviderIsAdmob() == true) {
                    if (lInterstitial != null) {
                        lInterstitial?.fullScreenContentCallback = object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                super.onAdDismissedFullScreenContent()
                                lInterstitial?.fullScreenContentCallback = null
                                lInterstitial = null
                                onAdClosed.invoke()
                                loadInterstitialAd()
                            }

                            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                                super.onAdFailedToShowFullScreenContent(p0)
                                lInterstitial?.fullScreenContentCallback = null
                                lInterstitial = null
                                loadInterstitialAd()
                            }
                        }
                        lInterstitial?.show(activity)
                    } else {
                        onAdClosed.invoke()
                        loadInterstitialAd()
                    }
                } else {
                    UnityAds.show(activity,
                        adsUnitUtil?.adUnit(AdUnitType.INTER),
                        UnityAdsShowOptions(),
                        object : IUnityAdsShowListener {
                            override fun onUnityAdsShowFailure(
                                placementId: String?,
                                error: UnityAds.UnityAdsShowError?,
                                message: String?
                            ) {
                                onAdClosed.invoke()
                                loadInterstitialUnity()
                            }

                            override fun onUnityAdsShowStart(placementId: String?) {}
                            override fun onUnityAdsShowClick(placementId: String?) {}
                            override fun onUnityAdsShowComplete(
                                placementId: String?,
                                state: UnityAds.UnityAdsShowCompletionState?
                            ) {
                                loadInterstitialUnity()
                                onAdClosed.invoke()
                            }

                        }
                    )
                }
            } else {
                onAdClosed.invoke()
            }
        }
    }

    override suspend fun showRewardedAd(
        activity: Activity,
        onRewardClosed: (rewarded: Boolean) -> Unit
    ) {
        if (isPrem) {
            onRewardClosed.invoke(true)
            return
        }
        adsUnitUtil?.checkAdUnit(AdUnitType.REWARD) {
            if (it) {
                if (adsUnitUtil?.serverProviderIsAdmob() == true) {
                    var rewarded = false
                    if (lRewardedAd != null) {
                        lRewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                super.onAdDismissedFullScreenContent()
                                lRewardedAd = null
                                lRewardedAd?.fullScreenContentCallback = null
                                onRewardClosed.invoke(rewarded)
                                loadRewardedAd()
                            }

                            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                                super.onAdFailedToShowFullScreenContent(p0)
                                lRewardedAd = null
                                lRewardedAd?.fullScreenContentCallback = null
                                loadRewardedAd()
                            }
                        }
                        lRewardedAd?.show(activity) {
                            rewarded = true
                        }
                    } else {
                        onRewardClosed.invoke(false)
                    }
                } else {
                    UnityAds.show(activity,
                        adsUnitUtil?.adUnit(AdUnitType.REWARD),
                        UnityAdsShowOptions(),
                        object : IUnityAdsShowListener {
                            override fun onUnityAdsShowFailure(
                                placementId: String?,
                                error: UnityAds.UnityAdsShowError?,
                                message: String?
                            ) {
                                onRewardClosed.invoke(false)
                                loadRewardedUnity()
                            }

                            override fun onUnityAdsShowStart(placementId: String?) {}
                            override fun onUnityAdsShowClick(placementId: String?) {}
                            override fun onUnityAdsShowComplete(
                                placementId: String?,
                                state: UnityAds.UnityAdsShowCompletionState?
                            ) {
                                loadRewardedUnity()
                                onRewardClosed.invoke(state?.equals(UnityAds.UnityAdsShowCompletionState.COMPLETED) == true)
                            }

                        }
                    )
                }
            } else {
                onRewardClosed.invoke(false)
            }
        }
    }

    override suspend fun showFrameAd(activity: Activity, frameLayout: FrameLayout) {
        if (isPrem) {
            frameLayout.visibility = GONE
            return
        }
        adsUnitUtil?.checkAdUnit(AdUnitType.NATIVE) {
            if (it) {
                if (adsUnitUtil?.serverProviderIsAdmob() == true) {
                    val viewTreeObserver = frameLayout.viewTreeObserver
                    viewTreeObserver.addOnGlobalLayoutListener(object :
                        ViewTreeObserver.OnGlobalLayoutListener {
                        override fun onGlobalLayout() {
                            frameLayout.viewTreeObserver.removeGlobalOnLayoutListener(this);
                            val height = frameLayout.height
                            val heightDp = SizeUtils.pxToDp(app, height)

                            when {
                                heightDp >= 270 -> {
                                    scope.launch {
                                        Log.e(
                                            "GT0ADS",
                                            "Showing big native ad. Preloaded :${lNativeAd != null}"
                                        )
                                        if (lNativeAd == null) {
                                            lNativeAd = loadNativeAdOnReq().await()
                                        }
                                        Log.e(
                                            "GT0ADS",
                                            "Showing big native ad. Load status : ${lNativeAd != null}"
                                        )
                                        adsFrameShow.showNative(frameLayout, false, lNativeAd)
                                        lNativeAd = null
                                        loadNativeAd()
                                    }
                                }
                                heightDp >= 150 -> {
                                    scope.launch {
                                        if (lNativeAd == null) {
                                            lNativeAd = loadNativeAdOnReq().await()
                                        }

                                        adsFrameShow.showNative(frameLayout, true, lNativeAd)
                                        lNativeAd = null
                                        loadNativeAd()
                                    }
                                }
                                height >= 50 -> {
                                    showBanner(activity, frameLayout)
                                }
                            }
                        }
                    })
                } else {
                    Log.e("GT0ADS", "Admob is selected. disabling native ad")
                    showBanner(activity, frameLayout)
                }
            } else {
                Log.e("GT0ADS", "Incorrect native ad units")
                frameLayout.visibility = GONE
            }
        }
    }

    override fun onAdvertisingCoreInitialized() {
        loadHelper.setIsAdmob(adsUnitUtil?.serverProviderIsAdmob() == true)
        coreCallback?.onAdvertisingCoreInitialized()
        if (adsUnitUtil?.serverProviderIsAdmob() == true) {
            loadInterstitialAd()
            loadNativeAd()
            loadRewardedAd()
        } else {
            loadInterstitialUnity()
            loadRewardedUnity()
        }
    }

    private fun loadInterstitialAd() {
        scope.launch(Dispatchers.Main) {
            loadHelper.loadInter(adsUnitUtil?.adUnit(AdUnitType.INTER).toString()) {
                if (lInterstitial == null)
                    lInterstitial = it
            }
        }
    }

    private fun loadInterstitialUnity() {
        scope.launch(Dispatchers.Main) {
            loadHelper.loadInterUnity(adsUnitUtil?.adUnit(AdUnitType.INTER).toString()) {

            }
        }
    }


    private fun loadRewardedUnity() {
        scope.launch(Dispatchers.Main) {
            loadHelper.loadRewardUnity(adsUnitUtil?.adUnit(AdUnitType.REWARD).toString()) {

            }
        }
    }

    private fun loadRewardedAd() {
        scope.launch(Dispatchers.Main) {
            loadHelper.loadReward(adsUnitUtil?.adUnit(AdUnitType.REWARD).toString()) {
                if (lRewardedAd == null)
                    lRewardedAd = it
            }
        }
    }

    private fun loadNativeAd() {
        loadHelper.loadNativeAd(adsUnitUtil?.adUnit(AdUnitType.NATIVE).toString()) {
            if (lNativeAd == null)
                lNativeAd = it
        }
    }

    override suspend fun removeNativeAd() {
        lNativeAd?.destroy()
        lNativeAd = null
    }

    private fun loadNativeAdOnReq(): CompletableDeferred<NativeAd?> {
        val load = CompletableDeferred<NativeAd?>()
        val native = AdLoader.Builder(app, adsUnitUtil?.adUnit(AdUnitType.NATIVE).toString())
            .forNativeAd {
                load.complete(it)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    load.complete(null)
                }
            })
            .build()

        native.loadAd(AdRequest.Builder().build())
        return load
    }

    fun showBanner(activity: Activity, bannerView: FrameLayout) {
        if (isPrem) {
            bannerView.visibility = GONE
            return
        }
        adsUnitUtil?.checkAdUnit(AdUnitType.BANNER) {
            if (it) {
                if (adsUnitUtil?.serverProviderIsAdmob() == true) {
                    lAdView = AdView(activity)
                    lAdView?.adUnitId = adsUnitUtil?.adUnit(AdUnitType.BANNER).toString()
                    try {
                        bannerView.addView(lAdView)
                        loadBanner(activity)
                        bannerView.visibility = View.VISIBLE
                    } catch (e: Exception) {
                        e.printStackTrace()
                        bannerView.visibility = GONE
                    }
                } else {
                    val bottomBanner =
                        BannerView(
                            activity,
                            adsUnitUtil?.adUnit(AdUnitType.BANNER),
                            UnityBannerSize(320, 50)
                        )
                    bottomBanner.listener = null
                    bottomBanner.load()
                    bannerView.addView(bottomBanner)
                }
            } else {
                bannerView.visibility = GONE
            }

        }

    }

    private fun loadBanner(activity: Activity) {
        if (isPrem) {
            return
        }
        val adRequest = AdRequest.Builder()
            .build()
        val adSize: AdSize = SizeUtils.getAdSize(activity)
        lAdView?.setAdSize(adSize)
        lAdView?.loadAd(adRequest)
    }

}
