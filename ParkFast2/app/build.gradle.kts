plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.parkfast2"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.parkfast2"
        minSdk = 24
        targetSdk = 34
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    implementation(platform("com.google.firebase:firebase-bom:33.8.0"))
    implementation ("com.google.android.gms:play-services-maps:19.0.0")
    implementation ("com.google.firebase:firebase-auth:23.1.0")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.android.gms:play-services-auth:21.3.0")  // Necesario para Google Sign-In
    implementation("com.google.firebase:firebase-auth")          // Firebase Authentication
    implementation ("androidx.navigation:navigation-fragment:2.8.5")
    implementation ("androidx.navigation:navigation-ui:2.8.5")

    implementation ("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")
    implementation ("org.eclipse.paho:org.eclipse.paho.android.service:1.1.1")
    implementation ("com.google.android.gms:play-services-location:21.3.0")
    implementation ("com.google.firebase:firebase-messaging:24.1.0")





}