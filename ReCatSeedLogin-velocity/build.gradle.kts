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
    compileOnly("com.velocitypowered:velocity-api:3.2.0-SNAPSHOT")
    compileOnly("org.yaml:snakeyaml:2.2")
    compileOnly("javax.inject:javax.inject-api:1.0")
}

tasks.shadowJar {
    archiveClassifier.set("")
}

tasks.named("build") {
    dependsOn("shadowJar")
}