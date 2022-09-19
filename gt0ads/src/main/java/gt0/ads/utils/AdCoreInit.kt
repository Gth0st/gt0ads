package gt0.ads.utils

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import com.google.android.gms.ads.MobileAds
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.UnityAds
import gt0.ads.interfaces.OnAdvertisingCoreInitialized
import gt0.ads.api_configurations.AdUnitsConfiguration

object AdCoreInit {
    fun initAdCore(application: Application, adUnitsConfiguration: AdUnitsConfiguration, onAdvertisingCoreInitialized: OnAdvertisingCoreInitialized) {
        if (adUnitsConfiguration.isAdmobProvider) {
            try {
                val applicationInfo: ApplicationInfo = application.packageManager.getApplicationInfo(
                    application.packageName,
                    PackageManager.GET_META_DATA
                )
                applicationInfo.metaData.putString(
                    "com.google.android.gms.ads.APPLICATION_ID",
                    adUnitsConfiguration.app_id
                )
                MobileAds.initialize(application) {
                    onAdvertisingCoreInitialized.onAdvertisingCoreInitialized()
                    Log.e("GT0ADS", "GT0ADS Initialized Successfully")
                }
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            } catch (e: NullPointerException) {
                e.printStackTrace()
            }
        } else {
            UnityAds.initialize(
                application,
                adUnitsConfiguration.app_id,
                false,
                object : IUnityAdsInitializationListener {
                    override fun onInitializationComplete() {
                        onAdvertisingCoreInitialized.onAdvertisingCoreInitialized()
                        Log.e("GT0ADS", "GT0ADS Unity Initialized Successfully")
                    }

                    override fun onInitializationFailed(
                        error: UnityAds.UnityAdsInitializationError?,
                        message: String?
                    ) {
                    }

                })
        }
    }
}