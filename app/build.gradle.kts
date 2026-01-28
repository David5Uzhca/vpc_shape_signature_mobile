plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android) // Aplica el plugin de Kotlin aquí
}

android {
    // 1. CORRECCIÓN: Este debe ser tu paquete, NO org.opencv
    namespace = "com.example.shapesignatureapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.shapesignatureapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        externalNativeBuild {
            cmake {
                cppFlags("-std=c++17")
                // Pasamos la ruta absoluta de OpenCV a CMake
                arguments("-DOpenCV_DIR=${file("../OpenCV-android-sdk/sdk/native/jni").absolutePath}")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = "21"
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // 2. IMPORTANTE: Aquí falta la librería de OpenCV para Java/Kotlin
    implementation(project(":opencv"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
}