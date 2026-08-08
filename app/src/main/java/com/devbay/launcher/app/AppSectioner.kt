package com.devbay.launcher.app

import com.devbay.launcher.folder.*

data class AppSection(
    val category: AppCategory,
    val apps: List<AppInfo>,
    val folders: List<AppFolder> = emptyList()
)

class AppSectioner(
    private val categoryPreferences: CategoryPreferences,
    private val folderRepository: FolderRepository
) {

    fun buildSections(apps: List<AppInfo>): List<AppSection> {
        val appsByKey = apps.associateBy { it.key }
        val folderedKeys = folderRepository.getFolders().flatMap { it.appKeys }.toSet()

        val pinnedKeys = categoryPreferences.getPinnedPackages().filter { appsByKey.containsKey(it) }
        val toolsKeys = categoryPreferences.getToolsPackages()
            .filter { appsByKey.containsKey(it) && it !in pinnedKeys }

        val pinnedApps = pinnedKeys.mapNotNull { appsByKey[it] }
        val toolsApps = toolsKeys.mapNotNull { appsByKey[it] }

        val assignedKeys = (pinnedKeys + toolsKeys).toSet()

        val debugApps = apps.filter { it.isDebuggable && it.key !in assignedKeys && it.key !in folderedKeys }
        val otherApps = apps.filter {
            it.key !in assignedKeys && !it.isDebuggable && it.key !in folderedKeys
        }
        val otherFolders = folderRepository.getFolders()

        val sections = mutableListOf<AppSection>()
        if (debugApps.isNotEmpty()) sections.add(AppSection(AppCategory.DEBUG, debugApps))
        if (pinnedApps.isNotEmpty()) sections.add(AppSection(AppCategory.PINNED, pinnedApps))
        if (toolsApps.isNotEmpty()) sections.add(AppSection(AppCategory.TOOLS, toolsApps))
        sections.add(AppSection(AppCategory.OTHER, otherApps, otherFolders))

        return sections
    }
}