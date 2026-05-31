package com.appcitas.app.ui.components

import android.view.View
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.appcitas.app.util.Constants

@Composable
fun AdBanner(
    modifier: Modifier = Modifier,
    adUnitId: String = Constants.ADMOB_BANNER_HOME,
    adSize: AdSize = AdSize.BANNER
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val adView = remember {
        AdView(context).apply {
            this.adUnitId = adUnitId
            this.adSize = adSize
            loadAd(AdRequest.Builder().build())
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> adView.resume()
                Lifecycle.Event.ON_PAUSE -> adView.pause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            adView.destroy()
        }
    }

    AndroidView(
        factory = { adView },
        modifier = modifier
            .width(AdSize.BANNER.widthInPixels(dp).dp)
            .height(AdSize.BANNER.heightInPixels(dp).dp)
    )
}

@Composable
fun AdBannerLarge(
    modifier: Modifier = Modifier,
    adUnitId: String = Constants.ADMOB_BANNER_HOME
) {
    val context = LocalContext.current

    val adView = remember {
        AdView(context).apply {
            this.adUnitId = adUnitId
            this.adSize = AdSize.LARGE_BANNER
            loadAd(AdRequest.Builder().build())
        }
    }

    AndroidView(
        factory = { adView },
        modifier = modifier
            .width(AdSize.LARGE_BANNER.widthInPixels(dp).dp)
            .height(AdSize.LARGE_BANNER.heightInPixels(dp).dp)
    )
}
