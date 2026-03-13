package ch.protonmail.tdanvers.lambda

import kotlin.io.path.*
import java.nio.file.*
import kotlin.uuid.*

const val VERSION_INFO = """lcc v1.1.1-pre
Copyright (C) 2026 Tressa Danvers.
This is free software; see the source for copying conditions.  There is NO
warranty; not even for MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
"""

const val HELP_INFO = """Usage: lcc [compile options] file...
Compile Options:
  -o <file>                Place the output into <file>.
  --unnormalized           Skip the normalization step during compilation.

Usage: lcc [information options]
Information Options:
  --help, -h               Display this information.
  --version                Display compiler version information.
"""

@ExperimentalUuidApi
fun main(vararg args: String) {
  val (switches, inputFiles) =
    parseArguments(args.map { SWITCH_ALIASES[it] ?: it })

  if (switches.any { (it) -> it !in SWITCHES })
    fatalError("unrecognized command-line option(s) " +
      "${ANSI_BOLD}${switches.prettySwitchList()}${ANSI_NORMAL}")

  if (!switches.areAllContinueOrAllNonContinue())
    fatalError("cannot coexist with the command-line option(s) "+
      "${switches.filter { (it) -> it !in CONTINUE_SWITCHES }.prettySwitchList()}, " +
      "are not compilation options, and so compilation cannot proceed\n" +
      "compilation terminated.")

  if (shouldContinueCompilation(switches))
    compile(inputFiles, switches.outputPath, switches.shouldNormalize)
  else
    callAllProcs(switches)
}

@ExperimentalUuidApi
fun compile(inputFiles: List<String>, outputPath: Path?, shouldNormalize: Boolean): Nothing {
  if (inputFiles.isEmpty())
    fatalError("no input files\ncompilation terminated.")

  inputFiles
    .map(::parsePath)
    .also { potentialInputPaths ->
      val nonFiles = potentialInputPaths.filterNot(Path::isRegularFile)
      if (nonFiles.isNotEmpty())
        fatalError("the following input files could not be found: ${nonFiles.joinToString(", ")}")
    }
    .map(Path::sequencedReader)
    .map(::parse)
    .reduce(::Application)
    .let { if (shouldNormalize) interpret(it) else normalizeParentheses(it) }
    .let { generateAndEmit(it, outputPath ?: getDefaultOutputPath(inputFiles)) }

  exitSuccess()
}

fun callAllProcs(switches: List<Pair<String,List<String>>>): Nothing {
  switches.forEach { (switch, arguments) ->
    SWITCHES[switch]!!.call(*(arguments.toTypedArray()))
  }
  exitSuccess()
}

val SWITCHES = mapOf(
  "-o" to ::parsePath,
  "--unnormalized" to {}::invoke,
  "--help" to { println(HELP_INFO) }::invoke,
  "--version" to { println(VERSION_INFO) }::invoke,
)

fun parsePath(filePath: String) =
  Path(filePath).toAbsolutePath()

val SWITCH_ALIASES = mapOf<String,String>()

val CONTINUE_SWITCHES = listOf(
  "-o",
  "--unnormalized",
)

fun String.isSwitch() =
  this.matches("^-.*$".toRegex())

fun parseArguments(args: Iterable<String>) =
  args.fold(emptyList<Pair<String,List<String>>>() to emptyList<String>()) { (switches, inputFiles), next ->
    if (switches.isEmpty()) {
      if (next.isSwitch()) (switches + (next to emptyList())) to inputFiles
      else switches to (inputFiles + next)
    } else {
      val (lastSwitch, lastSwitchArguments) = switches.last()
      val switchFunc = SWITCHES[lastSwitch]
      val argumentCountOfSwitch = switchFunc?.let { it.parameters.size } ?: 0
      val satisfiedArguments = lastSwitchArguments.size

      if (satisfiedArguments < argumentCountOfSwitch)
        (switches.dropLast(1) + (lastSwitch to (lastSwitchArguments + next))) to inputFiles
      else if (next.isSwitch()) (switches + (next to emptyList())) to inputFiles
      else switches to (inputFiles + next)
    }
  }.let { (switches, inputFiles) ->
    val distinctSwitches = switches.distinctBy { (it) -> it }
    if (distinctSwitches.size != switches.size)
      fatalError("provided command-line option(s) more than once, ${switches.prettySwitchList()}")
    distinctSwitches.sortedBy { (it) -> it } to inputFiles
  }

val List<Pair<String,List<String>>>.outputPath get() =
  find { (it) -> it == "-o" }
    ?.let { (_, arguments) -> arguments.first() }
    .let { it }
    ?.let(::parsePath)

fun getDefaultOutputPath(inputFiles: List<String>) =
  Path("${inputFiles.first()}")
    .run { "$nameWithoutExtension.lci" }
    .let(::parsePath)

val List<Pair<String,List<String>>>.shouldNormalize get() =
  none { (it) -> it ==  "--unnormalized" }

fun List<Pair<String,List<String>>>.areAllContinueOrAllNonContinue() =
  all { (it) -> it in CONTINUE_SWITCHES } || none { (it) -> it in CONTINUE_SWITCHES }

fun shouldContinueCompilation(switches: List<Pair<String,List<String>>>) =
  switches.none { (it) -> it !in CONTINUE_SWITCHES }

fun List<Pair<String,List<String>>>.prettySwitchList() =
  joinToString(", ") { (it) -> "‘$it’" }
