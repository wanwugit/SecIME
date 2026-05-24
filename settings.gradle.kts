pluginManagement {
    includeBuild("build-logic")
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "secureime"

include(":sect9")
include(":lib:common")
include(":lib:fcitx5")
include(":lib:fcitx5-lua")
include(":lib:fcitx5-chinese-addons")
include(":codegen")
include(":app")
