package com.devbay.launcher.app

import android.content.Context
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.devbay.launcher.R
import com.google.android.material.bottomsheet.BottomSheetDialog

object ActionSheetHelper {

    fun show(context: Context, title: String, actions: List<Pair<String, () -> Unit>>) {
        val dialog = BottomSheetDialog(context)
        val root = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_app_actions, null)
        val titleView = root.findViewById<TextView>(R.id.sheetTitle)
        val actionsContainer = root.findViewById<LinearLayout>(R.id.sheetActionsContainer)

        titleView.text = title

        actions.forEach { (label, onClick) ->
            val itemView = LayoutInflater.from(context)
                .inflate(R.layout.item_bottom_sheet_action, actionsContainer, false)
            (itemView as TextView).text = label
            itemView.setOnClickListener {
                dialog.dismiss()
                onClick()
            }
            actionsContainer.addView(itemView)
        }

        dialog.setContentView(root)
        dialog.show()
    }
}