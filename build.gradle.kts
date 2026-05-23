import org.gradle.api.plugins.antlr.AntlrTask

plugins {
    id("java")
    id("application")
    id("antlr") 
    id("org.graalvm.buildtools.native") version "1.1.0"
}

group = "recaf"
version = "1.1.4"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
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
            setSrcDirs(listOf("src/main/antlr"))
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

val buildStdlib by tasks.registering(Exec::class) {
    workingDir = file("stdlib")
    commandLine("make")
}

// jar
tasks.named<Jar>("jar") {
    dependsOn(buildStdlib)

    from("stdlib") {
        include("lib*.a")
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
tasks.named("nativeCompile") {
    dependsOn(buildStdlib)
}

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
