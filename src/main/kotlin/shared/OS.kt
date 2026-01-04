package org.matkini.shared

import org.matkini.service.computePath
import java.nio.file.Files
import java.nio.file.Path

fun getDefaultIPv4Interface(): String =
    Files.lines(Path.of("/proc/net/route")).toList().firstOrNull {
        val cols = it.split("\t")

        cols.size > 1 && cols[1] == "00000000"
    } ?: throw error("No default adapter found")

fun isPackageInstalled(packageName: String): Boolean = runCatching {
    runCommand("apt-cache", "policy", packageName).second.any { it.trim().startsWith("Installed:") }
}.getOrElse {
    return false
}

fun installPackage() =
    (isPackageInstalled("amneziawg") ||
    (
        !runCommandBoolean("sudo", "apt-get", "update") ||
        !runCommandBoolean("sudo", "apt-get", "install", "-y", "software-properties-common") ||
        !runCommandBoolean("sudo", "add-apt-repository", "-y", "ppa:amnezia/ppa") ||
        !runCommandBoolean("sudo", "apt-get", "update") ||
        !runCommandBoolean("sudo", "apt-get", "install", "-y", "amneziawg")
    )).let { require(it) }

fun createFileIfNotExists(path: Path) = {
    if (!Files.exists(path)) {
        path.parent?.let { Files.createDirectories(it) }
        Files.createFile(path)
    }
}

fun enableService(configFolder : Path, interfaceName: String) = runCatching {
    val configPath = computePath(configFolder, interfaceName)

    if (!Files.exists(configPath)) {
        Files.createDirectories(configPath.parent)
        Files.createFile(configPath)
    }

    runCommandUnit("sudo", "systemctl", "enable", getUnit(interfaceName))
}.getOrThrow()

fun reloadService(interfaceName: String) = runCatching {
    runCommandUnit("sudo", "systemctl", "reload", getUnit(interfaceName))
}.getOrThrow()

fun runCommandBoolean(vararg command: String): Boolean = runCommand(*command).first

private fun getUnit(interfaceName: String) = "awg-quick@$interfaceName"

fun runCommand(vararg command: String): Pair<Boolean, List<String>> =
    runCatching {
        val process = ProcessBuilder(*command)
            .redirectErrorStream(true)
            .start()
        val exitCode = process.waitFor()
        val output = process.inputStream.bufferedReader().lines().toList()
        return (exitCode == 0) to output
    }.getOrThrow()

fun runCommandUnit(vararg command: String) = require(runCommandBoolean(*command))