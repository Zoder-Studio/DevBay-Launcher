package com.devbay.launcher.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.devbay.launcher.databinding.ActivityIconPackPickerBinding
import kotlinx.coroutines.launch

class IconPackPickerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIconPackPickerBinding
    private lateinit var iconPackRepository: IconPackRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIconPackPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        iconPackRepository = IconPackRepository(applicationContext)
        binding.closeButton.setOnClickListener { finish() }

        loadIconPacks()
    }

    private fun loadIconPacks() {
        val selected = iconPackRepository.getSelectedIconPack()
        val installedPacks = iconPackRepository.getInstalledIconPacks()

        val items = mutableListOf(
            IconPackPickerItem(packageName = null, label = getString(R.string.icon_pack_default), isSelected = selected == null)
        )
        items.addAll(
            installedPacks.map { pack ->
                IconPackPickerItem(pack.packageName, pack.label, isSelected = pack.packageName == selected)
            }
        )

        binding.iconPackList.layoutManager = LinearLayoutManager(this)
        binding.iconPackList.adapter = IconPackPickerAdapter(items) { item -> selectIconPack(item.packageName) }

        binding.emptyIconPackState.visibility =
            if (installedPacks.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun selectIconPack(packageName: String?) {
        iconPackRepository.setSelectedIconPack(packageName)
        lifecycleScope.launch {
            AppCacheRefresher.refresh(applicationContext)
            finish()
        }
    }
}