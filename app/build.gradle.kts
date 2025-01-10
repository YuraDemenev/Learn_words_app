import com.google.protobuf.gradle.id

plugins {
    id("com.google.devtools.ksp")
    id("com.google.protobuf")
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.learn_words_app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.learn_words_app"
        minSdk = 27
        //noinspection EditedTargetSdkVersion
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        //Чтобы room не создавал schema
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        //View Binding — это механизм, который автоматически генерирует безопасные и удобные ссылки
        // на элементы пользовательского интерфейса (UI) в ваших макетах XML. Это упрощает работу
        // с View-элементами, улучшает читаемость кода и помогает избежать ошибок, таких как
        // NullPointerException.
        viewBinding = true
    }
}


dependencies {
//    Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    //Для FLow
    // Lifecycles only (without ViewModel or LiveData)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    //Для работы с Fragment
    implementation(libs.androidx.fragment.ktx)

    //Для сохранения данных
    implementation(libs.androidx.datastore)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.protobuf.kotlin)
    implementation(libs.grpc.stub)
    implementation(libs.grpc.protobuf)
    implementation(libs.grpc.okhttp)

    implementation(libs.protobuf.java.util)
    implementation(libs.protobuf.kotlin)
    implementation(libs.grpc.kotlin.stub)


    //Base
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.20.1"
    }
//    plugins {
//        id("java") {
//            artifact = "io.grpc:protoc-gen-grpc-java:3.22.3}"
//        }
//        id("grpc") {
//            artifact = "io.grpc:protoc-gen-grpc-java:3.21.2"
//        }
//
//    }
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                id("java") {
                    option("lite")
                }
            }
            task.builtins {
                id("kotlin") {
                    option("lite")
                }
            }
        }
    }
}