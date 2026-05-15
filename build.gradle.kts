plugins {
    kotlin("jvm") version "1.9.22" apply false
    id("com.gradleup.shadow") version "8.3.0" apply false
}

allprojects {
    group = "cc.baka9"
    version = "2.0.0"
    
    repositories {
        mavenCentral()
        maven { url = uri("https://repo.maven.apache.org/maven2/") }
        maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
        maven { url = uri("https://oss.sonatype.org/content/repositories/releases/") }
        maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://libraries.minecraft.net") }
        maven { url = uri("https://mvn-repo.arim.space/lesser-gpl3/") }
    }
}