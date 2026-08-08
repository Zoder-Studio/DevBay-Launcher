package com.devbay.launcher.folder

import com.devbay.launcher.app.*
import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class FolderRepository(context: Context) {

    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getFolders(): List<AppFolder> {
        val raw = preferences.getString(KEY_FOLDERS, "") ?: ""
        if (raw.isBlank()) return emptyList()

        val jsonArray = JSONArray(raw)
        return (0 until jsonArray.length()).mapNotNull { index ->
            val obj = jsonArray.optJSONObject(index) ?: return@mapNotNull null
            val id = obj.optString(FIELD_ID)
            val name = obj.optString(FIELD_NAME)
            val keysArray = obj.optJSONArray(FIELD_APP_KEYS) ?: JSONArray()
            val keys = (0 until keysArray.length()).mapNotNull { keysArray.optString(it) }
            if (id.isBlank()) null else AppFolder(id, name, keys)
        }
    }

    fun createFolder(name: String, initialAppKey: String): AppFolder {
        val folder = AppFolder(id = UUID.randomUUID().toString(), name = name, appKeys = listOf(initialAppKey))
        val current = getFolders().toMutableList()
        current.add(folder)
        saveFolders(current)
        return folder
    }

    fun renameFolder(folderId: String, newName: String) {
        val current = getFolders().map { folder ->
            if (folder.id == folderId) folder.copy(name = newName) else folder
        }
        saveFolders(current)
    }

    fun deleteFolder(folderId: String) {
        val current = getFolders().filterNot { it.id == folderId }
        saveFolders(current)
    }

    fun addAppToFolder(folderId: String, appKey: String) {
        val current = getFolders().map { folder ->
            if (folder.id == folderId && appKey !in folder.appKeys) {
                folder.copy(appKeys = folder.appKeys + appKey)
            } else {
                folder
            }
        }
        saveFolders(current)
    }

    fun removeAppFromFolder(folderId: String, appKey: String) {
        val current = getFolders()
            .map { folder ->
                if (folder.id == folderId) folder.copy(appKeys = folder.appKeys.filterNot { it == appKey }) else folder
            }
            .filter { it.appKeys.isNotEmpty() }
        saveFolders(current)
    }

    fun findFolderContaining(appKey: String): AppFolder? {
        return getFolders().firstOrNull { appKey in it.appKeys }
    }

    private fun saveFolders(folders: List<AppFolder>) {
        val jsonArray = JSONArray()
        folders.forEach { folder ->
            val obj = JSONObject()
            obj.put(FIELD_ID, folder.id)
            obj.put(FIELD_NAME, folder.name)
            obj.put(FIELD_APP_KEYS, JSONArray(folder.appKeys))
            jsonArray.put(obj)
        }
        preferences.edit().putString(KEY_FOLDERS, jsonArray.toString()).apply()
    }

    companion object {
        private const val PREFS_NAME = "devbay_folders"
        private const val KEY_FOLDERS = "folders_json"
        private const val FIELD_ID = "id"
        private const val FIELD_NAME = "name"
        private const val FIELD_APP_KEYS = "app_keys"
    }
}