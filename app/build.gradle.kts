plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

android {
    namespace = "dev.jvqtil.flow"

    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "dev.jvqtil.flow"
        minSdk = 26
        targetSdk = 37
        versionCode = 104
        versionName = "1.0.4-1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = (
                    providers.gradleProperty("FLOW_KEYSTORE").orNull
                        ?: System.getenv("FLOW_KEYSTORE")
                    )?.let { rootProject.file(it) }

            storePassword =
                providers.gradleProperty("FLOW_KEYSTORE_PASSWORD").orNull
                    ?: System.getenv("FLOW_KEYSTORE_PASSWORD")

            keyAlias =
                providers.gradleProperty("FLOW_KEY_ALIAS").orNull
                    ?: System.getenv("FLOW_KEY_ALIAS")

            keyPassword =
                providers.gradleProperty("FLOW_KEY_PASSWORD").orNull
                    ?: System.getenv("FLOW_KEY_PASSWORD")
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }

        release {
            signingConfig = signingConfigs.getByName("release")

            optimization {
                enable = true
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.ui.text)
    implementation(libs.androidx.compose.ui.unit)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material3)

    implementation(libs.androidx.room3.runtime)
    implementation(libs.androidx.sqlite.framework)
    ksp(libs.androidx.room3.compiler)

    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.reorderable)
    implementation(libs.coil.compose)

    implementation(libs.kotlinx.serialization.json)
}