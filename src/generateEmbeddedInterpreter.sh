#!/usr/bin/env sh
(
  echo 'package ch.protonmail.tdanvers.lambda'
  echo 'const val EMBEDDED_INTERPRETER=$$"""'
  echo 'package ch.protonmail.tdanvers.lambda.interpreter.specific'
  echo 'import kotlin.io.path.*'
  echo 'import java.nio.file.*'
  echo 'import kotlin.system.*'
  sed 's/package ch.protonmail.tdanvers.lambda//' src/util.kt | sed 's/^import.*$//g'
  sed 's/package ch.protonmail.tdanvers.lambda//' src/proc.kt | sed 's/^import.*$//g'
  sed 's/package ch.protonmail.tdanvers.lambda//' src/data.kt
  sed 's/package ch.protonmail.tdanvers.lambda//' src/parser.kt
  sed 's/package ch.protonmail.tdanvers.lambda//' src/interpreter.kt
  echo '"""'
) > out/embeddedInterpreter.kt
