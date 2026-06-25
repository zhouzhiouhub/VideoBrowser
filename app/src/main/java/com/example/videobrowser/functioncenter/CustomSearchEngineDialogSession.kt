package com.example.videobrowser.functioncenter

import android.os.Handler
import android.os.Looper
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.R
import com.example.videobrowser.browser.search.CustomSearchEngineInputAnalysis
import com.example.videobrowser.browser.search.CustomSearchEngineInputAnalyzer
import com.example.videobrowser.browser.search.SearchEngineConfig
import com.example.videobrowser.browser.search.SearchEngineTemplateProber
import com.example.videobrowser.utils.ShortToast
import com.example.videobrowser.utils.TwoTextInputValues
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Coordinates one add/edit custom search engine dialog submission.
 *
 * Synchronous inputs are saved immediately; unknown home URLs are probed off the
 * UI thread and then saved through the same settings callback.
 */
internal class CustomSearchEngineDialogSession(
    private val activity: AppCompatActivity,
    private val templateProber: SearchEngineTemplateProber = SearchEngineTemplateProber(),
    private val postToMain: ((() -> Unit) -> Unit) = { action ->
        Handler(Looper.getMainLooper()).post { action() }
    },
    private val runInBackground: ((() -> Unit) -> Unit) = { action ->
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            try {
                action()
            } finally {
                executor.shutdown()
            }
        }
    }
) {
    private val probeRunning = AtomicBoolean(false)

    fun submit(
        values: TwoTextInputValues,
        @StringRes successToastRes: Int,
        saveConfig: (String, SearchEngineConfig) -> Boolean,
        onSaved: () -> Unit,
        isDialogActive: () -> Boolean,
        dismissDialog: () -> Unit
    ): Boolean {
        val name = values.first.trim()
        if (name.isEmpty()) {
            ShortToast.show(activity, R.string.toast_custom_search_engine_invalid)
            return false
        }
        return when (val analysis = CustomSearchEngineInputAnalyzer.analyze(values.second)) {
            is CustomSearchEngineInputAnalysis.Resolved -> {
                saveResolved(
                    name = name,
                    config = analysis.config,
                    successToastRes = successToastRes,
                    saveConfig = saveConfig,
                    onSaved = onSaved
                )
            }

            is CustomSearchEngineInputAnalysis.ProbeRequired -> {
                startProbe(
                    name = name,
                    homeUrl = analysis.homeUrl,
                    successToastRes = successToastRes,
                    saveConfig = saveConfig,
                    onSaved = onSaved,
                    isDialogActive = isDialogActive,
                    dismissDialog = dismissDialog
                )
                false
            }

            CustomSearchEngineInputAnalysis.Invalid -> {
                ShortToast.show(activity, R.string.toast_custom_search_engine_invalid)
                false
            }
        }
    }

    private fun saveResolved(
        name: String,
        config: SearchEngineConfig,
        @StringRes successToastRes: Int,
        saveConfig: (String, SearchEngineConfig) -> Boolean,
        onSaved: () -> Unit
    ): Boolean {
        val saved = saveConfig(name, config)
        if (saved) {
            ShortToast.show(activity, successToastRes)
            onSaved()
        } else {
            ShortToast.show(activity, R.string.toast_custom_search_engine_invalid)
        }
        return saved
    }

    private fun startProbe(
        name: String,
        homeUrl: String,
        @StringRes successToastRes: Int,
        saveConfig: (String, SearchEngineConfig) -> Boolean,
        onSaved: () -> Unit,
        isDialogActive: () -> Boolean,
        dismissDialog: () -> Unit
    ) {
        if (!probeRunning.compareAndSet(false, true)) {
            ShortToast.show(activity, R.string.toast_custom_search_engine_probe_started)
            return
        }
        ShortToast.show(activity, R.string.toast_custom_search_engine_probe_started)
        runInBackground {
            val result = templateProber.probe(homeUrl = homeUrl, name = name)
            postToMain {
                probeRunning.set(false)
                if (isDialogActive()) {
                    if (result == null) {
                        ShortToast.show(activity, R.string.toast_custom_search_engine_probe_failed)
                    } else {
                        val saved = saveConfig(name, result.config)
                        if (saved) {
                            dismissDialog()
                            ShortToast.show(activity, successToastRes)
                            onSaved()
                        } else {
                            ShortToast.show(activity, R.string.toast_custom_search_engine_invalid)
                        }
                    }
                }
            }
        }
    }
}
