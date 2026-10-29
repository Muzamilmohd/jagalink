import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// API key Google Safe Browsing — dalam local.properties (tidak masuk git): safeBrowsing.apiKey=...
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

// Kunci tandatangan release (tidak masuk git). Semua APK update mesti guna kunci yang sama.
val keystoreProps = Properties().apply {
    val f = rootProject.file("keystore.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

android {
    namespace = "com.jagalink"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.jagalink"
        minSdk = 26
        targetSdk = 36
        versionCode = 10
        versionName = "1.8"
    }

    buildFeatures {
        buildConfig = true
    }

    // github = APK di GitHub Releases (kemas kini sendiri + Baca skrin).
    // play   = Google Play: tiada kemas kini sendiri, tiada internet, tiada Baca skrin
    //          (kebenaran tu tak dibenarkan / perlu deklarasi khas di Play).
    flavorDimensions += "store"
    productFlavors {
        create("github") {
            dimension = "store"
            // Repo GitHub (owner/nama) tempat semak update — tetapkan dalam gradle.properties
            val repo = (project.findProperty("jagalink.updateRepo") as String?).orEmpty()
            buildConfigField("String", "UPDATE_REPO", "\"$repo\"")
            buildConfigField("boolean", "PLAY", "false")
            buildConfigField("String", "SB_KEY", "\"${localProps.getProperty("safeBrowsing.apiKey", "")}\"")
        }
        create("play") {
            dimension = "store"
            buildConfigField("String", "UPDATE_REPO", "\"\"")
            buildConfigField("boolean", "PLAY", "true")
            buildConfigField("String", "SB_KEY", "\"\"") // versi Play tiada internet
        }
    }

    signingConfigs {
        if (keystoreProps.containsKey("storeFile")) {
            create("release") {
                storeFile = rootProject.file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.findByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    // Tiada library luar — semua guna Android standard supaya ringan.
}
