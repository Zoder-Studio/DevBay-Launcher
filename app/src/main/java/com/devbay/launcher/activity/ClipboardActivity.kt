package com.devbay.launcher.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.devbay.launcher.databinding.ActivityClipboardBinding

class ClipboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClipboardBinding
    private lateinit var clipboardRepository: ClipboardRepository
    private lateinit var clipboardAdapter: ClipboardAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClipboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        clipboardRepository = ClipboardRepository(applicationContext)

        clipboardAdapter = ClipboardAdapter(
            entries = emptyList(),
            onEntryClick = { entry -> copyToClipboard(entry) },
            onEntryLongClick = { entry -> confirmDeleteEntry(entry) }
        )
        binding.clipboardList.layoutManager = LinearLayoutManager(this)
        binding.clipboardList.adapter = clipboardAdapter

        binding.closeButton.setOnClickListener { finish() }
        binding.clearAllButton.setOnClickListener { confirmClearAll() }

        loadHistory()
    }

    private fun loadHistory() {
        val history = clipboardRepository.getHistory()
        clipboardAdapter.updateEntries(history)
        binding.emptyClipboardState.visibility = if (history.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun copyToClipboard(entry: ClipboardEntry) {
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.setPrimaryClip(ClipData.newPlainText(CLIP_LABEL, entry.text))
        Toast.makeText(this, R.string.clip_copied, Toast.LENGTH_SHORT).show()
    }

    private fun confirmDeleteEntry(entry: ClipboardEntry) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_clip_title)
            .setPositiveButton(R.string.action_remove) { dialog, _ ->
                clipboardRepository.removeEntry(entry)
                loadHistory()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.action_close, null)
            .show()
    }

    private fun confirmClearAll() {
        AlertDialog.Builder(this)
            .setTitle(R.string.clear_clipboard_title)
            .setPositiveButton(R.string.action_clear) { dialog, _ ->
                clipboardRepository.clearAll()
                loadHistory()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.action_close, null)
            .show()
    }

    companion object {
        private const val CLIP_LABEL = "DevBay Clipboard"
    }
}