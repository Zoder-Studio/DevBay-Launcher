package com.devbay.launcher.activity

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.devbay.launcher.R
import com.devbay.launcher.databinding.*
import com.devbay.launcher.app.*
import com.devbay.launcher.settings.*
import com.devbay.launcher.databinding.ActivitySettingsShortcutsBinding

class SettingsShortcutsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsShortcutsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsShortcutsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.closeButton.setOnClickListener { finish() }

        val shortcuts = SettingsShortcutRepository(applicationContext).getAvailableShortcuts()
        val adapter = SettingsShortcutAdapter(shortcuts) { shortcut -> openShortcut(shortcut) }

        binding.shortcutGrid.layoutManager = GridLayoutManager(this, SPAN_COUNT)
        binding.shortcutGrid.adapter = adapter
        binding.emptyShortcutsState.visibility = if (shortcuts.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun openShortcut(shortcut: SettingsShortcut) {
        try {
            startActivity(Intent(shortcut.intentAction))
        } catch (exception: ActivityNotFoundException) {
            Toast.makeText(this, R.string.shortcut_unavailable, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val SPAN_COUNT = 4
    }
}