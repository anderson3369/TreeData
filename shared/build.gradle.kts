import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
    kotlin("plugin.serialization")
    id("com.google.devtools.ksp")
    id("androidx.room") version "2.8.4"
}

kotlin {
    // Apply modern hierarchy template
    applyDefaultHierarchyTemplate()

    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
    }

    val xcf = XCFramework()
    val xcfName = "shared"

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = xcfName
            isStatic = true
            xcf.add(this)
        }
    }

    sourceSets {
        val kotlinVersion = rootProject.extra["kotlinVersion"] as String
        val roomVersion = "2.8.4"
        val ktorVersion = "3.1.2"
        val coroutinesVersion = "1.10.2"
        val serializationVersion = "1.8.0"
        val lifecycleVersion = "2.8.7"

        all {
            languageSettings.optIn("kotlin.time.ExperimentalTime")
            languageSettings.optIn("kotlin.experimental.ExperimentalObjCName")
        }

        commonMain.dependencies {
            implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
            implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
            implementation("androidx.room:room-runtime:$roomVersion")
            implementation("androidx.sqlite:sqlite-bundled:2.6.2")
            implementation("androidx.lifecycle:lifecycle-viewmodel:$lifecycleVersion")
            // Firestore
            implementation("dev.gitlive:firebase-auth:1.13.0")
            implementation("dev.gitlive:firebase-firestore:1.13.0")
            implementation("com.benasher44:uuid:0.8.4")

        }

        val androidMain by getting {
            dependencies {
                // Moved to bottom dependencies block
            }
        }

        commonTest.dependencies {
            implementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
        }
    }
}

android {
    namespace = "com.orchardlog.treedata"
    compileSdk = 36
    defaultConfig {
        minSdk = 33
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    val roomVersion = "2.8.4"
    add("kspAndroid", "androidx.room:room-compiler:$roomVersion")
    add("kspIosSimulatorArm64", "androidx.room:room-compiler:$roomVersion")
    add("kspIosX64", "androidx.room:room-compiler:$roomVersion")
    add("kspIosArm64", "androidx.room:room-compiler:$roomVersion")

    // Use explicit versions for Android to bypass BOM resolution issues in the shared KMP module.
    // These versions correspond to Firebase BOM 34.10.0
    val firebaseAuthVersion = "23.2.0"
    val firebaseCommonVersion = "21.0.0"
    val firebaseFirestoreVersion = "25.1.2"

    implementation("com.google.firebase:firebase-auth:$firebaseAuthVersion")
    implementation("com.google.firebase:firebase-auth-ktx:$firebaseAuthVersion")
    implementation("com.google.firebase:firebase-common:$firebaseCommonVersion")
    implementation("com.google.firebase:firebase-common-ktx:$firebaseCommonVersion")
    implementation("com.google.firebase:firebase-firestore:$firebaseFirestoreVersion")
    implementation("com.google.firebase:firebase-firestore-ktx:$firebaseFirestoreVersion")
}

room {
    schemaDirectory("$projectDir/schemas")
}

kotlin {
    sourceSets{
        all {
            languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
        }
    }

}