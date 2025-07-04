plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.compose)
}

android {
  namespace = "com.thanes.wardstock"
  compileSdk = 36

  defaultConfig {
    applicationId = "com.thanes.wardstock"
    minSdk = 28
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      storeFile = file("sign.jks")
      storePassword = project.findProperty("RELEASE_STORE_PASSWORD") as String? ?: "default"
      keyAlias = "key0"
      keyPassword = project.findProperty("RELEASE_KEY_PASSWORD") as String? ?: "default"
    }
  }

  buildTypes {
    getByName("release") {
      isMinifyEnabled = false
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
      signingConfig = signingConfigs.getByName("release")
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }

  kotlinOptions {
    jvmTarget = "11"
    freeCompilerArgs = listOf("-XXLanguage:+PropertyParamAnnotationDefaultTargetMode")
  }

  buildFeatures {
    compose = true
  }

  sourceSets {
    getByName("main") {
      jniLibs.srcDirs("src/main/jniLibs")
    }
  }

  packaging {
    jniLibs {
      keepDebugSymbols += listOf(
        "*/armeabi/*.so",
        "*/armeabi-v7a/*.so",
        "*/arm64-v8a/*.so",
        "*/x86_64/*.so"
      )
    }
    resources {
      excludes += listOf(
        "META-INF/AL2.0",
        "META-INF/LGPL2.1"
      )
    }

    pickFirsts += listOf(
      "**/libjnidispatch.so",
      "**/libjnidispatch.dll",
      "**/libjnidispatch.jnilib"
    )
  }
}

dependencies {
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.ui)
  implementation(libs.androidx.ui.graphics)
  implementation(libs.androidx.ui.tooling.preview)
  implementation(libs.androidx.material3)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.retrofit)
  implementation(libs.converter.gson)
  implementation(libs.amqp.client)
  implementation(libs.coil.compose)
  implementation(libs.coil.network.okhttp)
  implementation(libs.androidx.foundation)
  implementation(libs.androidx.appcompat)
  implementation(libs.androidx.appcompat.resources)
  implementation(libs.androidx.material)
  implementation(libs.androidx.core.splashscreen)
  implementation(libs.glide)
  implementation(libs.compose)
  implementation(libs.androidx.datastore.preferences)

//  implementation(files("libs/jna-min.jar"))
//  implementation(files("libs/jna-platform.jar"))
//  implementation(files("libs/json-20220924.jar"))
//
//  implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

  implementation(libs.jna.platform)

  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.ui.test.junit4)
  debugImplementation(libs.androidx.ui.tooling)
  debugImplementation(libs.androidx.ui.test.manifest)
}
