package com.example.videobrowser.functioncenter

import android.content.Context
import android.widget.Toast
import com.example.videobrowser.R

internal object FeatureToggleToast {
    fun showGlobal(context: Context, featureName: String, enabled: Boolean) {
        Toast.makeText(
            context,
            context.getString(
                if (enabled) R.string.toast_feature_enabled else R.string.toast_feature_disabled,
                featureName
            ),
            Toast.LENGTH_SHORT
        ).show()
    }

    fun showForSite(context: Context, featureName: String, hostName: String, enabled: Boolean) {
        Toast.makeText(
            context,
            context.getString(
                if (enabled) {
                    R.string.toast_current_site_feature_enabled
                } else {
                    R.string.toast_current_site_feature_disabled
                },
                featureName,
                hostName
            ),
            Toast.LENGTH_SHORT
        ).show()
    }
}
