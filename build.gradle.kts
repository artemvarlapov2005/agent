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
    mainClass.set("ru.tinkoff.kora.example.ApplicationKt")
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

    implementation("ru.tinkoff.kora:http-server-undertow")
    implementation("ru.tinkoff.kora:json-module")
    implementation("ru.tinkoff.kora:config-hocon")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.7.3")
    implementation("ch.qos.logback:logback-classic:1.4.8")
    implementation("org.bouncycastle:bcprov-jdk18on:1.78")
    implementation("org.matkini:AWG-API:1.0-SNAPSHOT")
}

kotlin {
    jvmToolchain { languageVersion.set(JavaLanguageVersion.of("21")) }
    sourceSets.main { kotlin.srcDir("build/generated/ksp/main/kotlin") }
    sourceSets.test { kotlin.srcDir("build/generated/ksp/test/kotlin") }
}

tasks.distTar {
    archiveFileName.set("application.tar")
}