import kotlin.system.exitProcess

import java.io.IOException
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths

var hadError = false

@Throws(IOException::class)
fun main(args: Array<String>) {
    when (args.size) {
        0 -> runPrompt()
        1 -> runFile(args.first())
        else -> {
            println("Usage: klox [script]")
            exitProcess(64)
        }
    }
}

@Throws(IOException::class)
private fun runPrompt() {
    while (true) {
        print("> ")
        val line = readlnOrNull() ?: break
        run(line)
        hadError = false
    }
}

@Throws(IOException::class)
private fun runFile(path: String) {
    val source = Files.readString(Paths.get(path), Charset.defaultCharset())
    run(source)

    if (hadError) exitProcess(65)
}

private fun run(source: String) {
    val scanner = Scanner(source)
    val tokens = scanner.scanTokens()

    for (token in tokens) println(token)
}

fun error(line: Int, message: String) {
    report(line, "", message)
}

private fun report(line: Int, where: String, message: String) {
    System.err.println("[line $line] Error $where: $message")
    hadError = true
}
