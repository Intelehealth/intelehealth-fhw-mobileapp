plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}
apply("${rootProject.projectDir}/whitelabel.gradle")
android {
    namespace = "org.intelehealth.core.utils"
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

    api(project(":resources"))
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")

    api("androidx.activity:activity-ktx:1.9.2")
    api("androidx.fragment:fragment-ktx:1.8.3")

    api("com.google.code.gson:gson:2.10.1")

    // Timber logging
    api("com.github.ajalt:timberkt:1.5.1")

    // Preference
    api("androidx.preference:preference-ktx:1.2.1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}