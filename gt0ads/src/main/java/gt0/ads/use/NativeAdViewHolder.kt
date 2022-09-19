package gt0.ads.use

import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import gt0.ads.StaticNativeAd
import gt0.ads.R
import gt0.ads.api_configurations.NativeAdConfiguration

class NativeAdViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val holder: FrameLayout = view.findViewById(R.id.nativeAdHolder)

    fun bindAd(nativeADUnit: String, nativeADConfigurationConfig: NativeAdConfiguration) {
        StaticNativeAd.showNative(holder, nativeADUnit, nativeADConfigurationConfig)
    }
}