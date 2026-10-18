package nl.mlhk.tvlauncher.model

import android.graphics.drawable.Drawable

data class AppItem(
    val packageName: String,
    val activityName: String,
    val label: String,
    val icon: Drawable?,
    val isKpn: Boolean = false,
    val isPlex: Boolean = false
)
