package ch.protonmail.tdanvers.lambda

import kotlin.io.path.*
import java.nio.file.*

fun Path.sequencedReader() = reader().let { reader ->
  sequence {
    var c = reader.read()
    while (c != -1) {
      yield(c.toChar())
      c = reader.read()
    }
    reader.close()
  }
}

fun <T> Sequence<T>.splitFirst() =
  firstOrNull()?.let { it to drop(1) }

fun <T> Sequence<T>.memoize(): Sequence<T> {
  val list = mutableListOf<T>()
  val iter = iterator()
  return Sequence { iterator {
    var i = 0
    while (i in list.indices || iter.hasNext()) {
      if (i in list.indices) yield(list[i])
      else yield(iter.next().also { list.add(it) })
      i++
    }
  } }
}
