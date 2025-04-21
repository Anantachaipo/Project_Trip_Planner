plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.project_cbnew"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.project_cbnew"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // หลัก ๆ
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.core.ktx)

    // Firebase (ตามที่คุณมี)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)

    // Google Maps
    implementation(libs.play.services.maps)
    implementation("com.google.android.libraries.places:places:3.3.0")


    implementation("com.google.maps:google-maps-services:0.18.0")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.google.firebase:firebase-storage:20.3.0")

    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")

    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.2.1")

    // ViewPager2 – สำคัญสำหรับ TabLayoutMediator
    implementation("androidx.viewpager2:viewpager2:1.0.0")

    // ✅ เพิ่ม FlexboxLayout
    implementation("com.google.android.flexbox:flexbox:3.0.0")

    implementation("com.google.android.material:material:1.11.0") // หรือเวอร์ชันล่าสุด



    // (ทดสอบ)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
