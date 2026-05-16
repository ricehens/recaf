import org.gradle.api.plugins.antlr.AntlrTask

plugins {
    id("java")
    id("application")
    id("antlr") 
    id("org.graalvm.buildtools.native") version "1.1.0"
}

group = "recaf"
version = "1.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(24))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    antlr("org.antlr:antlr4:4.13.2")
    implementation("org.antlr:antlr4-runtime:4.13.2") 
}

application {
    mainClass.set("recaf.Main")
}

// ANTLR
sourceSets {
    main {
        antlr {
            setSrcDirs(listOf("src/main/java/recaf/antlr"))
        }
        java {
            srcDirs("src/main/java", "src/main/gen")
        }
    }
}

tasks.named<AntlrTask>("generateGrammarSource") {
    outputDirectory = file("src/main/gen/recaf/antlr")
    arguments = arguments + listOf(
        "-package", "recaf.antlr",
        "-visitor",
        "-Xexact-output-dir"
    )
}

// jar
tasks.named<Jar>("jar") {
    from("stdlib") {
        include("libsystem.a", "libfloat64.a")
        into("stdlib")
    }

    manifest {
        attributes["Main-Class"] = application.mainClass.get()
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from({
        configurations
            .runtimeClasspath
            .get()
            .filter { it.name.endsWith("jar") }
            .map { zipTree(it) }
    })
}

// nativeCompile
graalvmNative {
    binaries {
        named("main") {
            imageName.set("recaf")
            mainClass.set("recaf.Main")
            sharedLibrary.set(false)

            buildArgs.addAll(
                listOf(
                    "--static-nolibc",
                    "--no-fallback",
                    "-H:IncludeResources=stdlib/.*"
                )
            )
        }
    }
}
