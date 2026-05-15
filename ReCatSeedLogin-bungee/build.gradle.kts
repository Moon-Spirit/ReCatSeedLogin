plugins {
    kotlin("jvm")
    id("com.gradleup.shadow") version "8.3.0"
}

val baseDir = "src/main/kotlin"
val resourcesDir = "src/main/resources"

sourceSets {
    main {
        kotlin { setSrcDirs(listOf(baseDir)) }
        resources { setSrcDirs(listOf(resourcesDir)) }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    compileOnly("net.md-5:bungeecord-api:1.21-R0.1-SNAPSHOT")
    compileOnly("org.yaml:snakeyaml:2.2")
}

tasks.shadowJar {
    archiveClassifier.set("")
}

tasks.named("build") {
    dependsOn("shadowJar")
}