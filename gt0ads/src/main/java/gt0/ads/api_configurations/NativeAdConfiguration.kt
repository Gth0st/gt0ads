package gt0.ads.api_configurations

import android.graphics.Typeface
import android.util.Log

class NativeAdConfiguration {
    var font: Typeface? = null
    var headlineColorHex: String? = null
    var nativeAdBackgroundHex: String? = null
    var actionButtonHex: String? = null
    var actionButtonTextHex: String? = null

    fun setupNativeAdConfiguration(
        fontTypeFace: Typeface,
        headlinesColorHex: String,
        nativeAdBackgroundHex: String,
        actionButtonHex: String,
        actionButtonTextHex: String
    ) {
        this.font = fontTypeFace
        this.headlineColorHex = headlinesColorHex
        this.nativeAdBackgroundHex = nativeAdBackgroundHex
        this.actionButtonHex = actionButtonHex
        this.actionButtonTextHex = actionButtonTextHex
        Log.d("GT0ADS", "Native ad configured")
    }
}