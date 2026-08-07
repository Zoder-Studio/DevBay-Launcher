package com.devbay.launcher.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.devbay.launcher.databinding.ActivityLogViewerBinding
import kotlinx.coroutines.launch

class LogViewerDialogActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLogViewerBinding
    private val dumpsysLogViewer = DumpsysLogViewer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME)
        val isCrashMode = intent.getBooleanExtra(EXTRA_IS_CRASH_MODE, false)
        val label = intent.getStringExtra(EXTRA_APP_LABEL) ?: packageName.orEmpty()

        binding.logViewerTitle.text = if (isCrashMode) {
            getString(R.string.log_viewer_crash_title, label)
        } else {
            getString(R.string.log_viewer_logcat_title, label)
        }
        binding.closeButton.setOnClickListener { finish() }

        if (packageName == null) {
            binding.logViewerContent.text = getString(R.string.log_viewer_no_package)
            return
        }

        lifecycleScope.launch {
            binding.logViewerContent.text = if (isCrashMode) {
                dumpsysLogViewer.fetchRecentCrashLog(packageName)
            } else {
                dumpsysLogViewer.fetchRecentLogcat(packageName)
            }
        }
    }

    companion object {
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
        const val EXTRA_APP_LABEL = "extra_app_label"
        const val EXTRA_IS_CRASH_MODE = "extra_is_crash_mode"
    }
}