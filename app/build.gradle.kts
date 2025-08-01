import java.io.FileInputStream
import java.util.Properties

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.compose)
}

val keystorePropertiesFile = rootProject.file("gradle.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
  keystoreProperties.load(FileInputStream(keystorePropertiesFile))
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

    externalNativeBuild {
      cmake {
        cppFlags("-std=c++11")
      }
    }

    ndk {
//      abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64"))
      abiFilters.add("armeabi-v7a")
    }
  }

  externalNativeBuild {
    cmake {
      path("src/main/cpp/CMakeLists.txt")
    }
  }

  signingConfigs {
    create("release") {
      keyAlias = keystoreProperties["MYAPP_RELEASE_KEY_ALIAS"] as String
      keyPassword = keystoreProperties["MYAPP_RELEASE_KEY_PASSWORD"] as String
      storeFile = file(keystoreProperties["MYAPP_RELEASE_STORE_FILE"] as String)
      storePassword = keystoreProperties["MYAPP_RELEASE_STORE_PASSWORD"] as String
    }
  }

  buildTypes {
    release {
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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

  packagingOptions {
    pickFirsts += "**/libjnidispatch.so"

    jniLibs {
      useLegacyPackaging = true
    }

    resources.excludes += "META-INF/AL2.0"
    resources.excludes += "META-INF/LGPL2.1"
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
  implementation(libs.coil.gif)
  implementation(libs.coil.network.okhttp)
  implementation(libs.androidx.foundation)
  implementation(libs.androidx.appcompat)
  implementation(libs.androidx.appcompat.resources)
  implementation(libs.androidx.material)
  implementation(libs.androidx.core.splashscreen)
  implementation(libs.glide)
  implementation(libs.compose)
  implementation(libs.androidx.datastore.preferences)
  implementation(libs.accompanist.navigation.animation)

//  implementation(libs.android.serialport.api)

  implementation(files("libs/jna.jar"))
  implementation(files("libs/jna-platform.jar"))
  implementation(files("libs/json-20220924.jar"))

  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.ui.test.junit4)
  debugImplementation(libs.androidx.ui.tooling)
  debugImplementation(libs.androidx.ui.test.manifest)
}
