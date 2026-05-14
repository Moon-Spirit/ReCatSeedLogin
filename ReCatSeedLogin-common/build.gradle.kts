plugins {
    kotlin("jvm")
}

val baseDir = "src/main/kotlin"

sourceSets {
    main {
        kotlin { setSrcDirs(listOf(baseDir)) }
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    compileOnly("org.yaml:snakeyaml:2.2")
}

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(17)) }
}