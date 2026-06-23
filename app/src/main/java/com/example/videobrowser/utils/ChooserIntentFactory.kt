package com.example.videobrowser.utils

import android.content.Context
import android.content.Intent

object ChooserIntentFactory {
    fun create(context: Context, intent: Intent, chooserTitleRes: Int): Intent {
        return Intent.createChooser(intent, context.getString(chooserTitleRes))
    }
}
