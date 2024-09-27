plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("kotlin-parcelize")
}

apply("${rootProject.projectDir}/whitelabel.gradle")

android {
    namespace = "org.intelehealth.core.socket"
    compileSdk = 34

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {

    api(project(":core:utils"))
    api("io.socket:socket.io-client:1.0.0") {
        exclude(
            group = "org.json", module = "json"
        )
    }

    val hiltVersion = "2.47"
    api("com.google.dagger:hilt-android:$hiltVersion")
    annotationProcessor("com.google.dagger:hilt-compiler:$hiltVersion")
    kapt("com.google.dagger:hilt-compiler:$hiltVersion")

    // WorkManager
    val work_version = "2.9.1"
    api("androidx.work:work-runtime-ktx:$work_version")
    api("androidx.hilt:hilt-work:1.2.0")
    // When using Kotlin.
    kapt("androidx.hilt:hilt-compiler:1.2.0")

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}