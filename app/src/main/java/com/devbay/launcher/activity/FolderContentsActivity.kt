package com.devbay.launcher.activity

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.devbay.launcher.databinding.ActivityFolderContentsBinding

class FolderContentsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFolderContentsBinding
    private lateinit var folderRepository: FolderRepository
    private lateinit var contentsAdapter: FolderContentsAdapter
    private var folderId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFolderContentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        folderRepository = FolderRepository(applicationContext)
        folderId = intent.getStringExtra(EXTRA_FOLDER_ID) ?: run { finish(); return }

        contentsAdapter = FolderContentsAdapter(
            apps = emptyList(),
            onAppClick = { app -> launchApp(app) },
            onAppLongClick = { app -> confirmRemoveFromFolder(app) }
        )
        binding.folderContentsGrid.layoutManager = GridLayoutManager(this, SPAN_COUNT)
        binding.folderContentsGrid.adapter = contentsAdapter

        binding.closeButton.setOnClickListener { finish() }
        binding.renameButton.setOnClickListener { showRenameDialog() }
        binding.deleteFolderButton.setOnClickListener { confirmDeleteFolder() }

        loadFolderContents()
    }

    private fun loadFolderContents() {
        val folder = folderRepository.getFolders().firstOrNull { it.id == folderId }
        if (folder == null) {
            finish()
            return
        }
        binding.folderContentsTitle.text = folder.name

        val appsByKey = AppCacheStore.getApps().associateBy { it.key }
        val folderApps = folder.appKeys.mapNotNull { appsByKey[it] }
        contentsAdapter.updateApps(folderApps)
        binding.emptyFolderState.visibility = if (folderApps.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun launchApp(app: AppInfo) {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            component = ComponentName(app.packageName, app.activityName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        }
        try {
            startActivity(intent)
            finish()
        } catch (exception: ActivityNotFoundException) {
            Toast.makeText(this, getString(R.string.app_launch_failed, app.label), Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmRemoveFromFolder(app: AppInfo) {
        AlertDialog.Builder(this)
            .setTitle(app.label)
            .setItems(arrayOf(getString(R.string.action_remove_from_folder))) { dialog, _ ->
                folderRepository.removeAppFromFolder(folderId, app.key)
                dialog.dismiss()
                loadFolderContents()
            }
            .show()
    }

    private fun showRenameDialog() {
        val input = EditText(this)
        val folder = folderRepository.getFolders().firstOrNull { it.id == folderId }
        input.setText(folder?.name.orEmpty())

        AlertDialog.Builder(this)
            .setTitle(R.string.rename_folder_title)
            .setView(input)
            .setPositiveButton(R.string.action_save) { dialog, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotBlank()) {
                    folderRepository.renameFolder(folderId, newName)
                    loadFolderContents()
                }
                dialog.dismiss()
            }
            .setNegativeButton(R.string.action_close, null)
            .show()
    }

    private fun confirmDeleteFolder() {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_folder_title)
            .setMessage(R.string.delete_folder_message)
            .setPositiveButton(R.string.action_remove) { dialog, _ ->
                folderRepository.deleteFolder(folderId)
                dialog.dismiss()
                finish()
            }
            .setNegativeButton(R.string.action_close, null)
            .show()
    }

    companion object {
        const val EXTRA_FOLDER_ID = "extra_folder_id"
        private const val SPAN_COUNT = 4
    }
}