// Top-level build file. Plugin versions chosen to match a recent stable
// Compose BOM as of early/mid 2026 -- bump in Android Studio's "Upgrade
// Assistant" if it flags newer ones by the time you build this.
plugins {
    id("com.android.application") version "8.6.0" apply false
    id("org.jetbrains.kotlin.android") version "2.0.20" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.20" apply false
}
