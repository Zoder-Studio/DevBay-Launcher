package com.devbay.launcher

import android.app.Activity
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
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
import androidx.recyclerview.widget.GridLayoutManager
import com.devbay.launcher.databinding.ActivityMainBinding
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appRepository: AppRepository
    private lateinit var categoryPreferences: CategoryPreferences
    private lateinit var vaultRepository: VaultRepository
    private lateinit var appSectioner: AppSectioner
    private lateinit var launcherAdapter: LauncherAdapter

    private lateinit var appWidgetManager: AppWidgetManager
    private lateinit var appWidgetHost: AppWidgetHost
    private lateinit var widgetRepository: WidgetRepository

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this) {
            if (currentQuery.isNotEmpty()) binding.searchInput.text?.clear()
        }

        appRepository = AppRepository(applicationContext)
        categoryPreferences = CategoryPreferences(applicationContext)
        vaultRepository = VaultRepository(applicationContext)
        appSectioner = AppSectioner(categoryPreferences)

        appWidgetManager = AppWidgetManager.getInstance(this)
        appWidgetHost = AppWidgetHost(this, WIDGET_HOST_ID)
        widgetRepository = WidgetRepository(applicationContext)

        setupAppGrid()
        setupSearch()
        setupWidgetControls()
        setupVaultBiometricButton()
        restoreSavedWidgets()
        loadApps()
    }

    override fun onResume() {
        super.onResume()
        loadApps()
        updateVaultBiometricButtonVisibility()
    }

    override fun onStart() {
        super.onStart()
        appWidgetHost.startListening()
    }

    override fun onStop() {
        super.onStop()
        appWidgetHost.stopListening()
    }

    // ---------- App grid & search ----------

    private fun setupAppGrid() {
        launcherAdapter = LauncherAdapter(
            items = emptyList(),
            onAppClick = { app -> launchApp(app) },
            onAppLongClick = { app -> openAppOptions(app) },
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

    private fun loadApps() {
        val hiddenKeys = vaultRepository.getHiddenApps()
        allApps = appRepository.loadInstalledApps().filterNot { it.key in hiddenKeys }
        renderList()
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
            items.add(LauncherListItem.Header(section.category, section.apps.size))
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
        } catch (exception: ActivityNotFoundException) {
            Toast.makeText(this, getString(R.string.app_launch_failed, app.label), Toast.LENGTH_SHORT).show()
        }
    }

    private fun openAppOptions(app: AppInfo) {
        val isPinned = categoryPreferences.isPinned(app.key)
        val isTools = categoryPreferences.isTools(app.key)

        val pinLabel = getString(if (isPinned) R.string.action_unpin else R.string.action_pin)
        val toolsLabel = getString(if (isTools) R.string.action_remove_tools else R.string.action_add_tools)
        val options = arrayOf(
            pinLabel,
            toolsLabel,
            getString(R.string.action_hide_app),
            getString(R.string.action_app_info)
        )

        AlertDialog.Builder(this)
            .setTitle(app.label)
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> {
                        if (isPinned) categoryPreferences.unpinApp(app.key) else categoryPreferences.pinApp(app.key)
                        loadApps()
                    }
                    1 -> {
                        if (isTools) categoryPreferences.removeFromTools(app.key) else categoryPreferences.addToTools(app.key)
                        loadApps()
                    }
                    2 -> handleHideApp(app)
                    3 -> openSystemAppInfo(app)
                }
                dialog.dismiss()
            }
            .show()
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
                loadApps()
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
                loadApps()
                Toast.makeText(this, getString(R.string.app_hidden, app.label), Toast.LENGTH_SHORT).show()
            }
        } else {
            vaultRepository.hideApp(app.key)
            loadApps()
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

    companion object {
        private const val SPAN_COUNT = 4
        private const val WIDGET_HOST_ID = 1024
        private const val WIDGET_CARD_WIDTH_DP = 260
        private const val WIDGET_CARD_HEIGHT_DP = 160
    }
}