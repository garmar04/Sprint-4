// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.google.gms:google-services:4.4.2") // Clase necesaria para Google Services
    }
}
plugins {
    id("com.android.application") version "8.8.0" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
}

