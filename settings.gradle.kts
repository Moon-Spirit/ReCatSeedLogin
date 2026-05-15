pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        id("com.gradleup.shadow") version "8.3.0"
    }
}

rootProject.name = "ReCatSeedLogin"

include(":ReCatSeedLogin-bukkit")
include(":ReCatSeedLogin-bungee")
include(":ReCatSeedLogin-velocity")
include(":ReCatSeedLogin-common")