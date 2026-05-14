plugins {
    kotlin("jvm")
    id("io.github.goooler.shadow")
}

val baseDir = "src/main/kotlin"
val resourcesDir = "src/main/resources"

sourceSets {
    main {
        kotlin { setSrcDirs(listOf(baseDir)) }
        resources { setSrcDirs(listOf(resourcesDir)) }
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    compileOnly("net.md-5:bungeecord-api:1.21-R0.1-SNAPSHOT")
    compileOnly("org.yaml:snakeyaml:2.2")
    implementation("cc.baka9:handyplus:3.2.8")
}

tasks {
    shadow { archiveClassifier.set("") }
    build { dependsOn(shadow) }
}

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(17)) }
}