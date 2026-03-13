package ch.protonmail.tdanvers.lambda

sealed interface Expression
sealed interface Value: Expression

data class Symbol(val c: Char): Value
data class Prime(val v: Value): Value

data object EmptyExpression: Expression
data class Parenthetical(val e: Expression): Expression
data class Lambda(val v: Value, val e: Expression): Expression
data class Application(val f: Expression, val x: Expression): Expression

fun Expression.prettyString(): String = when(this) {
  is Symbol -> "$c"
  is Prime -> "${v.prettyString()}'"
  is EmptyExpression -> ""
  is Parenthetical -> "(${e.prettyString()})"
  is Lambda -> "λ${v.prettyString()}.${e.prettyString()}"
  is Application -> "${f.prettyString()}${x.prettyString()}"
}
