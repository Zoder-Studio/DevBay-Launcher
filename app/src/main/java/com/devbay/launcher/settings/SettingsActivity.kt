package com.devbay.launcher.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.devbay.launcher.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var themePreferences: ThemePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        themePreferences = ThemePreferences(applicationContext)

        binding.closeButton.setOnClickListener { finish() }

        bindDeviceInfo()
        bindThemeOptions()
        bindGithubLink()
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