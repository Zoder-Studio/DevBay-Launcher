package com.devbay.launcher.icon

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat
import org.xmlpull.v1.XmlPullParser

class IconPackParser(private val context: Context) {

    fun loadIconMap(iconPackPackage: String): Map<String, String> {
        return try {
            val resources = context.packageManager.getResourcesForApplication(iconPackPackage)
            val xmlResId = resources.getIdentifier("appfilter", "xml", iconPackPackage)
            if (xmlResId == 0) return emptyMap()

            val parser = resources.getXml(xmlResId)
            val map = mutableMapOf<String, String>()
            var eventType = parser.eventType

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.name == "item") {
                    val component = parser.getAttributeValue(null, "component")
                    val drawable = parser.getAttributeValue(null, "drawable")
                    if (component != null && drawable != null) {
                        parseComponentKey(component)?.let { key -> map[key] = drawable }
                    }
                }
                eventType = parser.next()
            }
            map
        } catch (throwable: Throwable) {
            emptyMap()
        }
    }

    fun loadDrawable(iconPackPackage: String, drawableName: String): Drawable? {
        return try {
            val resources = context.packageManager.getResourcesForApplication(iconPackPackage)
            val resId = resources.getIdentifier(drawableName, "drawable", iconPackPackage)
            if (resId == 0) return null
            ResourcesCompat.getDrawable(resources, resId, null)
        } catch (throwable: Throwable) {
            null
        }
    }

    private fun parseComponentKey(component: String): String? {
        val match = COMPONENT_REGEX.find(component) ?: return null
        val packageName = match.groupValues[1]
        var activityName = match.groupValues[2]
        if (activityName.startsWith(".")) {
            activityName = packageName + activityName
        }
        return "$packageName/$activityName"
    }

    companion object {
        private val COMPONENT_REGEX = Regex("ComponentInfo\\{([^,}]+)/([^}]+)\\}")
    }
}