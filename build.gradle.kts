plugins {
    alias(libs.plugins.android.application) apply false
    // Si la línea de abajo da error, asegúrate de que el nombre coincida con tu archivo libs.versions.toml
    alias(libs.plugins.jetbrains.kotlin.android) apply false
}