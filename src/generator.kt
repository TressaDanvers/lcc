package ch.protonmail.tdanvers.lambda

import kotlin.io.path.*
import java.nio.file.*

const val SELF_EXECUTING_STUB = $$"#!/bin/sh\nexec java -jar $0 \"$@\"\n"

fun generateAndEmit(expression: Expression, targetPath: Path) {
  val kotlinSourcePath = parsePath("$targetPath.kt")
  val jarPath = parsePath("$targetPath.jar")

  try {
    emit(generateKotlinSource(expression), kotlinSourcePath)

    ProcessBuilder("kotlinc", "$kotlinSourcePath", "-include-runtime", "-d", "$jarPath")
      .inheritIO().start().waitFor()

    targetPath.outputStream().use {
      it.write(SELF_EXECUTING_STUB.toByteArray())
      jarPath.inputStream().use { jar -> jar.copyTo(it) }
    }

    targetPath.toFile().setExecutable(true)
  } finally {
    kotlinSourcePath.toFile().deleteOnExit()
    jarPath.toFile().deleteOnExit()
  }
}

fun generateMainTemplate(expression: Expression) = Sequence { iterator {
  yieldAll("fun main(vararg args: String) =\n  \"".asSequence())
  yieldAll(expression.prettyString().asSequence())
  yieldAll(("\".let { listOf(it) + args }.map { it.asSequence().let(::parse).let(::interpret) }"+
             ".reduce(::Application).let(::interpret).prettyString().let(::println)").asSequence())
} }

fun generateKotlinSource(expression: Expression) = Sequence { iterator {
  yieldAll("$EMBEDDED_INTERPRETER\n".asSequence())
  yieldAll(generateMainTemplate(expression))
} }

fun emit(chars: Sequence<Char>, target: Path) =
  target.writer().use { writer ->
    chars.map(Char::code).forEach(writer::write)
  }
