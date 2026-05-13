package io.aequicor.aikit.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option

class AiKit : CliktCommand(name = "ai-kit") {
    override fun run() = Unit
}

class ListCommand : CliktCommand(name = "list", help = "List available bundles") {
    override fun run() {
        TODO("not implemented")
    }
}

class ShowCommand : CliktCommand(name = "show", help = "Show bundle contents") {
    private val bundle by argument(help = "Bundle name")

    override fun run() {
        TODO("not implemented")
    }
}

class InstallCommand : CliktCommand(name = "install", help = "Install a bundle into the project") {
    private val bundle by argument(help = "Bundle name")
    private val agent by option("--agent", "-a", help = "Target agent id").default("")
    private val target by option("--target", "-t", help = "Target directory (default: current dir)").default(".")
    private val force by option("--force", "-f", help = "Overwrite existing files").flag()

    override fun run() {
        TODO("not implemented")
    }
}

class ValidateCommand : CliktCommand(name = "validate", help = "Validate a bundle at the given path") {
    private val path by argument(help = "Path to bundle directory or zip")

    override fun run() {
        TODO("not implemented")
    }
}

fun main(args: Array<String>) = AiKit()
    .subcommands(ListCommand(), ShowCommand(), InstallCommand(), ValidateCommand())
    .main(args)
