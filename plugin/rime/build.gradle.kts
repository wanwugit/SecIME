plugins {
    id("org.fcitx.fcitx5.android.app-convention")
    id("org.fcitx.fcitx5.android.plugin-app-convention")
    id("org.fcitx.fcitx5.android.native-app-convention")
    id("org.fcitx.fcitx5.android.build-metadata")
    id("org.fcitx.fcitx5.android.data-descriptor")
    id("org.fcitx.fcitx5.android.fcitx-component")
}

android {
    namespace = "org.fcitx.fcitx5.android.plugin.rime"

    defaultConfig {
        applicationId = "org.fcitx.fcitx5.android.plugin.rime"

        @Suppress("UnstableApiUsage")
        externalNativeBuild {
            cmake {
                targets(
                    "rime"
                )
            }
        }
    }

    buildFeatures {
        resValues = true
    }

    buildTypes {
        release {
            resValue("string", "app_name", "@string/app_name_release")
            proguardFile("proguard-rules.pro")
        }
        debug {
            resValue("string", "app_name", "@string/app_name_debug")
        }
    }

    packaging {
        jniLibs {
            excludes += setOf(
                "**/libc++_shared.so",
                "**/libFcitx5*"
            )
        }
    }
}

fcitxComponent {
    installPrebuiltAssets = true
}

generateDataDescriptor {
    symlinks.put("usr/share/rime-data/opencc", "usr/share/opencc")
}

dependencies {
    implementation(project(":lib:fcitx5"))
    implementation(project(":lib:plugin-base"))
}

// Pre-compile Rime dictionaries at build time using rime_deployer.exe
// This avoids the user having to wait for dictionary compilation on first launch
val precompileRimeData by tasks.registering {
    group = "rime"
    description = "Pre-compile Rime YAML dictionaries into binary format at build time"

    val rimeDataDir = file("src/main/cpp")
    val prebuiltHostDir = file("prebuilt-host/librime/dist/bin")
    val outputDir = file("${layout.buildDirectory.get()}/rime-precompile/user_data")
    val stagingDir = file("${layout.buildDirectory.get()}/rime-precompile/staging")

    inputs.dir(rimeDataDir)
    outputs.dir(outputDir)

    doLast {
        outputDir.deleteRecursively()
        stagingDir.deleteRecursively()
        outputDir.mkdirs()
        stagingDir.mkdirs()

        val deployerExe = prebuiltHostDir.resolve("rime_deployer.exe")
        if (!deployerExe.exists()) {
            logger.lifecycle("rime_deployer.exe not found, skipping pre-compilation")
            logger.lifecycle("To enable, download librime Windows binary to prebuilt-host/")
            return@doLast
        }

        logger.lifecycle("Pre-compiling Rime dictionaries...")
        logger.lifecycle("  Shared data dir: $rimeDataDir")
        logger.lifecycle("  User data dir: $outputDir")
        logger.lifecycle("  Staging dir: $stagingDir")

        val process = ProcessBuilder()
            .command(
                deployerExe.absolutePath,
                "--build",
                outputDir.absolutePath,
                rimeDataDir.absolutePath,
                stagingDir.absolutePath
            )
            .directory(prebuiltHostDir)
            .apply {
                environment()["PATH"] = prebuiltHostDir.absolutePath + ";" + System.getenv("PATH")
            }
            .redirectErrorStream(true)
            .start()

        val output = process.inputStream.bufferedReader().readText()
        val exitCode = process.waitFor()

        if (exitCode != 0) {
            logger.error("rime_deployer failed with exit code $exitCode")
            logger.error(output)
            throw GradleException("Rime pre-compilation failed")
        }

        logger.lifecycle(output)

        // Copy generated binary files from staging dir to rime-data assets
        val assetsRimeData = file("src/main/assets/usr/share/rime-data")
        assetsRimeData.mkdirs()
        stagingDir.walkTopDown().filter { it.isFile }.forEach { src ->
            val rel = src.relativeTo(stagingDir)
            val dest = assetsRimeData.resolve(rel)
            dest.parentFile.mkdirs()
            src.copyTo(dest, overwrite = true)
            logger.lifecycle("  Copied: $rel (${src.length()} bytes)")
        }
    }
}
