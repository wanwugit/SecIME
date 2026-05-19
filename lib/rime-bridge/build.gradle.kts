plugins {
    id("com.android.library")
}

android {
    namespace = "com.secure.ime.rime"
    compileSdk = 36

    defaultConfig {
        minSdk = 23

        @Suppress("UnstableApiUsage")
        externalNativeBuild {
            cmake {
                targets("rime_jni_bridge")
            }
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation("org.secureime:secureime")
}