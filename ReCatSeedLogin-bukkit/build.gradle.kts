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
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib")
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    compileOnly("org.yaml:snakeyaml:2.2")
    compileOnly("org.projectlombok:lombok:1.18.30")
    compileOnly("org.geysermc.floodgate:api:2.2.0-SNAPSHOT")
    compileOnly("com.earth2me:QQ:5.0.0")
    compileOnly("jakarta.mail:jakarta.mail-api:2.1.3")
    compileOnly("com.sun.mail:jakarta.mail:2.0.1")
    
    implementation("space.arim.morepaperlib:morepaperlib-paper:0.4.0")
    implementation("cc.baka9:handyplus:3.2.8")
    
    compileOnly("org.yaml:snakeyaml:2.2") {
        because("Conflict with paper-api")
    }
}

tasks {
    shadow {
        relocate("org.yaml.snakeyaml", "cc.baka9.catseedlogin.lib.snakeyaml")
        relocate("jakarta.mail", "cc.baka9.catseedlogin.lib.mail")
        archiveClassifier.set("")
    }
    
    build {
        dependsOn(shadow)
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}