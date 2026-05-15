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
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib")
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    compileOnly("org.yaml:snakeyaml:2.2")
    compileOnly("org.projectlombok:lombok:1.18.30")
    compileOnly("jakarta.mail:jakarta.mail-api:2.1.3")
    compileOnly("com.sun.mail:jakarta.mail:2.0.1")

    compileOnly("org.yaml:snakeyaml:2.2") {
        because("Conflict with paper-api")
    }
}

tasks.shadowJar {
    relocate("org.yaml.snakeyaml", "cc.baka9.catseedlogin.lib.snakeyaml")
    relocate("jakarta.mail", "cc.baka9.catseedlogin.lib.mail")
    archiveClassifier.set("")
}

tasks.named("build") {
    dependsOn("shadowJar")
}