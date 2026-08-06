package com.devbay.launcher.activity

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.devbay.launcher.databinding.ActivityVaultBinding

class VaultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVaultBinding
    private lateinit var vaultRepository: VaultRepository
    private lateinit var vaultAppAdapter: VaultAppAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVaultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        vaultRepository = VaultRepository(applicationContext)

        vaultAppAdapter = VaultAppAdapter(
            apps = emptyList(),
            onAppClick = { app -> launchApp(app) },
            onAppLongClick = { app -> openVaultAppOptions(app) }
        )
        binding.vaultGrid.layoutManager = GridLayoutManager(this, SPAN_COUNT)
        binding.vaultGrid.adapter = vaultAppAdapter

        binding.closeButton.setOnClickListener { finish() }
        binding.changePasswordButton.setOnClickListener { showChangePasswordDialog() }

        loadHiddenApps()
    }

    private fun loadHiddenApps() {
        val hiddenKeys = vaultRepository.getHiddenApps()
        val hiddenApps = AppCacheStore.getApps().filter { it.key in hiddenKeys }
        vaultAppAdapter.updateApps(hiddenApps)
        binding.emptyVaultState.visibility = if (hiddenApps.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun launchApp(app: AppInfo) {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            component = ComponentName(app.packageName, app.activityName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        }
        try {
            startActivity(intent)
        } catch (exception: ActivityNotFoundException) {
            Toast.makeText(this, getString(R.string.app_launch_failed, app.label), Toast.LENGTH_SHORT).show()
        }
    }

    private fun openVaultAppOptions(app: AppInfo) {
        val options = arrayOf(
            getString(R.string.action_unhide),
            getString(R.string.action_app_info)
        )
        AlertDialog.Builder(this)
            .setTitle(app.label)
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> {
                        vaultRepository.unhideApp(app.key)
                        loadHiddenApps()
                    }
                    1 -> openSystemAppInfo(app)
                }
                dialog.dismiss()
            }
            .show()
    }

    private fun openSystemAppInfo(app: AppInfo) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", app.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    private fun showChangePasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_vault_password, null)
        val currentPasswordInput = dialogView.findViewById<EditText>(R.id.currentPasswordInput)
        val newPasswordInput = dialogView.findViewById<EditText>(R.id.newPasswordInput)
        val confirmPasswordInput = dialogView.findViewById<EditText>(R.id.confirmPasswordInput)

        AlertDialog.Builder(this)
            .setTitle(R.string.change_vault_password_title)
            .setView(dialogView)
            .setPositiveButton(R.string.action_save) { dialog, _ ->
                val currentPassword = currentPasswordInput.text.toString()
                val newPassword = newPasswordInput.text.toString()
                val confirmPassword = confirmPasswordInput.text.toString()

                when {
                    !vaultRepository.verifyPassword(currentPassword) ->
                        Toast.makeText(this, R.string.error_wrong_password, Toast.LENGTH_SHORT).show()
                    newPassword.length < VaultRepository.MIN_PASSWORD_LENGTH ->
                        Toast.makeText(this, R.string.error_password_too_short, Toast.LENGTH_SHORT).show()
                    newPassword != confirmPassword ->
                        Toast.makeText(this, R.string.error_password_mismatch, Toast.LENGTH_SHORT).show()
                    else -> {
                        vaultRepository.setVaultPassword(newPassword)
                        Toast.makeText(this, R.string.vault_password_updated, Toast.LENGTH_SHORT).show()
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton(R.string.action_close, null)
            .show()
    }

    companion object {
        private const val SPAN_COUNT = 4
    }
}