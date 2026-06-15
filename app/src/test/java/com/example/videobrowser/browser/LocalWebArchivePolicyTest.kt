package com.example.videobrowser.browser

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalWebArchivePolicyTest {
    @Test
    fun isWebArchiveMatchesMhtmlFileNames() {
        assertTrue(LocalWebArchivePolicy.isWebArchive("Saved Page.mhtml", null))
        assertTrue(LocalWebArchivePolicy.isWebArchive("Saved Page.MHT", null))
    }

    @Test
    fun isWebArchiveMatchesKnownMimeTypes() {
        assertTrue(LocalWebArchivePolicy.isWebArchive("download", "multipart/related; boundary=page"))
        assertTrue(LocalWebArchivePolicy.isWebArchive("download", "application/x-mimearchive"))
    }

    @Test
    fun isWebArchiveRejectsOrdinaryDocuments() {
        assertFalse(LocalWebArchivePolicy.isWebArchive("report.pdf", "application/pdf"))
        assertFalse(LocalWebArchivePolicy.isWebArchive("page.html", "text/html"))
    }
}
