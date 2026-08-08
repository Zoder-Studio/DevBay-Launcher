package com.devbay.launcher

import android.app.Activity
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import android.content.ClipData
import android.content.ClipboardManager
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import androidx.core.view.GestureDetectorCompat
import kotlin.math.abs
import com.devbay.launcher.databinding.ActivityMainBinding
import com.devbay.launcher.settings.SettingsActivity
import com.devbay.launcher.activity.*
import com.devbay.launcher.app.*
import com.devbay.launcher.clipboard.*
import com.devbay.launcher.folder.*
import com.devbay.launcher.gesture.*
import com.devbay.launcher.github.*
import com.devbay.launcher.icon.*
import com.devbay.launcher.launcher.*
import com.devbay.launcher.logcat.*
import com.devbay.launcher.notification.*
import com.devbay.launcher.quicktoggle.*
import com.devbay.launcher.recent.*
import com.devbay.launcher.shizuku.*
import com.devbay.launcher.vault.*
import com.devbay.launcher.widget.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var categoryPreferences: CategoryPreferences
    private lateinit var vaultRepository: VaultRepository
    private lateinit var folderRepository: FolderRepository
    private lateinit var appSectioner: AppSectioner
    private lateinit var launcherAdapter: LauncherAdapter

    private lateinit var appWidgetManager: AppWidgetManager
    private lateinit var appWidgetHost: AppWidgetHost
    private lateinit var widgetRepository: WidgetRepository

    private lateinit var systemToggleRepository: SystemToggleRepository
    private lateinit var notificationAccessPreferences: NotificationAccessPreferences
    private lateinit var quickToggleAdapter: QuickToggleAdapter

    private lateinit var recentAppsRepository: RecentAppsRepository
    private lateinit var recentAppsAdapter: RecentAppsAdapter

    private lateinit var clipboardRepository: ClipboardRepository
    private lateinit var systemClipboardManager: ClipboardManager

    private lateinit var gesturePreferences: GesturePreferences
    private lateinit var gestureDetector: GestureDetectorCompat

    private lateinit var gitHubPreferences: GitHubPreferences
    private lateinit var gitHubHomeAdapter: GitHubHomeAdapter

    private val clipboardChangeListener = ClipboardManager.OnPrimaryClipChangedListener {
        captureCurrentClipboardEntry()
    }

    private var pendingChipAction: QuickToggleChip? = null
    private var allApps: List<AppInfo> = emptyList()
    private var currentQuery: String = ""

    private var pendingWidgetId: Int = -1
    private var pendingProviderInfo: AppWidgetProviderInfo? = null

    private val pickWidgetLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val packageName = result.data?.getStringExtra(WidgetPickerActivity.EXTRA_PROVIDER_PACKAGE)
            val className = result.data?.getStringExtra(WidgetPickerActivity.EXTRA_PROVIDER_CLASS)
            if (packageName != null && className != null) {
                startWidgetBinding(ComponentName(packageName, className))
            }
        }
    }

    private val bindWidgetLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) proceedAfterBind() else cancelPendingWidget()
    }

    private val configureWidgetLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) finalizeWidgetAddition() else cancelPendingWidget()
    }

    private val shizukuPermissionResultListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (requestCode == ShizukuCommandExecutor.REQUEST_CODE) {
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                pendingChipAction?.let { chip -> executeChipCommand(chip) }
            } else {
                Toast.makeText(this, R.string.shizuku_permission_denied, Toast.LENGTH_SHORT).show()
            }
            pendingChipAction = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this) {
            if (currentQuery.isNotEmpty()) binding.searchInput.text?.clear()
        }

        categoryPreferences = CategoryPreferences(applicationContext)
        folderRepository = FolderRepository(applicationContext)
        vaultRepository = VaultRepository(applicationContext)
        appSectioner = AppSectioner(categoryPreferences, folderRepository)
        systemToggleRepository = SystemToggleRepository(applicationContext)

        notificationAccessPreferences = NotificationAccessPreferences(applicationContext)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                NotificationBadgeStore.countsFlow.collect {
                    renderList()
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                AppCacheStore.appsFlow.collect { rawApps ->
                    applyCachedApps(rawApps)
                }
            }
        }

        recentAppsRepository = RecentAppsRepository(applicationContext)
        clipboardRepository = ClipboardRepository(applicationContext)
        systemClipboardManager = getSystemService(ClipboardManager::class.java)
        gitHubPreferences = GitHubPreferences(applicationContext)
        setupGitHubSection()

        appWidgetManager = AppWidgetManager.getInstance(this)
        appWidgetHost = AppWidgetHost(this, WIDGET_HOST_ID)
        widgetRepository = WidgetRepository(applicationContext)

        Shizuku.addRequestPermissionResultListener(shizukuPermissionResultListener)

        setupAppGrid()
        setupSearch()
        setupWidgetControls()
        setupVaultBiometricButton()
        setupQuickToggles()
        setupRecentApps()
        binding.settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        binding.launcherSwitchButton.setOnClickListener { switchDefaultLauncher() }
        gesturePreferences = GesturePreferences(applicationContext)
        setupGestureZone()
        binding.clipboardButton.setOnClickListener {
            startActivity(Intent(this, ClipboardActivity::class.java))
        }
        restoreSavedWidgets()
    }

    override fun onResume() {
        super.onResume()
        refreshFilteredApps()
        updateVaultBiometricButtonVisibility()
        refreshQuickToggleChips()
        maybePromptNotificationAccess()
        refreshGitHubSection()
    }

    override fun onStart() {
        super.onStart()
        appWidgetHost.startListening()
        systemClipboardManager.addPrimaryClipChangedListener(clipboardChangeListener)
    }

    override fun onStop() {
        super.onStop()
        appWidgetHost.stopListening()
        systemClipboardManager.removePrimaryClipChangedListener(clipboardChangeListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        Shizuku.removeRequestPermissionResultListener(shizukuPermissionResultListener)
    }

    // ---------- App grid & search ----------

    private fun setupAppGrid() {
        launcherAdapter = LauncherAdapter(
            items = emptyList(),
            onAppClick = { app -> launchApp(app) },
            onAppLongClick = { app -> openAppOptions(app) },
            onFolderClick = { folder -> openFolder(folder) },
            onFolderLongClick = { folder -> openFolder(folder) },
            onEditPinnedClick = { openPinnedEditor() }
        )

        val layoutManager = GridLayoutManager(this, SPAN_COUNT)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return launcherAdapter.getSpanSize(position, SPAN_COUNT)
            }
        }

        binding.appGrid.layoutManager = layoutManager
        binding.appGrid.adapter = launcherAdapter
        binding.appGrid.setHasFixedSize(true)
    }

    private fun openFolder(folder: AppFolder) {
        val intent = Intent(this, FolderContentsActivity::class.java).apply {
            putExtra(FolderContentsActivity.EXTRA_FOLDER_ID, folder.id)
        }
        startActivity(intent)
    }

    private fun setupSearch() {
        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(text: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                currentQuery = text?.toString().orEmpty()
                if (isVaultUnlockQuery(currentQuery)) {
                    openVault()
                    binding.searchInput.text?.clear()
                    return
                }
                renderList()
            }

            override fun afterTextChanged(text: Editable?) = Unit
        })
    }

    private fun applyCachedApps(rawApps: List<AppInfo>) {
        val hiddenKeys = vaultRepository.getHiddenApps()
        allApps = rawApps.filterNot { it.key in hiddenKeys }
        renderList()
        refreshRecentApps()
    }

    private fun refreshFilteredApps() {
        applyCachedApps(AppCacheStore.getApps())
    }

    private fun renderList() {
        val items = if (currentQuery.isBlank()) buildSectionedItems() else buildSearchItems(currentQuery)
        launcherAdapter.updateItems(items)
        binding.emptyState.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun buildSectionedItems(): List<LauncherListItem> {
        val sections = appSectioner.buildSections(allApps)
        val items = mutableListOf<LauncherListItem>()
        for (section in sections) {
            val totalCount = section.apps.size + section.folders.size
            items.add(LauncherListItem.Header(section.category, totalCount))

            section.folders.forEach { folder ->
                val appsByKey = allApps.associateBy { it.key }
                val previewApps = folder.appKeys.take(4).mapNotNull { appsByKey[it] }
                items.add(LauncherListItem.FolderItem(folder, previewApps))
            }
            items.addAll(section.apps.map { LauncherListItem.AppItem(it) })
        }
        return items
    }

    private fun buildSearchItems(query: String): List<LauncherListItem> {
        return allApps
            .map { app -> app to FuzzyMatcher.score(query, app.label) }
            .filter { (_, score) -> score != FuzzyMatcher.NO_MATCH }
            .sortedWith(
                compareByDescending<Pair<AppInfo, Int>> { it.second }
                    .thenBy { it.first.label.lowercase(Locale.getDefault()) }
            )
            .map { (app, _) -> LauncherListItem.AppItem(app) }
    }

    private fun launchApp(app: AppInfo) {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            component = ComponentName(app.packageName, app.activityName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        }
        try {
            startActivity(intent)
            binding.searchInput.text?.clear()
            recentAppsRepository.recordLaunch(app.key)
            refreshRecentApps()
        } catch (exception: ActivityNotFoundException) {
            Toast.makeText(this, getString(R.string.app_launch_failed, app.label), Toast.LENGTH_SHORT).show()
        }
    }

    private fun openAppOptions(app: AppInfo) {
        val isPinned = categoryPreferences.isPinned(app.key)
        val isTools = categoryPreferences.isTools(app.key)
        val currentFolder = folderRepository.findFolderContaining(app.key)

        val pinLabel = getString(if (isPinned) R.string.action_unpin else R.string.action_pin)
        val toolsLabel = getString(if (isTools) R.string.action_remove_tools else R.string.action_add_tools)
        val folderLabel = if (currentFolder != null) {
            getString(R.string.action_remove_from_folder)
        } else {
            getString(R.string.action_add_to_folder)
        }

        val options = arrayOf(
            pinLabel,
            toolsLabel,
            folderLabel,
            getString(R.string.action_hide_app),
            getString(R.string.action_view_logcat),
            getString(R.string.action_view_crash),
            getString(R.string.action_app_info)
        )

        AlertDialog.Builder(this)
            .setTitle(app.label)
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> {
                        if (isPinned) categoryPreferences.unpinApp(app.key) else categoryPreferences.pinApp(app.key)
                        refreshFilteredApps()
                    }
                    1 -> {
                        if (isTools) categoryPreferences.removeFromTools(app.key) else categoryPreferences.addToTools(app.key)
                        refreshFilteredApps()
                    }
                    2 -> {
                        if (currentFolder != null) {
                            folderRepository.removeAppFromFolder(currentFolder.id, app.key)
                            refreshFilteredApps()
                        } else {
                            showFolderPicker(app)
                        }
                    }
                    3 -> handleHideApp(app)
                    4 -> openLogAccessPicker(app, isCrashMode = false)
                    5 -> openLogAccessPicker(app, isCrashMode = true)
                    6 -> openSystemAppInfo(app)
                }
                dialog.dismiss()
            }
            .show()
    }

    private fun showFolderPicker(app: AppInfo) {
        val existingFolders = folderRepository.getFolders()
        val options = existingFolders.map { it.name } + getString(R.string.create_new_folder)

        AlertDialog.Builder(this)
            .setTitle(R.string.add_to_folder_title)
            .setItems(options.toTypedArray()) { dialog, which ->
                dialog.dismiss()
                if (which == existingFolders.size) {
                    showCreateFolderDialog(app)
                } else {
                    folderRepository.addAppToFolder(existingFolders[which].id, app.key)
                    refreshFilteredApps()
                }
            }
            .show()
    }

    private fun showCreateFolderDialog(app: AppInfo) {
        val input = EditText(this)
        input.hint = getString(R.string.folder_name_hint)

        AlertDialog.Builder(this)
            .setTitle(R.string.create_new_folder)
            .setView(input)
            .setPositiveButton(R.string.action_save) { dialog, _ ->
                val name = input.text.toString().trim()
                if (name.isNotBlank()) {
                    folderRepository.createFolder(name, app.key)
                    refreshFilteredApps()
                }
                dialog.dismiss()
            }
            .setNegativeButton(R.string.action_close, null)
            .show()
    }

    private fun openLogAccessPicker(app: AppInfo, isCrashMode: Boolean) {
        val logFoxInstalled = LogViewerLauncher.isLogFoxInstalled(this)
        val options = if (logFoxInstalled) {
            arrayOf(getString(R.string.log_source_logfox), getString(R.string.log_source_shizuku))
        } else {
            arrayOf(getString(R.string.log_source_logfox_not_installed), getString(R.string.log_source_shizuku))
        }

        AlertDialog.Builder(this)
            .setTitle(if (isCrashMode) R.string.action_view_crash else R.string.action_view_logcat)
            .setItems(options) { dialog, which ->
                dialog.dismiss()
                when (which) {
                    0 -> {
                        if (logFoxInstalled) {
                            LogViewerLauncher.openLogFox(this)
                        } else {
                            LogViewerLauncher.openLogFoxOnPlayStore(this)
                        }
                    }
                    1 -> openShizukuLogViewer(app, isCrashMode)
                }
            }
            .show()
    }

    private fun openShizukuLogViewer(app: AppInfo, isCrashMode: Boolean) {
        val intent = Intent(this, LogViewerDialogActivity::class.java).apply {
            putExtra(LogViewerDialogActivity.EXTRA_PACKAGE_NAME, app.packageName)
            putExtra(LogViewerDialogActivity.EXTRA_APP_LABEL, app.label)
            putExtra(LogViewerDialogActivity.EXTRA_IS_CRASH_MODE, isCrashMode)
        }
        startActivity(intent)
    }

    private fun openPinnedEditor() {
        val pinnedApps = allApps.filter { categoryPreferences.isPinned(it.key) }
        if (pinnedApps.isEmpty()) {
            Toast.makeText(this, R.string.no_pinned_apps, Toast.LENGTH_SHORT).show()
            return
        }
        val labels = pinnedApps.map { it.label }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle(R.string.edit_pinned_title)
            .setItems(labels) { dialog, which ->
                categoryPreferences.unpinApp(pinnedApps[which].key)
                dialog.dismiss()
                refreshFilteredApps()
            }
            .setNegativeButton(R.string.action_close, null)
            .show()
    }

    private fun openSystemAppInfo(app: AppInfo) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", app.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    // ---------- Vault ----------

    private fun isVaultUnlockQuery(query: String): Boolean {
        return query.isNotBlank() && vaultRepository.isVaultConfigured() && vaultRepository.verifyPassword(query)
    }

    private fun openVault() {
        startActivity(Intent(this, VaultActivity::class.java))
    }

    private fun handleHideApp(app: AppInfo) {
        if (!vaultRepository.isVaultConfigured()) {
            showSetupVaultPasswordDialog {
                vaultRepository.hideApp(app.key)
                refreshFilteredApps()
                Toast.makeText(this, getString(R.string.app_hidden, app.label), Toast.LENGTH_SHORT).show()
            }
        } else {
            vaultRepository.hideApp(app.key)
            refreshFilteredApps()
            Toast.makeText(this, getString(R.string.app_hidden, app.label), Toast.LENGTH_SHORT).show()
        }
    }

    private fun showSetupVaultPasswordDialog(onSuccess: () -> Unit) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_setup_vault_password, null)
        val passwordInput = dialogView.findViewById<EditText>(R.id.setupPasswordInput)
        val confirmInput = dialogView.findViewById<EditText>(R.id.setupConfirmInput)
        val biometricCheckbox = dialogView.findViewById<CheckBox>(R.id.useBiometricCheckbox)

        val biometricAvailable = BiometricAuthenticator.isAvailable(this)
        biometricCheckbox.isEnabled = biometricAvailable
        if (!biometricAvailable) biometricCheckbox.text = getString(R.string.biometric_unavailable)

        AlertDialog.Builder(this)
            .setTitle(R.string.setup_vault_title)
            .setMessage(R.string.setup_vault_message)
            .setView(dialogView)
            .setPositiveButton(R.string.action_save) { dialog, _ ->
                val password = passwordInput.text.toString()
                val confirm = confirmInput.text.toString()
                when {
                    password.length < VaultRepository.MIN_PASSWORD_LENGTH ->
                        Toast.makeText(this, R.string.error_password_too_short, Toast.LENGTH_SHORT).show()
                    password != confirm ->
                        Toast.makeText(this, R.string.error_password_mismatch, Toast.LENGTH_SHORT).show()
                    else -> {
                        vaultRepository.setVaultPassword(password)
                        vaultRepository.setBiometricEnabled(biometricCheckbox.isChecked && biometricAvailable)
                        updateVaultBiometricButtonVisibility()
                        onSuccess()
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton(R.string.action_close, null)
            .show()
    }

    private fun setupVaultBiometricButton() {
        binding.vaultBiometricButton.setOnClickListener {
            BiometricAuthenticator.authenticate(
                activity = this,
                title = getString(R.string.biometric_prompt_title),
                subtitle = getString(R.string.biometric_prompt_subtitle),
                onSuccess = { openVault() },
                onError = { message -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show() }
            )
        }
        updateVaultBiometricButtonVisibility()
    }

    private fun updateVaultBiometricButtonVisibility() {
        binding.vaultBiometricButton.visibility =
            if (vaultRepository.isVaultConfigured() && vaultRepository.isBiometricEnabled()) View.VISIBLE else View.GONE
    }

    // ---------- Widgets ----------

    private fun setupWidgetControls() {
        binding.addWidgetButton.setOnClickListener {
            pickWidgetLauncher.launch(Intent(this, WidgetPickerActivity::class.java))
        }
    }

    private fun restoreSavedWidgets() {
        binding.widgetStrip.removeAllViews()
        for (widgetId in widgetRepository.getWidgetIds()) {
            val providerInfo = appWidgetManager.getAppWidgetInfo(widgetId)
            if (providerInfo != null) {
                addWidgetView(widgetId, providerInfo)
            } else {
                widgetRepository.removeWidgetId(widgetId)
                appWidgetHost.deleteAppWidgetId(widgetId)
            }
        }
    }

    private fun startWidgetBinding(provider: ComponentName) {
        val widgetId = appWidgetHost.allocateAppWidgetId()
        pendingWidgetId = widgetId

        val allowed = appWidgetManager.bindAppWidgetIdIfAllowed(widgetId, provider)
        if (allowed) {
            proceedAfterBind()
        } else {
            val bindIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, provider)
            }
            bindWidgetLauncher.launch(bindIntent)
        }
    }

    private fun proceedAfterBind() {
        val widgetId = pendingWidgetId
        if (widgetId == -1) return

        val providerInfo = appWidgetManager.getAppWidgetInfo(widgetId)
        if (providerInfo == null) {
            cancelPendingWidget()
            return
        }
        pendingProviderInfo = providerInfo

        if (providerInfo.configure != null) {
            val configureIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
                component = providerInfo.configure
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            }
            try {
                configureWidgetLauncher.launch(configureIntent)
            } catch (exception: ActivityNotFoundException) {
                finalizeWidgetAddition()
            }
        } else {
            finalizeWidgetAddition()
        }
    }

    private fun finalizeWidgetAddition() {
        val widgetId = pendingWidgetId
        val providerInfo = pendingProviderInfo
        if (widgetId == -1 || providerInfo == null) return

        widgetRepository.addWidgetId(widgetId)
        addWidgetView(widgetId, providerInfo)

        pendingWidgetId = -1
        pendingProviderInfo = null
    }

    private fun cancelPendingWidget() {
        if (pendingWidgetId != -1) appWidgetHost.deleteAppWidgetId(pendingWidgetId)
        pendingWidgetId = -1
        pendingProviderInfo = null
        Toast.makeText(this, R.string.widget_add_cancelled, Toast.LENGTH_SHORT).show()
    }

    private fun addWidgetView(widgetId: Int, providerInfo: AppWidgetProviderInfo) {
        val hostView = appWidgetHost.createView(applicationContext, widgetId, providerInfo)
        hostView.setAppWidget(widgetId, providerInfo)

        val density = resources.displayMetrics.density
        val cardWidthPx = (WIDGET_CARD_WIDTH_DP * density).toInt()
        val cardHeightPx = (WIDGET_CARD_HEIGHT_DP * density).toInt()

        val wrapper = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(cardWidthPx, cardHeightPx).apply {
                marginEnd = (8 * density).toInt()
            }
            setBackgroundResource(R.drawable.bg_widget_card)
            clipToOutline = true
            setOnLongClickListener {
                confirmRemoveWidget(widgetId, this)
                true
            }
            addView(hostView)
        }

        binding.widgetStrip.addView(wrapper)
    }

    private fun confirmRemoveWidget(widgetId: Int, view: View) {
        AlertDialog.Builder(this)
            .setTitle(R.string.remove_widget_title)
            .setPositiveButton(R.string.action_remove) { dialog, _ ->
                appWidgetHost.deleteAppWidgetId(widgetId)
                widgetRepository.removeWidgetId(widgetId)
                binding.widgetStrip.removeView(view)
                dialog.dismiss()
            }
            .setNegativeButton(R.string.action_close, null)
            .show()
    }

    // ---------- Quick toggle chips (Shizuku) ----------

    private fun setupQuickToggles() {
        quickToggleAdapter = QuickToggleAdapter(emptyList()) { chip -> onChipClicked(chip) }
        binding.quickToggleRow.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.quickToggleRow.adapter = quickToggleAdapter
        refreshQuickToggleChips()
    }

    private fun refreshQuickToggleChips() {
        quickToggleAdapter.updateChips(buildQuickToggleChips())
    }

    private fun buildQuickToggleChips(): List<QuickToggleChip> {
        return listOf(
            QuickToggleChip(
                type = QuickToggleType.DEVELOPER_SETTINGS,
                label = getString(R.string.chip_developer_settings),
                iconRes = R.drawable.ic_developer_settings,
                isToggle = false,
                isActive = systemToggleRepository.isDeveloperSettingsEnabled()
            ),
            QuickToggleChip(
                type = QuickToggleType.KILL_ACTIVITIES,
                label = getString(R.string.chip_kill_activities),
                iconRes = R.drawable.ic_kill_activities,
                isToggle = false,
                isActive = false
            ),
            QuickToggleChip(
                type = QuickToggleType.SLOW_ANIMATIONS,
                label = getString(R.string.chip_slow_animations),
                iconRes = R.drawable.ic_slow_animations,
                isToggle = true,
                isActive = systemToggleRepository.isSlowAnimationsEnabled()
            ),
            QuickToggleChip(
                type = QuickToggleType.BIG_FONTS,
                label = getString(R.string.chip_big_fonts),
                iconRes = R.drawable.ic_big_fonts,
                isToggle = true,
                isActive = systemToggleRepository.isBigFontEnabled()
            ),
            QuickToggleChip(
                type = QuickToggleType.WIRELESS_ADB,
                label = getString(R.string.chip_wireless_adb),
                iconRes = R.drawable.ic_wireless_adb,
                isToggle = true,
                isActive = systemToggleRepository.isWirelessAdbEnabled()
            )
        )
    }

    private fun onChipClicked(chip: QuickToggleChip) {
        if (chip.type == QuickToggleType.DEVELOPER_SETTINGS) {
            try {
                startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
            } catch (exception: ActivityNotFoundException) {
                Toast.makeText(this, R.string.developer_settings_unavailable, Toast.LENGTH_SHORT).show()
            }
            return
        }
        runShizukuAction(chip)
    }

    private fun runShizukuAction(chip: QuickToggleChip) {
        if (!ShizukuCommandExecutor.isShizukuAvailable()) {
            Toast.makeText(this, R.string.shizuku_not_running, Toast.LENGTH_LONG).show()
            return
        }
        if (!ShizukuCommandExecutor.isPermissionGranted()) {
            pendingChipAction = chip
            ShizukuCommandExecutor.requestPermission()
            return
        }
        executeChipCommand(chip)
    }

    private fun executeChipCommand(chip: QuickToggleChip) {
        val command = when (chip.type) {
            QuickToggleType.KILL_ACTIVITIES -> systemToggleRepository.killAllBackgroundAppsCommand()
            QuickToggleType.SLOW_ANIMATIONS ->
                systemToggleRepository.setSlowAnimationsCommand(!systemToggleRepository.isSlowAnimationsEnabled())
            QuickToggleType.BIG_FONTS ->
                systemToggleRepository.setBigFontCommand(!systemToggleRepository.isBigFontEnabled())
            QuickToggleType.WIRELESS_ADB ->
                systemToggleRepository.setWirelessAdbCommand(!systemToggleRepository.isWirelessAdbEnabled())
            QuickToggleType.DEVELOPER_SETTINGS -> return
        }

        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) { ShizukuCommandExecutor.execute(command) }
            if (!result.success) {
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.shizuku_command_failed, result.output),
                    Toast.LENGTH_SHORT
                ).show()
            } else if (chip.type == QuickToggleType.KILL_ACTIVITIES) {
                Toast.makeText(this@MainActivity, R.string.kill_activities_done, Toast.LENGTH_SHORT).show()
            }
            refreshQuickToggleChips()
        }
    }

    private fun isNotificationAccessGranted(): Boolean {
        val enabledListeners = androidx.core.app.NotificationManagerCompat.getEnabledListenerPackages(this)
        return enabledListeners.contains(packageName)
    }

    private fun maybePromptNotificationAccess() {
        if (isNotificationAccessGranted()) return
        if (notificationAccessPreferences.hasAskedBefore()) return
        notificationAccessPreferences.markAsked()

        AlertDialog.Builder(this)
            .setTitle(R.string.notification_access_title)
            .setMessage(R.string.notification_access_message)
            .setPositiveButton(R.string.action_enable) { dialog, _ ->
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                dialog.dismiss()
            }
            .setNegativeButton(R.string.action_close, null)
            .show()
    }

    private fun setupRecentApps() {
        recentAppsAdapter = RecentAppsAdapter(
            apps = emptyList(),
            onAppClick = { app -> launchApp(app) },
            onAppLongClick = { app ->
                recentAppsRepository.removeKey(app.key)
                refreshRecentApps()
            }
        )
        binding.recentAppsRow.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recentAppsRow.adapter = recentAppsAdapter
        binding.clearRecentButton.setOnClickListener {
            recentAppsRepository.clearAll()
            refreshRecentApps()
        }
    }

    private fun refreshRecentApps() {
        val keys = recentAppsRepository.getRecentKeys()
        val appsByKey = allApps.associateBy { it.key }
        val recentApps = keys.mapNotNull { appsByKey[it] }
        recentAppsAdapter.updateApps(recentApps)
        binding.recentAppsSection.visibility = if (recentApps.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun switchDefaultLauncher() {
        try {
            packageManager.clearPackagePreferredActivities(packageName)
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(homeIntent)
        } catch (exception: Exception) {
            try {
                startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
            } catch (fallbackException: ActivityNotFoundException) {
                Toast.makeText(this, R.string.switch_launcher_unavailable, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun captureCurrentClipboardEntry() {
        try {
            val clipData = systemClipboardManager.primaryClip ?: return
            if (clipData.itemCount == 0) return
            val text = clipData.getItemAt(0).coerceToText(this)?.toString() ?: return
            clipboardRepository.addEntry(text)
        } catch (throwable: Throwable) {
            // Clipboard read denied because the launcher lost focus; safely ignore.
        }
    }

    private fun setupGestureZone() {
        gestureDetector = GestureDetectorCompat(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                executeGestureAction(gesturePreferences.getAction(GestureDirection.DOUBLE_TAP))
                return true
            }

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 == null) return false
                val deltaX = e2.x - e1.x
                val deltaY = e2.y - e1.y
                if (abs(deltaX) > abs(deltaY) &&
                    abs(deltaX) > SWIPE_DISTANCE_THRESHOLD &&
                    abs(velocityX) > SWIPE_VELOCITY_THRESHOLD
                ) {
                    val direction = if (deltaX > 0) GestureDirection.SWIPE_RIGHT else GestureDirection.SWIPE_LEFT
                    executeGestureAction(gesturePreferences.getAction(direction))
                    return true
                }
                return false
            }
        })

        binding.gestureZone.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            false
        }
    }

    private fun executeGestureAction(action: GestureAction) {
        when (action) {
            GestureAction.None -> Unit
            GestureAction.OpenSearch -> {
                binding.searchInput.requestFocus()
                val inputMethodManager = getSystemService(InputMethodManager::class.java)
                inputMethodManager?.showSoftInput(binding.searchInput, InputMethodManager.SHOW_IMPLICIT)
            }
            GestureAction.OpenSettings -> startActivity(Intent(this, SettingsActivity::class.java))
            GestureAction.OpenClipboard -> startActivity(Intent(this, ClipboardActivity::class.java))
            GestureAction.OpenNotificationPanel -> expandStatusBarPanel(METHOD_EXPAND_NOTIFICATIONS)
            GestureAction.OpenQuickSettingsPanel -> expandStatusBarPanel(METHOD_EXPAND_QUICK_SETTINGS)
            is GestureAction.LaunchApp -> {
                val app = allApps.firstOrNull {
                    it.packageName == action.packageName && it.activityName == action.activityName
                }
                if (app != null) {
                    launchApp(app)
                } else {
                    Toast.makeText(this, R.string.gesture_app_not_found, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun expandStatusBarPanel(methodName: String) {
        try {
            val statusBarService = getSystemService("statusbar")
            val statusBarManagerClass = Class.forName("android.app.StatusBarManager")
            val method = statusBarManagerClass.getMethod(methodName)
            method.invoke(statusBarService)
        } catch (throwable: Throwable) {
            Toast.makeText(this, R.string.gesture_panel_unavailable, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupGitHubSection() {
        gitHubHomeAdapter = GitHubHomeAdapter(emptyList()) { repo ->
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/${repo.fullName}")))
        }
        binding.githubRepoRow.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.githubRepoRow.adapter = gitHubHomeAdapter
        refreshGitHubSection()
    }

    private fun refreshGitHubSection() {
        val repos = gitHubPreferences.getWatchedRepos()
        gitHubHomeAdapter.updateRepos(repos)
        binding.githubSection.visibility = if (repos.isEmpty()) View.GONE else View.VISIBLE
    }

    companion object {
        private const val SPAN_COUNT = 4
        private const val WIDGET_HOST_ID = 1024
        private const val WIDGET_CARD_WIDTH_DP = 260
        private const val WIDGET_CARD_HEIGHT_DP = 160
        private const val SWIPE_DISTANCE_THRESHOLD = 100
        private const val SWIPE_VELOCITY_THRESHOLD = 150
        private const val METHOD_EXPAND_NOTIFICATIONS = "expandNotificationsPanel"
        private const val METHOD_EXPAND_QUICK_SETTINGS = "expandSettingsPanel"
    }
}