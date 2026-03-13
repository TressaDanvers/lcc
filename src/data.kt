package ch.protonmail.tdanvers.lambda

sealed interface Expression
sealed interface Value: Expression
sealed interface Decomposed

data class Symbol(val c: Char): Value, Decomposed {
  override fun toString() = "$c"
}


data class Prime(val v: Value): Value {
  override fun toString() = "$v'"
}

data object EmptyExpression: Expression, Decomposed {
  override fun toString() = "<e>"
}

data class Parenthetical(val e: Expression): Expression {
  override fun toString() = "PAR( $e )"
}

data class Lambda(val v: Value, val e: Expression): Expression {
  override fun toString() = "LAM( v=$v e=($e) )"
}

data class Application(val f: Expression, val x: Expression): Expression, Decomposed {
  override fun toString() = "APPL( f=$f x=$x )"
}

fun Expression.prettyString(): String =
  prettyStringRecursive(this.decompose(), "")

tailrec fun prettyStringRecursive(expression: Decomposed, string: String): String =
  when (expression) {
    is EmptyExpression -> string
    is Symbol -> "$string${expression.c}"
    is Application -> when (expression.f) {
      is EmptyExpression -> prettyStringRecursive(expression.x.decompose(), string)
      is Symbol -> prettyStringRecursive(expression.x.decompose(), "$string${expression.f.c}")
      else -> throw IllegalStateException()
    }
  }

fun Expression.decompose(): Decomposed {
  var expression = this

  if (expression !is Decomposed) {
    expression = when (expression) {
      is Prime -> Application(expression.v, Symbol('\''))
      is Parenthetical -> Application(Symbol('('), Application(expression.e, Symbol(')')))
      is Lambda -> Application(Symbol('λ'), Application(expression.v, Application(Symbol('.'), expression.e)))
    }
  }

  if (expression is Application) do {
    while ((expression as Application).f is Application)
      expression = Application((expression.f as Application).f, Application(expression.f.x, expression.x))

    if (expression.f !is Decomposed) {
      expression = Application(when (expression.f) {
        is Prime -> Application(expression.f.v, Symbol('\''))
        is Parenthetical -> Application(Symbol('('), Application(expression.f.e, Symbol(')')))
        is Lambda -> Application(Symbol('λ'), Application(expression.f.v, Application(Symbol('.'), expression.f.e)))
      }, expression.x)
    }
  } while (expression.f !is Decomposed || expression.f is Application)

  return expression as Decomposed
}

