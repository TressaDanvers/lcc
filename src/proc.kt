package ch.protonmail.tdanvers.lambda

import kotlin.system.*

const val ANSI_RED = "\u001b[31m"
const val ANSI_YELLOW = "\u001b[33m"
const val ANSI_BLUE = "\u001b[34m"
const val ANSI_NOCOLOR = "\u001b[39m"

const val ANSI_BOLD = "\u001b[1m"
const val ANSI_NORMAL = "\u001b[22m"

fun info(msg: String, source: String = "lcc") {
  System.err.println("${ANSI_BOLD}$source: ${ANSI_BLUE}info:${ANSI_NOCOLOR}${ANSI_NORMAL} $msg")
}

fun warning(msg: String, source: String = "lcc") {
  System.err.println("${ANSI_BOLD}$source: ${ANSI_YELLOW}warning:${ANSI_NOCOLOR}${ANSI_NORMAL} $msg")
}

fun error(msg: String, source: String = "lcc") {
  System.err.println("${ANSI_BOLD}$source: ${ANSI_RED}error:${ANSI_NOCOLOR}${ANSI_NORMAL} $msg")
}

fun fatalError(msg: String, source: String = "lcc"): Nothing {
  System.err.println("${ANSI_BOLD}$source: ${ANSI_RED}fatal error:${ANSI_NOCOLOR}${ANSI_NORMAL} $msg")
  exitProcess(1)
}

fun exitSuccess(): Nothing {
  exitProcess(0)
}
