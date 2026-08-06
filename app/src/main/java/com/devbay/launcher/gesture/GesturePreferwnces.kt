package com.devbay.launcher.gesture

import android.content.Context

class GesturePreferences(context: Context) {

    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getAction(direction: GestureDirection): GestureAction {
        return GestureActionCodec.decode(preferences.getString(direction.name, null))
    }

    fun setAction(direction: GestureDirection, action: GestureAction) {
        preferences.edit().putString(direction.name, GestureActionCodec.encode(action)).apply()
    }

    companion object {
        private const val PREFS_NAME = "devbay_gestures"
    }
}