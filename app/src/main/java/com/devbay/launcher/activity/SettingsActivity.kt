package com.devbay.launcher.activity

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.devbay.launcher.R
import com.devbay.launcher.databinding.*
import com.devbay.launcher.lock.*
import com.devbay.launcher.theme.*
import com.devbay.launcher.settings.*
import com.devbay.launcher.app.HomeLayoutPreferences
import com.devbay.launcher.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var lockScreenPreferences: LockScreenPreferences
    private lateinit var themePreferences: ThemePreferences
    private lateinit var homeLayoutPreferences: HomeLayoutPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        themePreferences = ThemePreferences(applicationContext)

        lockScreenPreferences = LockScreenPreferences(applicationContext)
        binding.lockScreenSwitch.isChecked = lockScreenPreferences.isEnabled()
        binding.lockScreenSwitch.setOnCheckedChangeListener { _, isChecked ->
            lockScreenPreferences.setEnabled(isChecked)
        }

        binding.closeButton.setOnClickListener { finish() }

        bindDeviceInfo()
        bindThemeOptions()
        bindGithubLink()
        homeLayoutPreferences = HomeLayoutPreferences(applicationContext)
        when (homeLayoutPreferences.getMode()) {
            HomeLayoutMode.FULL -> binding.homeLayoutRadioGroup.check(R.id.homeLayoutFullOption)
            HomeLayoutMode.MINIMAL -> binding.homeLayoutRadioGroup.check(R.id.homeLayoutMinimalOption)
        }
        binding.homeLayoutRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val mode = if (checkedId == R.id.homeLayoutMinimalOption) HomeLayoutMode.MINIMAL else HomeLayoutMode.FULL
            homeLayoutPreferences.setMode(mode)
        }
        binding.shortcutsRow.setOnClickListener {
            startActivity(Intent(this, SettingsShortcutsActivity::class.java))
        }
        binding.gestureRow.setOnClickListener {
            startActivity(Intent(this, GestureSettingsActivity::class.java))
        }
        binding.iconPackRow.setOnClickListener {
            startActivity(Intent(this, IconPackPickerActivity::class.java))
        }
        binding.shizukuPairingRow.setOnClickListener {
            startActivity(Intent(this, ShizukuPairingActivity::class.java))
        }
        binding.githubMonitorRow.setOnClickListener {
            startActivity(Intent(this, GitHubMonitorActivity::class.java))
        }
    }

    private fun bindDeviceInfo() {
        binding.deviceModelText.text = getString(
            R.string.settings_device_model_format,
            Build.MANUFACTURER,
            Build.MODEL
        )
        binding.deviceSdkText.text = getString(R.string.settings_sdk_format, Build.VERSION.SDK_INT)
        binding.deviceAndroidText.text = getString(R.string.settings_android_format, Build.VERSION.RELEASE)
    }

    private fun bindThemeOptions() {
        when (themePreferences.getThemeOption()) {
            ThemeOption.LIGHT -> binding.themeRadioGroup.check(R.id.themeLightOption)
            ThemeOption.DARK -> binding.themeRadioGroup.check(R.id.themeDarkOption)
            ThemeOption.SYSTEM -> binding.themeRadioGroup.check(R.id.themeSystemOption)
        }

        binding.themeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedOption = when (checkedId) {
                R.id.themeLightOption -> ThemeOption.LIGHT
                R.id.themeDarkOption -> ThemeOption.DARK
                else -> ThemeOption.SYSTEM
            }
            themePreferences.setThemeOption(selectedOption)
            AppCompatDelegate.setDefaultNightMode(themePreferences.getNightMode())
        }
    }

    private fun bindGithubLink() {
        binding.githubRow.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URL)))
        }
    }

    companion object {
        private const val GITHUB_URL = "https://github.com/Zoder-Studio/DevBay-Launcher"
    }
}