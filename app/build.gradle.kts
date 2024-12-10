plugins {
    id("com.android.application")
    id("com.google.gms.google-services") // Google Services Plugin
    id("org.jetbrains.kotlin.android") // Kotlin Android Plugin
    id("kotlin-kapt") // Kapt for annotation processing
}

android {
    namespace = "com.tdtu.edu.vn.mygallery"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.tdtu.edu.vn.mygallery"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    // Firebase dependencies using Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-auth:22.1.2")
    implementation("com.google.firebase:firebase-database:21.0.0")
    implementation("com.google.firebase:firebase-analytics")
    implementation ("com.google.firebase:firebase-dynamic-module-support:16.0.0-beta03")

    // Google Maps SDK for Android
    implementation("com.google.android.gms:play-services-maps:18.0.2")

    // AndroidX and Material Design dependencies
    implementation("androidx.recyclerview:recyclerview:1.3.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.cardview:cardview:1.0.0")

    // AndroidX ExifInterface for handling EXIF metadata
    implementation("androidx.exifinterface:exifinterface:1.3.6")

    // PhotoView and Glide for image handling
    implementation("com.github.chrisbanes:PhotoView:2.3.0")
    implementation("com.github.bumptech.glide:glide:4.15.1")
    kapt("com.github.bumptech.glide:compiler:4.15.1")

    // Room Database
    kapt("androidx.room:room-compiler:2.5.1")
    implementation("androidx.room:room-runtime:2.5.1")

    // ShowHidePasswordEditText
    implementation("com.github.scottyab:showhidepasswordedittext:0.8")

    // Testing dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
