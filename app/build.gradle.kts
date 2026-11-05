plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")      // Hilt (Chapter 10)
    id("com.google.gms.google-services")      // Firebase (Chapter 11)
}

android {
    namespace  = "com.example.ch12"
    compileSdk = 34

    defaultConfig {
        // WAJIB sama dengan package name aplikasi Android di Firebase Console
        applicationId = "com.example.ch12"
        minSdk        = 24
        targetSdk     = 34
        versionCode   = 1
        versionName   = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // true -> Auth, Firestore, dan Storage diarahkan ke Firebase Emulator Suite
        // di komputer (tanpa paket Blaze). Diatur di gradle.properties.
        val useEmulator = providers.gradleProperty("catatanku.useFirebaseEmulator").orNull == "true"
        buildConfigField("boolean", "USE_FIREBASE_EMULATOR", useEmulator.toString())
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.09.00")
    implementation(composeBom)

    implementation("androidx.core:core-ktx:1.13.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.8.5")

    // ViewModel + StateFlow di Compose (Chapter 6)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

    // Hilt (Chapter 10)
    val hiltVersion = "2.51.1"
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    ksp("com.google.dagger:hilt-android-compiler:$hiltVersion")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Firebase BoM: satu versi mengatur semua library Firebase.
    // Sejak BoM 32.5, API Kotlin sudah ada di modul utama; modul *-ktx tidak diperlukan.
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    // Task.await(): mengubah Task Firebase menjadi suspend function
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")

    // Chapter 12: berkas biner (foto profil) di Cloud Storage for Firebase
    implementation("com.google.firebase:firebase-storage")

    // Chapter 12: kamera di dalam aplikasi (preview live + ambil foto)
    val cameraxVersion = "1.3.4"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")
    // ListenableFuture.await(): seperti Task.await() pada Firebase.
    // Guava sudah ikut lewat Firebase, tetapi hanya saat runtime; di sini
    // dibuat terlihat saat compile (versi sama, jadi APK tidak bertambah).
    implementation("androidx.concurrent:concurrent-futures-ktx:1.2.0")
    implementation("com.google.guava:guava:32.0.1-android")

    // Chapter 12: membaca orientasi foto (EXIF) sebelum dikompres
    implementation("androidx.exifinterface:exifinterface:1.3.7")

    // Coil (Chapter 9): menampilkan preview lokal (content://, file://) dan URL Storage
    implementation("io.coil-kt.coil3:coil-compose:3.0.4")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.0.4")

    // Google Sign-In lewat Credential Manager (Tugas 5)
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Unit test JVM: ViewModel diuji dengan fake repository, tanpa Firebase
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
}
