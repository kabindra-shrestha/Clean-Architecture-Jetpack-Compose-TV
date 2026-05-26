plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.kabindra.tv.iptv.feature.presentation"
    compileSdk {
        version = release(libs.versions.android.compileSdk.get().toInt()) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlin {
        jvmToolchain(21)
    }
}

dependencies {
    api(project(":core:common"))
    api(project(":domain"))

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.kotlinx.coroutines.core)
}
