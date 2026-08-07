package com.devbay.launcher.activity

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.devbay.launcher.databinding.ActivityShizukuPairingBinding

class ShizukuPairingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShizukuPairingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShizukuPairingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.closeButton.setOnClickListener { finish() }
        refreshState()

        binding.openWirelessDebuggingButton.setOnClickListener {
            try {
                startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
            } catch (exception: Exception) {
                Toast.makeText(this, R.string.developer_settings_unavailable, Toast.LENGTH_SHORT).show()
            }
        }

        binding.openShizukuButton.setOnClickListener {
            if (ShizukuPairingLauncher.isShizukuAppInstalled(this)) {
                if (!ShizukuPairingLauncher.openShizukuApp(this)) {
                    Toast.makeText(this, R.string.shizuku_open_failed, Toast.LENGTH_SHORT).show()
                }
            } else {
                ShizukuPairingLauncher.openShizukuOnStore(this)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshState()
    }

    private fun refreshState() {
        val installed = ShizukuPairingLauncher.isShizukuAppInstalled(this)
        binding.shizukuStatusText.text = getString(
            if (installed) R.string.shizuku_status_installed else R.string.shizuku_status_not_installed
        )
        binding.openShizukuButton.text = getString(
            if (installed) R.string.open_shizuku_app else R.string.install_shizuku_app
        )
    }
}