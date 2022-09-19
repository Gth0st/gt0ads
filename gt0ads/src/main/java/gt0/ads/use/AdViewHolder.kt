package gt0.ads.use

import android.app.Activity
import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import gt0.ads.Ad
import gt0.ads.AdNew
import gt0.ads.R
import gt0.ads.configs.UaNativeAd

class AdViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val adHolder: FrameLayout = view.findViewById(R.id.nativeAdHolder)

    fun bind(nativeADUnit: String, nativeADConfig: UaNativeAd) {
        Ad.showNative(adHolder, nativeADUnit, nativeADConfig)
    }

    fun bindNew(
        nativeADUnit: String,
        nativeADConfig: UaNativeAd,
        scope: CoroutineScope,
        uan: AdNew,
        activity: Activity
    ) {
        Ad.showNative(adHolder, nativeADUnit, nativeADConfig)
    }
}