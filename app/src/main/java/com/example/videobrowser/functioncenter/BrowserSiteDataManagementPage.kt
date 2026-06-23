package com.example.videobrowser.functioncenter

import android.webkit.WebStorage
import com.example.videobrowser.R

internal class BrowserSiteDataManagementPage(
    private val host: FunctionCenterPageHost,
    private val dialogController: BrowserDataManagementDialogController,
    private val showRootPage: () -> Unit
) {
    private val activity = host.activity

    fun show(replaceCurrent: Boolean = false, query: String? = null) {
        WebStorage.getInstance().getOrigins { origins ->
            activity.runOnUiThread {
                val siteDataOrigins = origins
                    ?.values
                    ?.filterIsInstance<WebStorage.Origin>()
                    ?.sortedBy { origin -> origin.origin }
                    ?: emptyList()
                showOrigins(siteDataOrigins, replaceCurrent, query)
            }
        }
    }

    private fun showOrigins(
        origins: List<WebStorage.Origin>,
        replaceCurrent: Boolean,
        query: String?
    ) {
        val filteredOrigins = origins.filter { origin ->
            BrowserSiteDataOriginSearch.matches(origin.origin, query)
        }
        host.showPage(
            title = activity.getString(R.string.title_site_data_management),
            onBack = showRootPage,
            replaceCurrent = replaceCurrent
        ) { content ->
            if (origins.isNotEmpty()) {
                host.contentFactory.addFunctionSection(
                    content,
                    activity.getString(R.string.function_center_section_actions)
                ) { section ->
                    host.contentFactory.addActionRow(
                        parent = section,
                        title = activity.getString(R.string.action_search_site_data),
                        summary = SearchSummaryFormatter.current(
                            query,
                            activity.getString(R.string.action_search_site_data_summary)
                        )
                    ) {
                        dialogController.showSiteDataSearchDialog(query) { searchQuery ->
                            show(
                                replaceCurrent = true,
                                query = searchQuery
                            )
                        }
                    }
                    if (!query.isNullOrBlank()) {
                        host.contentFactory.addActionRow(
                            parent = section,
                            title = activity.getString(R.string.action_clear_search),
                            summary = query
                        ) {
                            show(replaceCurrent = true)
                        }
                    }
                    host.contentFactory.addActionRow(
                        parent = section,
                        title = activity.getString(R.string.action_clear),
                        summary = activity.getString(R.string.action_clear_site_data_summary)
                    ) {
                        dialogController.showClearSiteDataDialog {
                            show(replaceCurrent = true)
                        }
                    }
                }
            }

            if (origins.isEmpty()) {
                host.contentFactory.addEmptyState(content, activity.getString(R.string.dialog_site_data_empty))
            } else {
                host.contentFactory.addFunctionSection(
                    content,
                    activity.getString(R.string.function_center_section_records)
                ) { section ->
                    if (filteredOrigins.isEmpty()) {
                        host.contentFactory.addEmptyState(section, activity.getString(R.string.dialog_site_data_search_empty))
                        return@addFunctionSection
                    }
                    filteredOrigins.forEach { origin ->
                        host.contentFactory.addActionRow(
                            parent = section,
                            title = origin.origin,
                            summary = activity.getString(
                                R.string.site_data_usage_summary,
                                BrowserDataDisplayFormatter.siteDataUsageSummary(origin.usage)
                            )
                        ) {
                            dialogController.showRemoveSiteDataDialog(origin.origin) {
                                show(replaceCurrent = true, query = query)
                            }
                        }
                    }
                }
            }
        }
    }

}
