// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

// settings.gradle.kts 에는 손댈 필요 없음

// build.gradle.kts (루트)
buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.1") // 최신 버전 확인
    }
}