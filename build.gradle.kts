plugins {
    kotlin("jvm") version "1.9.22" apply false
    id("io.github.goooler.shadow") version "8.1.7" apply false
}

allprojects {
    group = "cc.baka9"
    version = "2.0.0"
    
    repositories {
        mavenCentral()
        maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
        maven { url = uri("https://oss.sonatype.org/content/repositories/releases/") }
        maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
        maven { url = uri("https://repo.codemc.io/repository/maven-releases/") }
        maven { url = uri("https://repo.codemc.io/repository/maven-snapshots/") }
        maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
    }
}