package com.devbay.launcher

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.devbay.launcher.databinding.ActivityWidgetPickerBinding

class WidgetPickerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWidgetPickerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWidgetPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val appWidgetManager = AppWidgetManager.getInstance(this)
        val providers = appWidgetManager.installedProviders
            .map { info -> WidgetProviderEntry(info, info.loadLabel(packageManager)) }
            .sortedBy { it.label.lowercase() }

        val adapter = WidgetPickerAdapter(providers) { entry ->
            val resultIntent = Intent().apply {
                putExtra(EXTRA_PROVIDER_PACKAGE, entry.providerInfo.provider.packageName)
                putExtra(EXTRA_PROVIDER_CLASS, entry.providerInfo.provider.className)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }

        binding.providerList.layoutManager = LinearLayoutManager(this)
        binding.providerList.adapter = adapter
        binding.closeButton.setOnClickListener { finish() }

        binding.emptyProvidersState.visibility = if (providers.isEmpty()) View.VISIBLE else View.GONE
    }

    companion object {
        const val EXTRA_PROVIDER_PACKAGE = "extra_provider_package"
        const val EXTRA_PROVIDER_CLASS = "extra_provider_class"
    }
}