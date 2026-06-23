package com.example.videobrowser.functioncenter

import android.content.Context
import com.example.videobrowser.R
import com.example.videobrowser.utils.ShortToast

internal object FeatureToggleToast {
    fun showGlobal(context: Context, featureName: String, enabled: Boolean) {
        ShortToast.show(
            context,
            context.getString(
                if (enabled) R.string.toast_feature_enabled else R.string.toast_feature_disabled,
                featureName
            )
        )
    }

    fun showForSite(context: Context, featureName: String, hostName: String, enabled: Boolean) {
        ShortToast.show(
            context,
            context.getString(
                if (enabled) {
                    R.string.toast_current_site_feature_enabled
                } else {
                    R.string.toast_current_site_feature_disabled
                },
                featureName,
                hostName
            )
        )
    }
}
