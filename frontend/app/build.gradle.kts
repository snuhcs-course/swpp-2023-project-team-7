plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.example.readability"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.readability"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "com.example.readability.HiltTestRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        externalNativeBuild {
            cmake {
                cppFlags += ""
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }
    packaging {
        resources {
            excludes += "/META-INF/LICENSE.md"
            excludes += "/META-INF/LICENSE-notice.md"
            excludes += "/META-INF/AL2.0"
            excludes += "/META-INF/LGPL2.1"
            excludes += "/META-INF/licenses/ASM"
            excludes += "/META-INF/**/attach_hotspot_windows.dll"
        }
    }
    aaptOptions {
        noCompress += "txt"
        noCompress += "ttf"
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
        unitTests.all { it.jvmArgs("-noverify") }
    }
    sourceSets {
        getByName("main") {
            java.srcDirs("src/main/kotlin")
        }
        getByName("test") {
            java.srcDirs("src/test/kotlin")
        }
        getByName("androidTest") {
            java.srcDirs("src/androidTest/kotlin")
        }
    }
}

dependencies {
    val hiltVersion = "2.48"
    ksp("com.google.dagger:hilt-compiler:$hiltVersion")
    ksp("com.google.dagger:hilt-android-compiler:$hiltVersion")
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    val roomVersion = "2.6.0"
    ksp("androidx.room:room-compiler:$roomVersion")
    annotationProcessor("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    implementation("androidx.room:room-rxjava2:$roomVersion")
    implementation("androidx.room:room-rxjava3:$roomVersion")
    implementation("androidx.room:room-guava:$roomVersion")
    implementation("androidx.room:room-paging:$roomVersion")

    implementation("androidx.core:core-splashscreen:1.1.0-alpha02")
    implementation("androidx.navigation:navigation-compose:2.7.5")
    implementation("androidx.compose.material3:material3:1.2.0-alpha11")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.1")
    implementation(platform("androidx.compose:compose-bom:2023.03.00"))
    implementation("androidx.compose.ui:ui:1.5.4")
    implementation("androidx.compose.ui:ui-graphics:1.5.4")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.4")
    implementation("androidx.compose.foundation:foundation-android:1.5.4")
    implementation("io.coil-kt:coil:2.4.0")
    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("androidx.compose.runtime:runtime-tracing:1.0.0-alpha05")

    testImplementation("androidx.room:room-testing:$roomVersion")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.11.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
    testImplementation("org.mockito:mockito-core:4.4.0")
    testImplementation("org.mockito:mockito-inline:4.4.0") // For final classes
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.0")
    testImplementation("com.google.dagger:hilt-android-testing:$hiltVersion")

    androidTestImplementation("androidx.test:core:1.5.0")
    androidTestImplementation("androidx.test:core-ktx:1.5.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.ext:junit-ktx:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.4")
    androidTestImplementation("androidx.navigation:navigation-testing:2.7.5")
    androidTestImplementation("com.google.dagger:hilt-android-testing:$hiltVersion")
    androidTestImplementation("org.awaitility:awaitility:4.2.0")

    val mocckVersion = "1.13.8"
    testImplementation("io.mockk:mockk:$mocckVersion")
    androidTestImplementation("io.mockk:mockk-android:$mocckVersion")
    androidTestImplementation("io.mockk:mockk-agent:$mocckVersion")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

configurations.all {
    resolutionStrategy.dependencySubstitution {
        substitute(module("org.hamcrest:hamcrest-core:1.3")).using(module("junit:junit:4.13.2"))
        substitute(module("org.hamcrest:hamcrest-library:1.3")).using(module("junit:junit:4.13.2"))
    }
}
