/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2026 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android.ui.main.settings.secure

import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import org.fcitx.fcitx5.android.R
import org.fcitx.fcitx5.android.data.prefs.AppPrefs

class EncryptionManagementFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val secure = AppPrefs.getInstance().secure
        val clipboard = AppPrefs.getInstance().clipboard
        preferenceScreen = preferenceManager.createPreferenceScreen(preferenceManager.context).apply {
            addPreference(ListPreference(preferenceManager.context).apply {
                key = secure.encryptMode.key
                title = getString(R.string.encrypt_mode)
                entries = arrayOf("密码本 (Codebook)")
                entryValues = arrayOf("CODEBOOK")
                setDefaultValue("CODEBOOK")
                summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
            })
            addPreference(EditTextPreference(preferenceManager.context).apply {
                key = secure.codebookId.key
                title = getString(R.string.codebook_id)
                setDefaultValue("default")
                summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()
            })
            addPreference(SwitchPreference(preferenceManager.context).apply {
                key = clipboard.clipboardListening.key
                title = getString(R.string.clipboard_auto_decrypt)
                setDefaultValue(true)
                setOnPreferenceChangeListener { _, newValue ->
                    clipboard.clipboardListening.setValue(newValue as Boolean)
                    true
                }
            })
        }
    }
}