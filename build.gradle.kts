//buildscript {
//    dependencies {
//        classpath("com.google.protobuf:protobuf-gradle-plugin:0.9.4")
//    }
//}

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.google.protobuf") version "0.9.4" apply false
    id("com.google.devtools.ksp") version "2.1.0-1.0.29" apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false

}
