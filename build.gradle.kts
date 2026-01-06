import org.gradle.kotlin.dsl.attributes
import org.gradle.kotlin.dsl.from
import kotlin.text.set

plugins {
    kotlin("jvm") version ("1.9.25")
    id("com.google.devtools.ksp") version ("1.9.25-1.0.20")
    id("application")
}

repositories {
    mavenCentral()
    mavenLocal()
}

application {
    mainClass.set("org.matkini.MainKt")
}

val koraBom: Configuration by configurations.creating
configurations {
    ksp.get().extendsFrom(koraBom)
    compileOnly.get().extendsFrom(koraBom)
    api.get().extendsFrom(koraBom)
    implementation.get().extendsFrom(koraBom)
}

dependencies {
    koraBom(platform("ru.tinkoff.kora:kora-parent:1.2.5"))
    ksp("ru.tinkoff.kora:symbol-processors")

    implementation("org.apache.avro:avro:1.11.3")

    implementation("ru.tinkoff.kora:http-server-undertow")
    implementation("ru.tinkoff.kora:json-module")
    implementation("ru.tinkoff.kora:config-hocon")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.7.3")
    implementation("ch.qos.logback:logback-classic:1.4.8")
    implementation("org.bouncycastle:bcprov-jdk18on:1.78")
    implementation("org.matkini:AWG-API:1.0-SNAPSHOT")
    implementation("ru.tinkoff.kora:cache-caffeine")
    implementation("ru.tinkoff.kora:scheduling-jdk")
}

kotlin {
    jvmToolchain { languageVersion.set(JavaLanguageVersion.of("21")) }
    sourceSets.main { kotlin.srcDir("build/generated/ksp/main/kotlin") }
    sourceSets.test { kotlin.srcDir("build/generated/ksp/test/kotlin") }
}

tasks.distTar {
    archiveFileName.set("application.tar")
}

tasks {
    val fatJar =
        register<Jar>("fatJar") {
            dependsOn.addAll(
                listOf(
                    "compileJava",
                    "compileKotlin",
                    "processResources",
                ),
            ) // We need this for Gradle optimization to work
            archiveClassifier.set("standalone") // Naming the jar
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
            manifest {
                attributes(
                    mapOf("Main-Class" to application.mainClass),
                )
            } // Provided we set it up in the application plugin configuration
            val sourcesMain = sourceSets.main.get()
            val contents = configurations.runtimeClasspath.get().map {
                if (it.isDirectory) it
                else zipTree(it).matching {
                    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
                }
            } + sourcesMain.output

            from(contents)
        }
    build {
        dependsOn(fatJar) // Trigger fat jar creation during build
    }
}