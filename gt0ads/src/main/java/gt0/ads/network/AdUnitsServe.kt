package gt0.ads.network

import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import gt0.ads.api_configurations.AdUnitsConfiguration
import java.lang.Exception
import javax.security.auth.callback.Callback

class AdUnitsServe {
    private var retriesAds = 0
    private var url: String = ""

    fun setAdUnitsUrl(unitsUrl: String) {
        this.url = unitsUrl
    }

    fun req(result: (adUnitsConfiguration: AdUnitsConfiguration?) -> Unit) {
        if (retriesAds > 3) {
            result.invoke(null)
            return
        }
        if (url.contains("https://")) {
            val request = Request.Builder()
                .url(url)
                .build()
            OkHttpClient().newCall(request).enqueue(object : Callback, okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                    e.printStackTrace()
                    result.invoke(null)
                }

                override fun onResponse(call: okhttp3.Call, response: Response) {
                    if (response.isSuccessful) {
                        try {
                            val units = Gson().fromJson(response.body?.string(), AdUnitsConfiguration::class.java)
                            result.invoke(units)
                            retriesAds = 0
                        } catch (e: Exception) {
                            result.invoke(null)
                        }

                    } else {
                        result.invoke(null)
                    }
                }
            })
        }
    }

    fun retryReq(result: (adUnitsConfiguration: AdUnitsConfiguration?) -> Unit) {
        if (url.contains("https://")) {
            retriesAds += 1
            req(result)
        }
    }
}