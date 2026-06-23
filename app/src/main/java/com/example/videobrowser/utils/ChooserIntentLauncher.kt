package com.example.videobrowser.utils

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.R

object ChooserIntentLauncher {
    fun start(
        activity: AppCompatActivity,
        intent: Intent,
        chooserTitleRes: Int,
        activityNotFoundToastRes: Int = R.string.toast_no_external_browser,
        securityExceptionToastRes: Int? = null
    ) {
        try {
            activity.startActivity(
                ChooserIntentFactory.create(activity, intent, chooserTitleRes)
            )
        } catch (_: ActivityNotFoundException) {
            ShortToast.show(activity, activityNotFoundToastRes)
        } catch (exception: SecurityException) {
            val toastRes = securityExceptionToastRes ?: throw exception
            ShortToast.show(activity, toastRes)
        }
    }
}
