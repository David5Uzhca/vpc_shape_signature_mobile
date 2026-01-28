pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ShapeSignatureApp"
include(":app")
include(":opencv") // El nombre del módulo para el SDK
project(":opencv").projectDir = file("OpenCV-android-sdk/sdk") // Ruta hacia la carpeta sdk