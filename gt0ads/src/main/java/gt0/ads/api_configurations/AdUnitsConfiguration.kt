package gt0.ads.api_configurations

import com.google.gson.annotations.SerializedName

data class AdUnitsConfiguration
    (
    @SerializedName("admob")
    val isAdmobProvider: Boolean = true,
    @SerializedName("app")
    val app_id : String = "",
    @SerializedName("interstitial")
    val interUnit : String = "",
    @SerializedName("native")
    val nativeUnit : String = "",
    @SerializedName("open")
    val openAdsUnit : String = "",
    @SerializedName("rewarded")
    val rewardAdUnit : String = "",
    @SerializedName("banner")
    val bannerAdUnit : String = "",
)