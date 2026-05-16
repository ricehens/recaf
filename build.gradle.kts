import org.gradle.api.plugins.antlr.AntlrTask

plugins {
    id("java")
    id("application")
    id("antlr") 
}

group = "recaf"
version = "0.1.0-SNAPSHOT"

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

// Javadoc
tasks.register<Javadoc>("generateJavadoc") {
    source = sourceSets.main.get().allJava
    destinationDir = file("build/docs/javadoc")
    classpath = sourceSets.main.get().compileClasspath
    options {
        (this as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
    }
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
