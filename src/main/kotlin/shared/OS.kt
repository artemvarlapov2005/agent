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

fun installPackage() {
    if (isPackageInstalled("amneziawg")) return

    runCommandThrow("sudo", "apt-get", "update")
    runCommandThrow("sudo", "apt-get", "install", "-y", "software-properties-common")
    runCommandThrow("sudo", "add-apt-repository", "-y", "ppa:amnezia/ppa")
    runCommandThrow("sudo", "apt-get", "update")
    runCommandThrow("sudo", "apt-get", "install", "-y", "amneziawg")

    if (!isPackageInstalled("amneziawg")) {
        throw error("Failed to install amneziawg")
    }
}

fun createFileIfNotExists(path: Path) = {
    if (!Files.exists(path)) {
        path.parent?.let { Files.createDirectories(it) }
        Files.createFile(path)
    }
}

fun enableService(configFolder : Path, interfaceName: String) {
    val configPath = computePath(configFolder, interfaceName)

    createFileIfNotExists(configPath)

    runCommandThrow("sudo", "systemctl", "enable", getUnit(interfaceName))
}

fun reloadService(interfaceName: String) {
    runCommandThrow("sudo", "systemctl", "reload", getUnit(interfaceName))
}

fun restartService(interfaceName: String) = {
    runCommandThrow("sudo", "systemctl", "restart", getUnit(interfaceName))
}

fun runCommandThrow(vararg command: String) = runCommand(*command).let {
    if (it.first) it.second else throw error(it.second.joinToString("\n"))
}

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
