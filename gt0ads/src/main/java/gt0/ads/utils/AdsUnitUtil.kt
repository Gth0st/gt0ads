package gt0.ads.utils

import android.app.Application
import android.util.Log
import gt0.ads.interfaces.OnAdvertisingCoreInitialized
import gt0.ads.api_configurations.AdUnitsConfiguration
import gt0.ads.enums.AdUnitType
import gt0.ads.network.AdUnitsServe

class AdsUnitUtil(private val app: Application, private val onAdvertisingCoreInitialized: OnAdvertisingCoreInitialized, private val adUnitsServe: AdUnitsServe) {
    private var adUnitsConfiguration: AdUnitsConfiguration? = null

    fun defaultAdUnits(adUnitsConfiguration: AdUnitsConfiguration) {
        if (adUnitsConfiguration.app_id.isNotEmpty() && adUnitsConfiguration.interUnit.isNotEmpty()) {
            this.adUnitsConfiguration = adUnitsConfiguration
        } else {
            Log.e("GT0ADS_ERROR", "Failed to setup default ad units.")
        }
    }

    fun serveAdUnits(adUnitsConfiguration: AdUnitsConfiguration) {
        if (adUnitsConfiguration.app_id.isNotEmpty() && adUnitsConfiguration.interUnit.isNotEmpty()) {
            this.adUnitsConfiguration = adUnitsConfiguration
        } else if (this.adUnitsConfiguration == null) {
            Log.e("GT0ADS_ERROR", "AD Units is null.")
            Log.e("GT0ADS_ERROR", "Default ad units not specified")
        } else {
            Log.e("GT0ADS_ERROR", "Server returned incorrect ad units. Using defaults. ")
        }
    }

    fun serverProviderIsAdmob(): Boolean = adUnitsConfiguration?.isAdmobProvider == true

    fun adUnit(adUnitType: AdUnitType): String {
        when (adUnitType) {
            AdUnitType.BANNER -> {
                return adUnitsConfiguration?.bannerAdUnit ?: ""
            }
            AdUnitType.INTER -> {
                return adUnitsConfiguration?.interUnit ?: ""
            }
            AdUnitType.NATIVE -> {
                return adUnitsConfiguration?.nativeUnit ?: ""
            }
            AdUnitType.OPEN -> {
                return adUnitsConfiguration?.openAdsUnit ?: ""
            }
            AdUnitType.REWARD -> {
                return adUnitsConfiguration?.rewardAdUnit ?: ""
            }
        }
    }

    fun checkAdUnit(adUnitType: AdUnitType, onCorrect: (isCorrect: Boolean) -> Unit) {
        when (adUnitType) {
            AdUnitType.BANNER -> {
                onCorrect.invoke(!adUnitsConfiguration?.bannerAdUnit.isNullOrEmpty())
            }
            AdUnitType.REWARD -> {
                onCorrect.invoke(!adUnitsConfiguration?.rewardAdUnit.isNullOrEmpty())
            }
            AdUnitType.INTER -> {
                onCorrect.invoke(!adUnitsConfiguration?.interUnit.isNullOrEmpty())
            }
            AdUnitType.NATIVE -> {
                if (adUnitsConfiguration?.nativeUnit.isNullOrEmpty()) {
                    retryADUnits()
                }
                onCorrect.invoke(!adUnitsConfiguration?.nativeUnit.isNullOrEmpty())
            }
            AdUnitType.OPEN -> {
                onCorrect.invoke(!adUnitsConfiguration?.openAdsUnit.isNullOrEmpty())
            }
        }
    }

    private fun retryADUnits() {
        adUnitsServe.retryReq {
            if (it != null) {
                serveAdUnits(it)
                initAd(null)
            }
        }
    }

    fun initAd(action: (() -> Unit)?) {
        adUnitsConfiguration?.let { it1 -> AdCoreInit.initAdCore(app, it1, onAdvertisingCoreInitialized) }
        action?.invoke()
    }

}